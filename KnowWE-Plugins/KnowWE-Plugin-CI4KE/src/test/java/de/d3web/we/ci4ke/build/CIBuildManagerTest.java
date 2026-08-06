/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.ci4ke.build;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import connector.DummyConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.TestUtils;

import com.denkbares.plugin.test.InitPluginManager;
import com.denkbares.strings.Strings;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the asynchronous predecessor/successor handover through the public build-starting methods of
 * {@link CIBuildManager}. The build executor is deliberately recording-only: a submitted build remains pending until
 * the test explicitly executes its future, making the scheduling order deterministic without relying on sleeps.
 */
public class CIBuildManagerTest {
	private static CIDashboard dashboard;

	private RecordingExecutorService buildExecutor;
	private ExecutorService triggerExecutor;
	private CIBuildManager buildManager;

	@BeforeClass
	public static void setUpDashboard() throws Exception {
		InitPluginManager.init();
		if (!Environment.isInitialized()) {
			DummyConnector connector = new DummyConnector();
			connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
			Environment.initInstance(connector);
		}
		ArticleManager articleManager = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
		String dashboardSource = Strings.readStream(CIBuildManagerTest.class.getResourceAsStream("/Dashboard.txt"));
		articleManager.registerArticle("Dashboard", dashboardSource);
		assertTrue("dashboard compilation did not finish", articleManager.getCompilerManager().awaitTermination(5000));
		dashboard = CIDashboardManager.getDashboard(articleManager, "dashname");
		assertNotNull("test dashboard was not registered", dashboard);
	}

	@Before
	public void setUp() {
		buildExecutor = new RecordingExecutorService();
		triggerExecutor = Executors.newSingleThreadExecutor();
		buildManager = new CIBuildManager(buildExecutor, triggerExecutor);
	}

	@After
	public void tearDown() throws InterruptedException {
		buildManager.shutDownNow();
		buildExecutor.shutdownNow();
		triggerExecutor.shutdownNow();
		assertTrue(triggerExecutor.awaitTermination(1, TimeUnit.SECONDS));
	}

	/**
	 * Calling {@link CIBuildManager#startBuild(CIDashboard)} for a dashboard that already has a build must return while
	 * the predecessor is still pending. The successor may only reach the build executor after that predecessor becomes
	 * terminal.
	 */
	@Test(timeout = 5000)
	public void startBuildWaitsAsynchronouslyForPredecessor() throws Exception {
		buildManager.startBuild(dashboard);
		Runnable predecessor = buildExecutor.awaitSubmittedBuild();

		buildManager.startBuild(dashboard);
		buildExecutor.assertNoBuildSubmitted();

		predecessor.run();
		Runnable successor = buildExecutor.awaitSubmittedBuild();
		assertFalse(((Future<?>) successor).isCancelled());
	}

	/**
	 * The batch entry point must use the same handover. This protects the predecessor mapping in
	 * {@link CIBuildManager#startBuilds(Set)}, rather than only testing the single-dashboard path.
	 */
	@Test(timeout = 5000)
	public void startBuildsWaitsAsynchronouslyForPredecessor() throws Exception {
		buildManager.startBuild(dashboard);
		Runnable predecessor = buildExecutor.awaitSubmittedBuild();

		buildManager.startBuilds(Set.of(dashboard));
		buildExecutor.assertNoBuildSubmitted();

		predecessor.run();
		Runnable successor = buildExecutor.awaitSubmittedBuild();
		assertFalse(((Future<?>) successor).isCancelled());
	}

	/**
	 * Models a compilation start aborting a successor while it is waiting for the old build. Once the predecessor ends,
	 * the aborted successor must be discarded and must never reach the build executor.
	 */
	@Test(timeout = 5000)
	public void abortedSuccessorIsNotSubmittedAfterPredecessorEnds() throws Exception {
		buildManager.startBuild(dashboard);
		Runnable predecessor = buildExecutor.awaitSubmittedBuild();

		buildManager.startBuild(dashboard);
		buildManager.shutDownNow(dashboard);
		predecessor.run();
		awaitTriggerExecutorIdle();

		buildExecutor.assertNoBuildSubmitted();
	}

	private void awaitTriggerExecutorIdle() throws Exception {
		triggerExecutor.submit(() -> {}).get(1, TimeUnit.SECONDS);
	}

	/**
	 * Executor that records submitted build futures without running them. The test can therefore decide exactly when a
	 * predecessor executes and becomes terminal.
	 */
	private static class RecordingExecutorService extends AbstractExecutorService {
		private final BlockingQueue<Runnable> submittedBuilds = new LinkedBlockingQueue<>();
		private volatile boolean shutdown;

		@Override
		public void shutdown() {
			shutdown = true;
		}

		@Override
		public List<Runnable> shutdownNow() {
			shutdown = true;
			List<Runnable> remaining = new ArrayList<>();
			submittedBuilds.drainTo(remaining);
			for (Runnable task : remaining) {
				if (task instanceof Future<?> future) {
					future.cancel(false);
				}
			}
			return remaining;
		}

		@Override
		public boolean isShutdown() {
			return shutdown;
		}

		@Override
		public boolean isTerminated() {
			return shutdown && submittedBuilds.isEmpty();
		}

		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit) {
			return isTerminated();
		}

		@Override
		public void execute(Runnable command) {
			if (shutdown) throw new RejectedExecutionException("executor is shut down");
			submittedBuilds.add(command);
		}

		private Runnable awaitSubmittedBuild() throws InterruptedException {
			Runnable submittedBuild = submittedBuilds.poll(1, TimeUnit.SECONDS);
			assertNotNull("no build was submitted", submittedBuild);
			assertTrue("submitted build is not a future", submittedBuild instanceof Future<?>);
			return submittedBuild;
		}

		private void assertNoBuildSubmitted() throws InterruptedException {
			assertNull("an unexpected build was submitted",
					submittedBuilds.poll(200, TimeUnit.MILLISECONDS));
		}
	}
}
