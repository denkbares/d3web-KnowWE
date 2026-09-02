/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package com.denkbares.versioning.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.URI;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import de.uniwue.d3web.gitConnector.UserCredentials;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The wiki's side of the launcher's credential channel: fetched when needed, kept until shortly before it expires,
 * one fetch however many ask at once, over http and over a unix domain socket alike.
 */
class LauncherCredentialProviderTest {

	private static final Instant NOW = Instant.parse("2026-09-02T12:00:00Z");

	private final List<AutoCloseable> running = new ArrayList<>();

	@AfterEach
	void stop() throws Exception {
		for (AutoCloseable server : running) {
			server.close();
		}
	}

	@Test
	void aCredentialIsFetchedOverHttpAndKeptUntilShortlyBeforeItExpires() throws Exception {
		AtomicInteger fetches = new AtomicInteger();
		URI launcher = httpLauncher(200, () -> body("oauth2", "token-" + fetches.incrementAndGet(),
				NOW.plus(Duration.ofMinutes(10))));
		MutableClock clock = new MutableClock(NOW);
		LauncherCredentialProvider provider = new LauncherCredentialProvider(launcher, clock);

		UserCredentials first = provider.get();
		assertEquals("oauth2", first.user);
		assertEquals("token-1", first.password);
		// still valid, so the same credential without another round trip
		clock.advance(Duration.ofMinutes(8));
		assertEquals("token-1", provider.get().password);
		assertEquals(1, fetches.get());
		// inside the skew before expiry, so the next request fetches again
		clock.advance(Duration.ofMinutes(1).plusSeconds(1));
		assertEquals("token-2", provider.get().password);
		assertEquals(2, fetches.get());
	}

	@Test
	void aCredentialWithoutExpiryIsKeptForGood() throws Exception {
		AtomicInteger fetches = new AtomicInteger();
		URI launcher = httpLauncher(200, () -> {
			fetches.incrementAndGet();
			return "{\"username\":\"launcher\",\"secret\":\"a-token\",\"expires\":null}";
		});
		MutableClock clock = new MutableClock(NOW);
		LauncherCredentialProvider provider = new LauncherCredentialProvider(launcher, clock);

		assertEquals("a-token", provider.get().password);
		clock.advance(Duration.ofDays(400));
		assertEquals("a-token", provider.get().password);
		assertEquals(1, fetches.get());
	}

	@Test
	void manyWaitingPushesShareOneFetch() throws Exception {
		AtomicInteger fetches = new AtomicInteger();
		CountDownLatch release = new CountDownLatch(1);
		URI launcher = httpLauncher(200, () -> {
			fetches.incrementAndGet();
			try {
				release.await(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return body("oauth2", "token", null);
		});
		LauncherCredentialProvider provider = new LauncherCredentialProvider(launcher, new MutableClock(NOW));

		ExecutorService callers = Executors.newFixedThreadPool(4);
		try {
			List<Future<UserCredentials>> answers = new ArrayList<>();
			for (int i = 0; i < 4; i++) {
				answers.add(callers.submit(provider::get));
			}
			Thread.sleep(200);
			release.countDown();
			for (Future<UserCredentials> answer : answers) {
				assertEquals("token", answer.get(5, TimeUnit.SECONDS).password);
			}
		}
		finally {
			callers.shutdownNow();
		}
		assertEquals(1, fetches.get());
	}

	@Test
	void aRefusalIsStatedInTheLaunchersWords() throws Exception {
		URI launcher = httpLauncher(403,
				() -> "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"hedwig has to sign in again\"}");
		LauncherCredentialProvider provider = new LauncherCredentialProvider(launcher, new MutableClock(NOW));

		IllegalStateException refused = assertThrows(IllegalStateException.class, provider::get);
		assertTrue(refused.getMessage().contains("hedwig has to sign in again"), refused.getMessage());
		assertTrue(refused.getMessage().contains("refused"), refused.getMessage());
	}

	@Test
	void anUnreachableLauncherIsNamedRatherThanGuessedAt() {
		LauncherCredentialProvider provider = new LauncherCredentialProvider(URI.create("http://127.0.0.1:1"),
				new MutableClock(NOW));

		IllegalStateException failed = assertThrows(IllegalStateException.class, provider::get);
		assertTrue(failed.getMessage().contains("http://127.0.0.1:1"), failed.getMessage());
	}

	@Test
	void aCredentialIsFetchedOverAUnixDomainSocket() throws Exception {
		String json = body("oauth2", "socket-token", NOW.plus(Duration.ofHours(2)));
		AtomicReference<String> request = new AtomicReference<>();
		URI launcher = socketLauncher(request, "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n"
				+ "Content-Length: " + json.getBytes(StandardCharsets.UTF_8).length + "\r\nConnection: close\r\n\r\n" + json);
		LauncherCredentialProvider provider = new LauncherCredentialProvider(launcher, new MutableClock(NOW));

		assertEquals("socket-token", provider.get().password);
		assertTrue(request.get().startsWith("POST /api/workspace/credential HTTP/1.1\r\n"), request.get());
	}

	@Test
	void aChunkedAnswerOverTheSocketIsReadWhole() throws Exception {
		String json = body("oauth2", "chunked-token", null);
		int split = json.length() / 2;
		String chunked = "HTTP/1.1 200 OK\r\nTransfer-Encoding: chunked\r\nConnection: close\r\n\r\n"
				+ Integer.toHexString(split) + "\r\n" + json.substring(0, split) + "\r\n"
				+ Integer.toHexString(json.length() - split) + "\r\n" + json.substring(split) + "\r\n0\r\n\r\n";
		URI launcher = socketLauncher(new AtomicReference<>(), chunked);
		LauncherCredentialProvider provider = new LauncherCredentialProvider(launcher, new MutableClock(NOW));

		assertEquals("chunked-token", provider.get().password);
	}

	@Test
	void onlyHttpAndSocketsAreChannels() {
		assertThrows(IllegalArgumentException.class,
				() -> new LauncherCredentialProvider(URI.create("ftp://launcher"), new MutableClock(NOW)));
		assertDoesNotThrow(() -> new LauncherCredentialProvider(URI.create("unix:/tmp/x.sock"), new MutableClock(NOW)));
		assertDoesNotThrow(() -> new LauncherCredentialProvider(URI.create("https://launcher"), new MutableClock(NOW)));
	}

	private static String body(String username, String secret, Instant expires) {
		return "{\"username\":\"" + username + "\",\"secret\":\"" + secret + "\",\"expires\":"
				+ (expires == null ? "null" : "\"" + expires + "\"") + "}";
	}

	/**
	 * A launcher over http answering every credential request with the given status and body.
	 */
	private URI httpLauncher(int status, java.util.function.Supplier<String> body) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.setExecutor(Executors.newCachedThreadPool());
		server.createContext(LauncherCredentialProvider.CREDENTIAL_PATH, exchange -> {
			byte[] answer = body.get().getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(status, answer.length);
			exchange.getResponseBody().write(answer);
			exchange.close();
		});
		server.start();
		running.add(() -> server.stop(0));
		return URI.create("http://127.0.0.1:" + server.getAddress().getPort());
	}

	/**
	 * A launcher on a unix domain socket answering the next request with the given raw http response, recording what
	 * it was asked.
	 */
	private URI socketLauncher(AtomicReference<String> request, String rawResponse) throws IOException {
		Path directory = Files.createTempDirectory("launcher-channel");
		Path socket = directory.resolve("w.sock");
		ServerSocketChannel server = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
		server.bind(UnixDomainSocketAddress.of(socket));
		Thread listener = new Thread(() -> {
			try (SocketChannel client = server.accept()) {
				ByteBuffer buffer = ByteBuffer.allocate(4096);
				StringBuilder received = new StringBuilder();
				while (!received.toString().contains("\r\n\r\n") && client.read(buffer) >= 0) {
					buffer.flip();
					received.append(StandardCharsets.UTF_8.decode(buffer));
					buffer.clear();
				}
				request.set(received.toString());
				client.write(StandardCharsets.UTF_8.encode(rawResponse));
			}
			catch (IOException ignored) {
				// the test stopped the server
			}
		}, "fake-launcher");
		listener.setDaemon(true);
		listener.start();
		running.add(() -> {
			server.close();
			Files.deleteIfExists(socket);
			Files.deleteIfExists(directory);
		});
		return URI.create("unix:" + socket.toAbsolutePath());
	}

	/**
	 * A clock the test moves by hand.
	 */
	private static final class MutableClock extends Clock {

		private Instant now;

		MutableClock(Instant now) {
			this.now = now;
		}

		void advance(Duration by) {
			now = now.plus(by);
		}

		@Override
		public ZoneOffset getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(java.time.ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}
}
