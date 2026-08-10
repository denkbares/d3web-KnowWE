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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
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
import de.d3web.we.ci4ke.hook.CIHookManager;
import de.knowwe.core.ServletContextEventListener;
import de.knowwe.core.compile.CompilationStartEvent;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.progress.DefaultAjaxProgressListener;
import de.knowwe.event.WikiContentReplacedEvent;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Coordinates the lifecycle of CI builds and their test executors.
 * <p>
 * A newly requested build immediately replaces the current queue entry for its dashboard, but it is not submitted to
 * the build executor until the preceding build has actually terminated. Waiting happens asynchronously on the trigger
 * executor, so callers and compilation threads only register or abort builds and never wait for test execution.
 * Before a waiting successor is submitted, the manager atomically verifies that it is still the current queue entry
 * and has not been aborted by another compilation. Obsolete successors are cancelled and removed from the queue.
 */
public class CIBuildManager implements EventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(CIBuildManager.class);

	private static CIBuildManager instance = null;

	private static final AtomicLong executorNumber = new AtomicLong();
	private static final ExecutorService CI_BUILD_EXECUTOR = Executors.newCachedThreadPool(
			r -> new Thread(r, "CI-Build-Executor-" + executorNumber.incrementAndGet()));
	private static final ExecutorService CI_BUILD_TRIGGER = Executors.newCachedThreadPool(
			r -> new Thread(r, "CI-Build-Trigger-" + executorNumber.incrementAndGet()));
	private static final AtomicLong THREAD_NUMBER = new AtomicLong();
	private static final ExecutorService TEST_EXECUTOR_SERVICE = createTestExecutorService();
	private static final ExecutorService SUB_TEST_EXECUTOR_SERVICE = createTestExecutorService();
	private static final Map<CIDashboard, CIBuildFuture> CI_BUILD_QUEUE =
			Collections.synchronizedMap(new WeakHashMap<>());

	private final ExecutorService ciBuildExecutor;
	private final ExecutorService ciBuildTrigger;
	private final Map<CIDashboard, CIBuildFuture> ciBuildQueue;
	private final Map<String, Double> priorityOverride = new ConcurrentHashMap<>();

	public static CIBuildManager getInstance() {
		if (instance == null) instance = new CIBuildManager();
		return instance;
	}

	private CIBuildManager() {
		this(CI_BUILD_EXECUTOR, CI_BUILD_TRIGGER, CI_BUILD_QUEUE);
		EventManager.getInstance().registerListener(this);
	}

	/**
	 * Creates an isolated build manager whose executors can be controlled by a test. Unlike the production singleton,
	 * this instance uses a private queue and is not registered as an event listener.
	 *
	 * @param ciBuildExecutor executor receiving builds that are ready to run
	 * @param ciBuildTrigger  executor performing asynchronous ordering and predecessor waits
	 */
	CIBuildManager(ExecutorService ciBuildExecutor, ExecutorService ciBuildTrigger) {
		this(ciBuildExecutor, ciBuildTrigger, Collections.synchronizedMap(new WeakHashMap<>()));
	}

	private CIBuildManager(
			ExecutorService ciBuildExecutor,
			ExecutorService ciBuildTrigger,
			Map<CIDashboard, CIBuildFuture> ciBuildQueue) {
		this.ciBuildExecutor = ciBuildExecutor;
		this.ciBuildTrigger = ciBuildTrigger;
		this.ciBuildQueue = ciBuildQueue;
	}

	@NotNull
	private static ExecutorService createTestExecutorService() {
		int threadCount = (int) Math.max(2, Runtime.getRuntime().availableProcessors() * 0.75);
		return new ThreadPoolExecutor(threadCount, threadCount,
				0L, TimeUnit.MILLISECONDS,
				new PriorityBlockingQueue<>(),
				r -> new Thread(r, "CI-Test-Executor-" + THREAD_NUMBER.incrementAndGet()));
	}

	static {
		ServletContextEventListener.registerOnContextDestroyedTask(servletContextEvent -> {
			LOGGER.info("Shutting down CI build executor.");
			CI_BUILD_EXECUTOR.shutdown();
			CI_BUILD_TRIGGER.shutdown();
			TEST_EXECUTOR_SERVICE.shutdown();
			SUB_TEST_EXECUTOR_SERVICE.shutdown();
		});
	}

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
		private final Map<CIDashboard, CIBuildFuture> ciBuildQueue;
		private final CIBuildProgress progress;

		public CIBuildCallable(CIDashboard dashboard, Map<CIDashboard, CIBuildFuture> ciBuildQueue) {
			this.dashboard = dashboard;
			this.ciBuildQueue = ciBuildQueue;
			List<TestObjectProvider> providers = new ArrayList<>();
			providers.add(DefaultWikiTestObjectProvider.getInstance());
			List<TestObjectProvider> pluggedProviders = TestObjectProviderManager.getTestObjectProviders();
			providers.addAll(pluggedProviders);

			progress = new CIBuildProgress();
			testExecutor = new TestExecutor(providers, dashboard.getTestSpecifications(), progress.getListener(),
					TEST_EXECUTOR_SERVICE, SUB_TEST_EXECUTOR_SERVICE, dashboard.getPriority());
		}

		@Override
		public Void call() {
			progress.markStarted();
			LOGGER.info("Executing new CI build for dashboard '" + dashboard.getDashboardName() + "'");
			try {
				testExecutor.run();

				BuildResult build = testExecutor.getBuildResult();
				CIBuildFrozenTestAdjuster.adjustFrozenTests(build, dashboard);

				// fire event to inform listeners about new result
				CIBuildResultEvent event = new CIBuildResultEvent(dashboard, build);
				EventManager.getInstance().fireEvent(event);

				// add resulting build to dashboard
				if (build != null && !testExecutor.isAborted()) {
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
	 * Registers builds for the specified dashboards and schedules them in descending priority order without waiting on
	 * the calling thread. Builds of one priority group may run in parallel; the next group is considered only after all
	 * builds of the previous group have terminated. If a dashboard already has a registered build, it is aborted and
	 * its replacement waits asynchronously for that predecessor before being submitted.
	 *
	 * @param dashboardsToTrigger dashboards for which new builds should be registered
	 */
	public synchronized void startBuilds(Set<CIDashboard> dashboardsToTrigger) {
		TreeMap<Double, Set<CIDashboard>> dashboardsByPriority = dashboardsToTrigger.stream()
				.collect(Collectors.groupingBy(CIDashboard::getPriority, TreeMap::new, Collectors.toSet()));
		TreeMap<Double, Set<CIBuildFuture>> futuresByPriority = new TreeMap<>();
		Map<CIBuildFuture, CIBuildFuture> precedingBuilds = new HashMap<>();
		synchronized (ciBuildQueue) {
			for (Set<CIDashboard> dashboards : dashboardsByPriority.descendingMap().values()) {
				for (CIDashboard dashboard : dashboards) {
					CIBuildFuture precedingBuild = ciBuildQueue.get(dashboard);
					shutDownNow(dashboard);
					CIBuildFuture ciBuildFuture = new CIBuildFuture(new CIBuildCallable(dashboard, ciBuildQueue));
					ciBuildQueue.put(dashboard, ciBuildFuture);
					if (precedingBuild != null) {
						precedingBuilds.put(ciBuildFuture, precedingBuild);
					}
					double priority = priorityOverride.getOrDefault(dashboard.getDashboardName(), dashboard.getPriority());
					futuresByPriority.computeIfAbsent(priority, k -> new HashSet<>()).add(ciBuildFuture);
				}
			}
		}
		priorityOverride.clear();
		ciBuildTrigger.submit(() -> {
			Set<CIBuildFuture> runningFutures = new HashSet<>();
			for (Set<CIBuildFuture> futures : futuresByPriority.descendingMap().values()) {
				for (CIBuildFuture runningFuture : runningFutures) {
					AsyncBuildScheduler.awaitTermination(runningFuture);
				}
				runningFutures.clear();
				for (CIBuildFuture future : futures) {
					CIBuildFuture precedingBuild = precedingBuilds.get(future);
					scheduleAfterTermination(future, precedingBuild);
				}
				runningFutures.addAll(futures);
			}
		});
	}

	/**
	 * Registers a build for the given dashboard and returns without waiting for test execution. If a previous build is
	 * registered for the dashboard, it is aborted and retained as the predecessor of the new build. The new build is
	 * submitted asynchronously only after that predecessor has actually terminated.
	 *
	 * @param dashboard dashboard for which a new build should be registered
	 */
	public synchronized void startBuild(final CIDashboard dashboard) {
		// if there already is a running build, we terminate it
		CIBuildFuture precedingBuild = ciBuildQueue.get(dashboard);
		shutDownNow(dashboard);

		CIBuildFuture ciBuildFuture = new CIBuildFuture(new CIBuildCallable(dashboard, ciBuildQueue));
		ciBuildQueue.put(dashboard, ciBuildFuture);
		scheduleAfterTermination(ciBuildFuture, precedingBuild);
	}

	/**
	 * Schedules the predecessor wait on the trigger executor. The successor remains registered while waiting, which
	 * allows a later compilation to abort or supersede it before it reaches the build executor.
	 *
	 * @param ciBuildFuture  successor to submit after the predecessor terminates
	 * @param precedingBuild previous build of the same dashboard, or {@code null} if there is none
	 */
	private void scheduleAfterTermination(CIBuildFuture ciBuildFuture, @Nullable CIBuildFuture precedingBuild) {
		AsyncBuildScheduler.schedule(
				ciBuildTrigger,
				precedingBuild,
				() -> submitIfCurrentAndActive(ciBuildFuture),
				() -> discardPendingBuild(ciBuildFuture));
	}

	/**
	 * Atomically verifies and submits a successor after its predecessor has terminated. Holding the queue lock across
	 * validation and submission prevents another lifecycle operation from replacing or aborting the successor between
	 * both operations.
	 *
	 * @param ciBuildFuture successor whose current state should be validated
	 * @return {@code true} if the successor was submitted, {@code false} if it became obsolete or was aborted
	 */
	private boolean submitIfCurrentAndActive(CIBuildFuture ciBuildFuture) {
		synchronized (ciBuildQueue) {
			CIDashboard dashboard = ciBuildFuture.ciBuildCallable.dashboard;
			if (ciBuildQueue.get(dashboard) != ciBuildFuture
					|| ciBuildFuture.ciBuildCallable.testExecutor.isAborted()) {
				return false;
			}
			ciBuildExecutor.execute(ciBuildFuture);
			return true;
		}
	}

	/**
	 * Cancels a successor that must no longer start and removes it if it is still the dashboard's current queue entry.
	 *
	 * @param ciBuildFuture pending successor to discard
	 */
	private void discardPendingBuild(CIBuildFuture ciBuildFuture) {
		ciBuildFuture.cancel(false);
		synchronized (ciBuildQueue) {
			CIDashboard dashboard = ciBuildFuture.ciBuildCallable.dashboard;
			if (ciBuildQueue.get(dashboard) == ciBuildFuture) {
				ciBuildQueue.remove(dashboard);
			}
		}
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
	 * Requests immediate shutdown of the build registered for the given dashboard, if there is one. This method does
	 * not wait for already running tests to terminate. Tests that disallow interruption may therefore continue until
	 * their natural end. A successor will still wait for the enclosing build future to terminate before it can start.
	 *
	 * @param dashboard dashboard whose registered build should be aborted
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
	 * Requests immediate shutdown of all registered builds without waiting for their execution to terminate. Pending
	 * builds remain registered long enough for the asynchronous scheduling gate to discard them safely.
	 *
	 * @return dashboards for which a shutdown was newly requested
	 */
	public @NotNull Set<CIDashboard> shutDownNow() {
		synchronized (ciBuildQueue) {
			Set<CIDashboard> stoppedDashboards = new HashSet<>();
			for (Map.Entry<CIDashboard, CIBuildFuture> entry : ciBuildQueue.entrySet()) {
				CIBuildFuture ciBuildFuture = entry.getValue();
				TestExecutor testExecutor = ciBuildFuture.ciBuildCallable.testExecutor;
				if (!testExecutor.isShutdown()) {
					testExecutor.shutDownNow();
					stoppedDashboards.add(entry.getKey());
				}
			}
			return stoppedDashboards;
		}
	}

	/**
	 * Waits for the currently registered build of the given dashboard to terminate. If that build is an asynchronously
	 * waiting successor, this also includes its predecessor wait. Calling this method does not abort tests.
	 *
	 * @param dashboard dashboard whose registered build should be awaited
	 */
	public void awaitTermination(CIDashboard dashboard) {
		CIBuildFuture ciBuildFuture = ciBuildQueue.get(dashboard);
		if (ciBuildFuture != null) {
			try {
				ciBuildFuture.get();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.error("Interrupted while awaiting CI Build termination", e);
			}
			catch (CancellationException e) {
				// A cancelled build is already terminated from the caller's perspective.
			}
			catch (ExecutionException e) {
				LOGGER.error("Exception while awaiting CI Build termination", e);
			}
		}
	}

	/**
	 * Waits for the builds currently registered for all dashboards to terminate. Asynchronously waiting successors are
	 * included. Calling this method does not abort tests. If the waiting thread is interrupted, its interrupt status is
	 * restored and no further builds are awaited.
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
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.error("Interrupted while awaiting CI Build termination", e);
				return;
			}
			catch (CancellationException e) {
				// A cancelled build is already terminated from the caller's perspective.
			}
			catch (ExecutionException e) {
				LOGGER.error("Exception while awaiting CI Build termination", e);
			}
		}
	}

	/**
	 * Looks up whether a build is registered for this dashboard. A registered build may still be waiting asynchronously
	 * for its predecessor and need not yet be executing.
	 *
	 * @param dashboard dashboard whose build registration should be checked
	 * @return {@code true} if a pending, waiting, or executing build is registered
	 * @created 16.08.2012
	 */
	public static boolean isRunning(CIDashboard dashboard) {
		return CI_BUILD_QUEUE.get(dashboard) != null;
	}

	/**
	 * Provides the progress listener of the build registered for the given dashboard. For a successor still waiting for
	 * its predecessor, the listener exists even though test execution has not started yet.
	 *
	 * @param dashboard the dashboard to get the progress listener for
	 * @return the progress listener for the given dashboard
	 */
	@Nullable
	public static DefaultAjaxProgressListener getProgress(CIDashboard dashboard) {
		CIBuildFuture ciBuildFuture = CI_BUILD_QUEUE.get(dashboard);
		if (ciBuildFuture == null) return null;
		return ciBuildFuture.ciBuildCallable.progress.getListener();
	}

	/**
	 * Returns an immutable snapshot of the currently queued or running build for the supplied dashboard.
	 *
	 * @param dashboard the dashboard whose current build status is requested
	 * @return the current status, or {@code null} if no build is queued or running
	 */
	@Nullable
	public static CIBuildStatus getBuildStatus(CIDashboard dashboard) {
		CIBuildFuture ciBuildFuture = CI_BUILD_QUEUE.get(dashboard);
		if (ciBuildFuture == null) return null;
		return ciBuildFuture.ciBuildCallable.progress.getStatus();
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		List<Class<? extends Event>> events = new ArrayList<>(1);
		events.add(CompilationStartEvent.class);
		events.add(CIDashboardPriorityOverrideEvent.class);
		events.add(WikiContentReplacedEvent.class);
		return events;
	}

	/**
	 * Handles build lifecycle events. A compilation start only requests shutdown and registers affected dashboards for
	 * the next trigger; it never waits on the compilation thread. When the next trigger starts a replacement, the
	 * normal predecessor handover ensures that it cannot overlap the terminating build.
	 *
	 * @param event lifecycle event to handle
	 */
	@Override
	public synchronized void notify(Event event) {
		// Exclude new build registration while running builds are marked for asynchronous shutdown/restart.
		if (event instanceof CompilationStartEvent) {
			Set<CIDashboard> ciDashboards = shutDownNow();
			// restart them with next trigger
			CIHookManager.getInstance().restartWithNextTrigger(ciDashboards);
		}
		else if (event instanceof CIDashboardPriorityOverrideEvent prioEvent) {
			priorityOverride.put(prioEvent.getDashboard().getDashboardName(), prioEvent.getPriority());
		}
		else if (event instanceof WikiContentReplacedEvent) {
			// wiki content has been replaced; re-initialization with compile follows; hence we can shut down
			shutDownNow();
		}
	}
}
