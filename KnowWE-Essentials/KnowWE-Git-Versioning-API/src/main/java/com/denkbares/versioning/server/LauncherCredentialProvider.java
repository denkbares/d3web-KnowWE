/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package com.denkbares.versioning.server;

import java.io.IOException;
import java.net.URI;
import java.net.UnixDomainSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniwue.d3web.gitConnector.UserCredentials;

/**
 * The credentials of a wiki that runs as a launcher's workspace: fetched from the launcher over the workspace's own
 * channel whenever they are needed, never written down. The launcher answers with the credential of the workspace's
 * owner, so every push and every server call of this wiki acts as the owner, while the commits themselves are
 * attributed to whoever is signed in to the wiki; a workspace belongs to one person by design, so the two coincide.
 * <p>
 * A credential is kept until shortly before it expires and then fetched again, one fetch at a time however many
 * pushes are waiting for it; a credential without an expiry is kept for the life of the wiki. The channel is what the
 * launcher wrote as {@code launcher.address}: an http address, or {@code unix:} and the path of a socket carrying http.
 */
public final class LauncherCredentialProvider implements Supplier<UserCredentials> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LauncherCredentialProvider.class);

	/**
	 * Where a workspace fetches the credential of its owner, on its channel, as the launcher's contract states it.
	 */
	public static final String CREDENTIAL_PATH = "/api/workspace/credential";

	/**
	 * How long before its expiry a credential is treated as expired, so a push that starts on a credential with
	 * seconds left does not fail halfway through.
	 */
	static final Duration SKEW = Duration.ofSeconds(60);

	private static final Duration TIMEOUT = Duration.ofSeconds(30);

	private final URI launcher;
	private final Clock clock;
	private final ObjectMapper json = new ObjectMapper();

	private @Nullable Fetched current;

	/**
	 * @param launcher the channel to the launcher: an http(s) address, or {@code unix:} and a socket path
	 */
	public LauncherCredentialProvider(@NotNull URI launcher) {
		this(launcher, Clock.systemUTC());
	}

	LauncherCredentialProvider(@NotNull URI launcher, @NotNull Clock clock) {
		if (!isUnixSocket(launcher) && !"http".equals(launcher.getScheme()) && !"https".equals(launcher.getScheme())) {
			throw new IllegalArgumentException("a launcher is reached over http(s) or unix:<socket path>, not " + launcher);
		}
		this.launcher = launcher;
		this.clock = clock;
	}

	/**
	 * The channel the launcher is reached on.
	 */
	public @NotNull URI launcher() {
		return launcher;
	}

	/**
	 * The owner's credential, valid now.
	 *
	 * @throws IllegalStateException if the launcher does not hand one out, which names why in plain words
	 */
	@Override
	public synchronized @NotNull UserCredentials get() {
		if (current == null || current.expired(clock.instant())) {
			current = fetch();
		}
		return current.credentials();
	}

	/**
	 * Forgets the kept credential, so the next request fetches again. For a server that refused a credential the
	 * launcher still considered valid, which a revoked delegation is.
	 */
	public synchronized void invalidate() {
		current = null;
	}

	private Fetched fetch() {
		String body;
		try {
			body = isUnixSocket(launcher) ? postOverSocket() : postOverHttp();
		}
		catch (IOException | InterruptedException e) {
			if (e instanceof InterruptedException) Thread.currentThread().interrupt();
			throw new IllegalStateException("the launcher at " + launcher + " could not be reached for a credential: "
					+ e.getMessage(), e);
		}
		CredentialResponse response;
		try {
			response = json.readValue(body, CredentialResponse.class);
		}
		catch (IOException e) {
			throw new IllegalStateException("the launcher at " + launcher + " answered the credential request with "
					+ "something other than a credential", e);
		}
		if (response.username == null || response.secret == null || response.secret.isBlank()) {
			throw new IllegalStateException("the launcher at " + launcher + " answered the credential request without "
					+ "a username or a secret");
		}
		Instant expires = response.expires == null ? null : Instant.parse(response.expires);
		LOGGER.debug("Fetched the workspace owner's credential from the launcher, valid until {}",
				expires == null ? "revoked" : expires);
		return new Fetched(new UserCredentials(response.username, response.secret), expires);
	}

	private String postOverHttp() throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
		HttpRequest request = HttpRequest.newBuilder(launcher.resolve(CREDENTIAL_PATH))
				.timeout(TIMEOUT)
				.header("Accept", "application/json")
				.POST(HttpRequest.BodyPublishers.noBody())
				.build();
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		return checked(response.statusCode(), response.body());
	}

	/**
	 * HTTP/1.1 by hand over the unix domain socket, because the JDK's http client speaks tcp only: one request with
	 * the connection closed after it, so the response is everything the launcher writes before hanging up.
	 */
	private String postOverSocket() throws IOException {
		Path socket = Path.of(launcher.getSchemeSpecificPart());
		try (SocketChannel channel = SocketChannel.open(UnixDomainSocketAddress.of(socket))) {
			channel.write(StandardCharsets.UTF_8.encode("POST " + CREDENTIAL_PATH + " HTTP/1.1\r\n"
					+ "Host: launcher\r\nAccept: application/json\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"));
			ByteBuffer buffer = ByteBuffer.allocate(8192);
			StringBuilder raw = new StringBuilder();
			while (channel.read(buffer) >= 0) {
				buffer.flip();
				raw.append(StandardCharsets.UTF_8.decode(buffer));
				buffer.clear();
			}
			return parseResponse(raw.toString());
		}
	}

	/**
	 * Splits a raw HTTP/1.1 response into status and body, undoing chunked transfer encoding where the launcher used
	 * it.
	 */
	static String parseResponse(String raw) throws IOException {
		int headerEnd = raw.indexOf("\r\n\r\n");
		if (headerEnd < 0 || !raw.startsWith("HTTP/1.")) {
			throw new IOException("the launcher did not answer with http");
		}
		String[] statusLine = raw.substring(0, raw.indexOf("\r\n")).split(" ", 3);
		int status = Integer.parseInt(statusLine[1]);
		String headers = raw.substring(0, headerEnd).toLowerCase(Locale.ROOT);
		String body = raw.substring(headerEnd + 4);
		if (headers.contains("transfer-encoding: chunked")) {
			body = dechunk(body);
		}
		return checked(status, body);
	}

	private static String dechunk(String chunked) {
		StringBuilder body = new StringBuilder();
		int position = 0;
		while (position < chunked.length()) {
			int lineEnd = chunked.indexOf("\r\n", position);
			if (lineEnd < 0) break;
			String sizeLine = chunked.substring(position, lineEnd);
			int semicolon = sizeLine.indexOf(';');
			int size = Integer.parseInt((semicolon < 0 ? sizeLine : sizeLine.substring(0, semicolon)).trim(), 16);
			if (size == 0) break;
			int chunkStart = lineEnd + 2;
			body.append(chunked, chunkStart, Math.min(chunkStart + size, chunked.length()));
			position = chunkStart + size + 2;
		}
		return body.toString();
	}

	private static String checked(int status, String body) throws IOException {
		if (status == 401 || status == 403) {
			throw new IOException("the launcher refused a credential (" + status + "), " + reasonOf(body));
		}
		if (status != 200) {
			throw new IOException("the launcher answered the credential request with " + status + ", " + reasonOf(body));
		}
		return body;
	}

	/**
	 * The launcher states its refusals in words, in a Spring error body; the words are what the wiki's log should
	 * show.
	 */
	private static String reasonOf(String body) {
		if (body == null || body.isBlank()) return "with no reason given";
		try {
			ErrorResponse error = new ObjectMapper().readValue(body, ErrorResponse.class);
			if (error.message != null && !error.message.isBlank()) return error.message;
			if (error.error != null && !error.error.isBlank()) return error.error;
		}
		catch (IOException ignored) {
			// not a Spring error body, the raw text will do
		}
		return body.strip();
	}

	private static boolean isUnixSocket(URI address) {
		return "unix".equals(address.getScheme());
	}

	private record Fetched(UserCredentials credentials, @Nullable Instant expires) {

		boolean expired(Instant now) {
			return expires != null && !now.plus(SKEW).isBefore(expires);
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static final class CredentialResponse {
		public String username;
		public String secret;
		public String expires;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static final class ErrorResponse {
		public String message;
		public String error;
	}
}
