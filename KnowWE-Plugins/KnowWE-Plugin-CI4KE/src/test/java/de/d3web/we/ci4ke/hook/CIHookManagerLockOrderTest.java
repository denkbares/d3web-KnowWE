/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.ci4ke.hook;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import connector.DummyConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.TestUtils;

import com.denkbares.plugin.test.InitPluginManager;
import com.denkbares.strings.Strings;
import de.d3web.we.ci4ke.build.CIBuildManager;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.CompilationStartEvent;
import de.knowwe.core.kdom.Article;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the lock order between {@link CIHookManager} and {@link CIBuildManager}: a request thread triggering the
 * hooks (e.g. the admin action "Download Finger-Print") must not deadlock with the compile thread handling the start
 * of a compilation. A deadlock there would hang the compile thread forever and with it every further compilation.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 26.11.2026
 */
public class CIHookManagerLockOrderTest {

	private static final long TIMEOUT_SECONDS = 10;

	private Environment environment;
	private Article article;
	private CIDashboard dashboard;
	// closing the environment needs the monitors of deadlocked threads, so it would hang the test run
	private boolean deadlocked = false;

	@Before
	public void setUp() throws Exception {
		InitPluginManager.init();
		DummyConnector connector = new DummyConnector();
		connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
		environment = Environment.create(connector);
		ArticleManager articleManager = environment.getArticleManager();
		String dashboardSource = Strings.readStream(CIHookManagerLockOrderTest.class.getResourceAsStream("/Dashboard.txt"));
		articleManager.registerArticle("Dashboard", dashboardSource);
		assertTrue("dashboard compilation did not finish", articleManager.getCompilerManager().awaitTermination(5000));
		article = articleManager.getArticle("Dashboard");
		dashboard = CIDashboardManager.getDashboard(articleManager, "dashname");
		assertNotNull("test dashboard was not registered", dashboard);
	}

	@After
	public void closeEnvironment() {
		if (environment != null && !deadlocked) environment.close();
	}

	/**
	 * The request thread is paused while it holds the monitor of the hook manager (inside
	 * {@link CIHookManager#triggerHooks(Article)}). Meanwhile, the compile thread handles a {@link
	 * CompilationStartEvent} in the build manager and waits for the hook manager's monitor. When the request thread
	 * continues, it starts the builds in the build manager. Before the fix, the compile thread still held the build
	 * manager's monitor at that point, so both threads waited for each other forever.
	 */
	@Test(timeout = 60000)
	public void triggeringHooksDuringCompilationStartDoesNotDeadlock() throws Exception {
		CIHookManager hookManager = CIHookManager.of(environment);
		CIBuildManager buildManager = CIBuildManager.of(environment);
		PausingHook hook = new PausingHook(dashboard, article.getTitle());
		hookManager.registerHook(hook);

		Thread request = new Thread(() -> hookManager.triggerHooks(article), "Test-Request-TriggerHooks");
		hook.pauseIn(request);
		Thread compile = new Thread(() -> buildManager.notify(
				new CompilationStartEvent(environment.getArticleManager().getCompilerManager())),
				"Test-Compile-CompilationStart");
		// deadlocked threads must not keep the test JVM alive
		request.setDaemon(true);
		compile.setDaemon(true);

		request.start();
		assertTrue("request thread did not reach the hook manager", hook.paused.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		compile.start();
		awaitBlockedOnOrTerminated(compile, hookManager);
		hook.resume.countDown();

		request.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
		compile.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
		assertNoDeadlock(request, compile);
		assertFalse("request thread did not finish", request.isAlive());
		assertFalse("compile thread did not finish", compile.isAlive());

		// let the triggered build end before the environment is closed
		buildManager.awaitTermination();
	}

	/**
	 * Waits until the specified thread waits for the monitor of the specified object, or has terminated.
	 */
	private static void awaitBlockedOnOrTerminated(Thread thread, Object monitor) throws InterruptedException {
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();
		long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
		while (System.nanoTime() < end) {
			if (thread.getState() == Thread.State.TERMINATED) return;
			ThreadInfo info = threads.getThreadInfo(thread.getId());
			LockInfo lock = info == null ? null : info.getLockInfo();
			if (info != null && info.getThreadState() == Thread.State.BLOCKED
					&& lock != null && lock.getIdentityHashCode() == System.identityHashCode(monitor)) {
				return;
			}
			//noinspection BusyWait
			Thread.sleep(10);
		}
		fail(thread.getName() + " neither waits for the monitor of " + monitor + " nor has it terminated");
	}

	private void assertNoDeadlock(Thread... candidates) {
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();
		long[] deadlockedThreads = threads.findDeadlockedThreads();
		if (deadlockedThreads == null) return;
		List<Long> deadlockedIds = Arrays.stream(deadlockedThreads).boxed().toList();
		StringBuilder message = new StringBuilder();
		for (Thread candidate : candidates) {
			if (!deadlockedIds.contains(candidate.getId())) continue;
			ThreadInfo info = threads.getThreadInfo(candidate.getId());
			message.append("\n").append(candidate.getName()).append(" waits for ").append(info.getLockName())
					.append(" held by ").append(info.getLockOwnerName());
		}
		if (!message.isEmpty()) {
			deadlocked = true;
			fail("Deadlock between request and compile thread:" + message);
		}
	}

	/**
	 * Hook that pauses the specified thread while it is checked by {@link CIHookManager#triggerHooks(Article)}, i.e.
	 * while that thread holds the monitor of the hook manager. Apart from that it behaves like a normal hook.
	 */
	private static class PausingHook extends CIHook {

		private final CountDownLatch paused = new CountDownLatch(1);
		private final CountDownLatch resume = new CountDownLatch(1);
		private volatile Thread pausingThread;

		private PausingHook(CIDashboard dashboard, String monitoredArticle) {
			super(dashboard, List.of(monitoredArticle));
		}

		private void pauseIn(Thread thread) {
			this.pausingThread = thread;
		}

		@Override
		public int getLastTrigger() {
			if (Thread.currentThread() == pausingThread) {
				pausingThread = null;
				paused.countDown();
				try {
					if (!resume.await(TIMEOUT_SECONDS * 3, TimeUnit.SECONDS)) {
						throw new IllegalStateException("paused thread was not resumed");
					}
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			return super.getLastTrigger();
		}
	}
}
