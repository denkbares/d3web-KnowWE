/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.d3web.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import connector.DummyConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.TestUserContext;
import utils.TestUtils;

import com.denkbares.plugin.test.InitPluginManager;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests that downloading a knowledge base blocks the compilation only while the knowledge base is saved, not while
 * it is streamed to the client. Otherwise, a slow client keeps every page save waiting for the end of the download.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 26.11.2026
 */
public class KnowledgeBaseDownloadActionTest {

	private static final long TIMEOUT_SECONDS = 20;

	private static Environment environment;
	private static Article article;

	@BeforeClass
	public static void initialize() throws Exception {
		InitPluginManager.init();
		DummyConnector connector = new DummyConnector();
		connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
		environment = Environment.create(connector);
		ArticleManager manager = environment.getArticleManager();
		manager.registerArticle("KnowledgeBaseDownload", "%%KnowledgeBase\n@name DownloadTest\n%\n");
		assertTrue("compilation did not finish", manager.getCompilerManager().awaitTermination(60000));
		article = manager.getArticle("KnowledgeBaseDownload");
		assertNotNull(article);
	}

	@AfterClass
	public static void shutdown() {
		if (environment != null) environment.close();
	}

	@Test(timeout = 120000)
	public void pageSaveIsNotBlockedWhileStreaming() throws Exception {
		SlowClientStream client = new SlowClientStream();
		DownloadContext context = new DownloadContext(article, client);

		ArticleManager manager = environment.getArticleManager();
		String otherTitle = "Saved during download " + UUID.randomUUID();
		ExecutorService executor = Executors.newCachedThreadPool();
		try {
			Future<?> download = executor.submit(() -> {
				new KnowledgeBaseDownloadAction().execute(context);
				return null;
			});
			try {
				client.awaitStreaming(download);
				Future<?> save = executor.submit(() -> manager.registerArticle(otherTitle, "Some text"));
				try {
					save.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
				}
				catch (TimeoutException e) {
					fail("Saving a page was blocked while the download was streamed to the client");
				}
				assertTrue("compilation did not finish while the download was streamed to the client",
						manager.getCompilerManager().awaitTermination(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS)));
			}
			finally {
				client.release();
			}
			download.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			// a saved knowledge base is a zip file
			byte[] content = client.getContent();
			assertTrue("download is no knowledge base file",
					content.length > 4 && content[0] == 'P' && content[1] == 'K');
		}
		finally {
			executor.shutdown();
			assertTrue(executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			if (manager.getArticle(otherTitle) != null) manager.deleteArticle(otherTitle);
			manager.getCompilerManager().awaitTermination(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
		}
	}

	/**
	 * User context whose response goes to the specified stream.
	 */
	private static class DownloadContext extends TestUserContext {

		private final OutputStream response;

		private DownloadContext(Article article, OutputStream response) {
			super(article);
			this.response = response;
		}

		@Override
		public OutputStream getOutputStream() {
			return response;
		}
	}

	/**
	 * Response stream of a slow client: every write blocks until the stream is released.
	 */
	private static class SlowClientStream extends OutputStream {

		private final CountDownLatch streaming = new CountDownLatch(1);
		private final CountDownLatch released = new CountDownLatch(1);
		private final ByteArrayOutputStream content = new ByteArrayOutputStream();

		@Override
		public void write(int b) throws IOException {
			awaitRelease();
			content.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			awaitRelease();
			content.write(b, off, len);
		}

		private void awaitRelease() throws IOException {
			streaming.countDown();
			try {
				if (!released.await(TIMEOUT_SECONDS * 3, TimeUnit.SECONDS)) {
					throw new IOException("client stream was not released");
				}
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new InterruptedIOException();
			}
		}

		/**
		 * Waits until the download writes to this stream, failing if the download ends before.
		 */
		private void awaitStreaming(Future<?> download) throws Exception {
			long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
			while (!streaming.await(50, TimeUnit.MILLISECONDS)) {
				if (download.isDone()) {
					download.get();
					fail("download finished without writing to the client");
				}
				if (System.nanoTime() > end) fail("download did not start to write to the client");
			}
		}

		private void release() {
			released.countDown();
		}

		private byte[] getContent() {
			return content.toByteArray();
		}
	}
}
