/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.events.EventManager;
import de.d3web.testing.BuildResult;
import de.d3web.testing.TestExecutor;
import de.d3web.testing.TestObjectProvider;
import de.d3web.testing.TestObjectProviderManager;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.ServletContextEventListener;
import de.knowwe.core.compile.CompilationStartEvent;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.progress.DefaultAjaxProgressListener;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CIBuildManager implements EventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(CIBuildManager.class);

	private static CIBuildManager instance = null;

	public static CIBuildManager getInstance() {
		if (instance == null) instance = new CIBuildManager();
		return instance;
	}

	private CIBuildManager() {
		EventManager.getInstance().registerListener(this);
	}

	private static final AtomicLong executorNumber = new AtomicLong();
	private static final ExecutorService CI_BUILD_EXECUTOR = Executors.newCachedThreadPool(
			r -> new Thread(r, "CI-Build-Executor-" + executorNumber.incrementAndGet()));
	private static final AtomicLong THREAD_NUMBER = new AtomicLong();
	private static final ExecutorService TEST_EXECUTOR_SERVICE = Executors.newFixedThreadPool(
			(int) Math.max(2, Runtime.getRuntime().availableProcessors() * 0.6),
			r -> new Thread(r, "CI-Test-Executor-" + THREAD_NUMBER.incrementAndGet()));

	static {
		ServletContextEventListener.registerOnContextDestroyedTask(servletContextEvent -> {
			LOGGER.info("Shutting down CI build executor.");
			CI_BUILD_EXECUTOR.shutdown();
		});
	}

	private static final Map<CIDashboard, CIBuildFuture> ciBuildQueue = Collections.synchronizedMap(new WeakHashMap<>());

	private static class CIBuildFuture extends FutureTask<Void> {

		private final CIBuildCallable ciBuildCallable;

		public CIBuildFuture(CIBuildCallable ciBuildCallable) {
			super(ciBuildCallable);
			this.ciBuildCallable = ciBuildCallable;
		}
	}

	private static class CIBuildCallable implements Callable<Void> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CIBuildCallable.class);

		private final CIDashboard dashboard;
		private final TestExecutor testExecutor;
		private final DefaultAjaxProgressListener listener;

		public CIBuildCallable(CIDashboard dashboard) {
			this.dashboard = dashboard;
			List<TestObjectProvider> providers = new ArrayList<>();
			providers.add(DefaultWikiTestObjectProvider.getInstance());
			List<TestObjectProvider> pluggedProviders = TestObjectProviderManager.getTestObjectProviders();
			providers.addAll(pluggedProviders);

			listener = new DefaultAjaxProgressListener();
			testExecutor = new TestExecutor(providers, dashboard.getTestSpecifications(), listener, TEST_EXECUTOR_SERVICE);
		}

		@Override
		public Void call() {
			LOGGER.info("Executing new CI build for dashboard '" + dashboard.getDashboardName() + "'");
			try {
				testExecutor.run();

				BuildResult build = testExecutor.getBuildResult();

				// add resulting build to dashboard
				if (build != null && !Thread.interrupted()) {
					// set verbose persistence flag, will be considered by persistence
					build.setVerbosePersistence(lookUpVerboseFlag(dashboard));
					dashboard.addNewBuild(build);
				}
				deleteAttachmentTempFiles(build);
			}
			catch (Exception e) {
				LOGGER.error("Exception while executing CI build", e);
			}
			finally {
				synchronized (ciBuildQueue) {
					CIBuildFuture ciBuildFuture = ciBuildQueue.get(dashboard);
					if (ciBuildFuture != null && ciBuildFuture.ciBuildCallable == this) {
						ciBuildQueue.remove(dashboard);
					}
				}
			}
			return null;
		}
	}

	/**
	 * Runs a build for the given dashboard, but does not wait until it is done. Waits until the build is registered in
	 * the manager, but does not wait until the build is done.
	 */
	public synchronized void startBuild(final CIDashboard dashboard) {
		// we synchronize on the build manager instance to exclude that new builds are added
		// while we shut down and wait for termination in notify(), because a new compilation frame
		// is opened. notify() also synchronizes on the build manager instance.

		// if there already is a running build, we terminate it
		shutDownNow(dashboard);

		CIBuildFuture ciBuildFuture = new CIBuildFuture(new CIBuildCallable(dashboard));
		ciBuildQueue.put(dashboard, ciBuildFuture);
		// the future will remove itself in method done().
		CI_BUILD_EXECUTOR.execute(ciBuildFuture);
	}

	private static void deleteAttachmentTempFiles(BuildResult build) {
		if (build == null) return;
		for (TestResult testResult : build.getResults()) {
			testResult.handleAutoDelete();
		}
	}

	private static boolean lookUpVerboseFlag(CIDashboard dashboard) {
		Section<CIDashboardType> ciDashboardSection = dashboard.getDashboardSection();
		String flagString = DefaultMarkupType.getAnnotation(ciDashboardSection,
				CIDashboardType.VERBOSE_PERSISTENCE_KEY);
		return "true".equalsIgnoreCase(flagString);
	}

	/**
	 * Terminates the build of the given dashboard (if there is one).
	 */
	public void shutDownNow(CIDashboard dashboard) {
		synchronized (ciBuildQueue) {
			CIBuildFuture ciBuildFuture = ciBuildQueue.get(dashboard);
			if (ciBuildFuture != null) {
				ciBuildFuture.ciBuildCallable.testExecutor.shutDownNow();
			}
		}
	}

	/**
	 * Terminates all currently running builds;
	 */
	public void shutDownNow() {
		synchronized (ciBuildQueue) {
			for (CIBuildFuture ciBuildFuture : ciBuildQueue.values()) {
				TestExecutor testExecutor = ciBuildFuture.ciBuildCallable.testExecutor;
				if (!testExecutor.isShutdown()) {
					testExecutor.shutDownNow();
				}
			}
		}
	}

	/**
	 * Blocks/waits until all running tests of the given dashboard are done, tests are not aborted
	 * by calling this method.
	 */
	public void awaitTermination(CIDashboard dashboard) {
		CIBuildFuture ciBuildFuture = ciBuildQueue.get(dashboard);
		if (ciBuildFuture != null) {
			try {
				ciBuildFuture.get();
			}
			catch (InterruptedException | ExecutionException e) {
				LOGGER.error("Exception while awaiting CI Build termination", e);
			}
		}
	}

	/**
	 * Blocks/waits until all running tests of all registered dashboards are done. Tests are not
	 * aborted by calling this method.
	 */
	public void awaitTermination() {
		ArrayList<CIBuildFuture> ciBuildFutures;
		synchronized (ciBuildQueue) {
			ciBuildFutures = new ArrayList<>(ciBuildQueue.values());
		}
		for (CIBuildFuture ciBuildFuture : ciBuildFutures) {
			try {
				ciBuildFuture.get();
			}
			catch (InterruptedException | ExecutionException e) {
				LOGGER.error("Exception while awaiting CI Build termination", e);
			}
		}
	}

	/**
	 * Looks up whether there is currently a build process running for this dashboard
	 *
	 * @created 16.08.2012
	 */
	public static boolean isRunning(CIDashboard dashboard) {
		return ciBuildQueue.get(dashboard) != null;
	}

	/**
	 * Provides a ProgressListener of the given dashboard, if the dashboard exists and is currently running, null
	 * otherwise.
	 *
	 * @param dashboard the dashboard to get the progress listener for
	 * @return the progress listener for the given dashboard
	 */
	@Nullable
	public static DefaultAjaxProgressListener getProgress(CIDashboard dashboard) {
		CIBuildFuture ciBuildFuture = ciBuildQueue.get(dashboard);
		if (ciBuildFuture == null) return null;
		return ciBuildFuture.ciBuildCallable.listener;
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		List<Class<? extends Event>> events = new ArrayList<>(1);
		events.add(CompilationStartEvent.class);
		return events;
	}

	@Override
	public synchronized void notify(Event event) {
		// we synchronize the method so there will not be any new builds
		// added between shutting down and awaiting termination
		shutDownNow();
//		awaitTermination();
	}
}
