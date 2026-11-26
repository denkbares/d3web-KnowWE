/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.kdom;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import connector.DummyConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.TestUtils;

import com.denkbares.plugin.test.InitPluginManager;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyMarkup;
import de.knowwe.rdf2go.Rdf2GoCore;

import static de.knowwe.core.kdom.parsing.Sections.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the import of an ontology from a URL during the compilation. The download runs on the compile thread, so an
 * unresponsive server must not stop the compilation, and the attachment must only be stored once the download is
 * complete, because storing it blocks every other attachment write of the wiki.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 26.11.2026
 */
public class InitTerminologyHandlerUrlImportTest {

	private static final long TIMEOUT_SECONDS = 30;
	private static final String TURTLE = "@prefix ex: <http://example.org/import#> .\n"
			+ "ex:ImportedConcept a <http://www.w3.org/2002/07/owl#Class> .\n";

	private Environment environment;
	private RecordingConnector connector;
	private HttpServer server;
	private ExecutorService serverExecutor;

	@BeforeClass
	public static void initialize() throws IOException {
		InitPluginManager.init();
	}

	@Before
	public void setUp() throws IOException {
		connector = new RecordingConnector();
		connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
		environment = Environment.create(connector);
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		serverExecutor = Executors.newCachedThreadPool();
		server.setExecutor(serverExecutor);
		server.start();
		InitTerminologyHandler.setDownloadTimeouts(1000, 1000);
	}

	@After
	public void tearDown() {
		InitTerminologyHandler.setDownloadTimeouts(InitTerminologyHandler.DEFAULT_CONNECT_TIMEOUT_MILLIS,
				InitTerminologyHandler.DEFAULT_READ_TIMEOUT_MILLIS);
		// stopping the server releases a compilation still waiting for an answer
		server.stop(0);
		serverExecutor.shutdownNow();
		environment.close();
	}

	@Test(timeout = 120000)
	public void unresponsiveServerDoesNotStopTheCompilation() throws Exception {
		CountDownLatch released = new CountDownLatch(1);
		server.createContext("/unresponsive", exchange -> {
			// accept the request, but never answer
			try {
				released.await(TIMEOUT_SECONDS * 3, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			exchange.close();
		});
		try {
			Article article = registerImport("Unresponsive", "/unresponsive");
			assertTrue("compilation did not finish while the server did not answer", awaitCompilation());
			assertTrue("missing error for the failed download, messages: " + errors(article),
					errors(article).contains("Unable to cache ontology file"));
		}
		finally {
			released.countDown();
		}
	}

	@Test(timeout = 120000)
	public void importedOntologyIsStoredAndCompiled() throws Exception {
		server.createContext("/ontology", exchange -> {
			// content negotiation, the extension of the attachment is taken from the Content-Location
			exchange.getResponseHeaders().add("Content-Location", "ontology.ttl");
			exchange.getResponseHeaders().add("Content-Type", "text/turtle");
			sendBody(exchange, TURTLE.getBytes(StandardCharsets.UTF_8), null);
		});
		Article article = registerImport("Imported", "/ontology");
		assertTrue("compilation did not finish", awaitCompilation());

		assertEquals("", errors(article));
		WikiAttachment attachment = connector.getStoredAttachment();
		assertNotNull("ontology was not stored as attachment", attachment);
		assertTrue(attachment.getFileName(), attachment.getFileName().endsWith(".ttl"));
		try (InputStream stream = attachment.getInputStream()) {
			assertEquals(TURTLE, new String(stream.readAllBytes(), StandardCharsets.UTF_8));
		}
		Rdf2GoCore core = Compilers.getCompiler($(article).successor(OntologyMarkup.class).getFirst(), OntologyCompiler.class)
				.getRdf2GoCore();
		assertTrue("imported concept is missing", core.sparqlAsk(
				"ASK { <http://example.org/import#ImportedConcept> ?p ?o }", Rdf2GoCore.Options.NO_CACHE));
	}

	@Test(timeout = 120000)
	public void attachmentIsStoredOnlyAfterTheDownloadIsComplete() throws Exception {
		// the server pauses in the middle of the ontology, the download must wait for it
		InitTerminologyHandler.setDownloadTimeouts(1000, 10000);
		AtomicBoolean downloadComplete = new AtomicBoolean(false);
		connector.setDownloadComplete(downloadComplete);
		server.createContext("/slow", exchange -> {
			exchange.getResponseHeaders().add("Content-Type", "text/turtle");
			sendBody(exchange, TURTLE.getBytes(StandardCharsets.UTF_8), downloadComplete);
		});
		Article article = registerImport("Slow", "/slow.ttl");
		assertTrue("compilation did not finish", awaitCompilation());

		assertEquals("", errors(article));
		assertNotNull("ontology was not stored as attachment", connector.getStoredAttachment());
		assertFalse("the attachment was stored while the ontology was still downloaded",
				connector.wasStoredBeforeDownloadComplete());
	}

	/**
	 * Sends the body to the client. If a flag is given, the first half is sent, then the server pauses, sets the flag
	 * and sends the rest.
	 */
	private static void sendBody(HttpExchange exchange, byte[] body, AtomicBoolean complete) throws IOException {
		if ("HEAD".equals(exchange.getRequestMethod())) {
			exchange.sendResponseHeaders(200, -1);
			exchange.close();
			return;
		}
		exchange.sendResponseHeaders(200, body.length);
		try (OutputStream out = exchange.getResponseBody()) {
			if (complete == null) {
				out.write(body);
				return;
			}
			int half = body.length / 2;
			out.write(body, 0, half);
			out.flush();
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			complete.set(true);
			out.write(body, half, body.length - half);
		}
	}

	private Article registerImport(String title, String path) {
		String url = "http://" + server.getAddress().getHostString() + ":" + server.getAddress().getPort() + path;
		ArticleManager manager = environment.getArticleManager();
		manager.registerArticle(title, "%%Ontology\n@import: " + url + "\n%\n");
		Article article = manager.getArticle(title);
		assertNotNull(article);
		return article;
	}

	private boolean awaitCompilation() throws InterruptedException {
		return environment.getArticleManager()
				.getCompilerManager()
				.awaitTermination(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
	}

	private static String errors(Article article) {
		Section<OntologyMarkup> markup = $(article).successor(OntologyMarkup.class).getFirst();
		// the errors are stored for the compiler, so the compiler independent lookup would not find them
		StringBuilder text = new StringBuilder();
		for (Collection<Message> errors : Messages.getMessagesMapFromSubtree(markup, Message.Type.ERROR).values()) {
			for (Message error : errors) {
				text.append(error.getVerbalization());
			}
		}
		return text.toString();
	}

	/**
	 * Connector remembering the stored attachment, and whether it was stored before the server sent the whole
	 * ontology.
	 */
	private static class RecordingConnector extends DummyConnector {

		private final AtomicReference<WikiAttachment> stored = new AtomicReference<>();
		private volatile AtomicBoolean downloadComplete = new AtomicBoolean(true);
		private volatile boolean storedBeforeDownloadComplete = false;

		void setDownloadComplete(AtomicBoolean downloadComplete) {
			this.downloadComplete = downloadComplete;
		}

		boolean wasStoredBeforeDownloadComplete() {
			return storedBeforeDownloadComplete;
		}

		WikiAttachment getStoredAttachment() {
			return stored.get();
		}

		@Override
		public WikiAttachment storeAttachment(String title, String filename, String user, InputStream stream) throws IOException {
			if (!downloadComplete.get()) storedBeforeDownloadComplete = true;
			// attachments stored from a stream have a new date on each access, which would make the import discard
			// its cached terminology right away, so the attachment is stored as a file
			Path file = Files.createTempDirectory("ontology-import-test-").resolve(filename);
			Files.copy(stream, file);
			file.toFile().deleteOnExit();
			file.getParent().toFile().deleteOnExit();
			WikiAttachment attachment = super.storeAttachment(title, user, file.toFile());
			stored.set(attachment);
			return attachment;
		}
	}
}
