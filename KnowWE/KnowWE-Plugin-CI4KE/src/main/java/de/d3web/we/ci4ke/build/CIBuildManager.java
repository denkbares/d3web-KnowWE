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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import de.d3web.testing.BuildResult;
import de.d3web.testing.TestExecutor;
import de.d3web.testing.TestObjectProvider;
import de.d3web.testing.TestObjectProviderManager;
import de.d3web.testing.TestResult;
import de.d3web.utils.Log;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.compile.CompilationStartEvent;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.progress.AjaxProgressListener;
import de.knowwe.core.utils.progress.DefaultAjaxProgressListener;
import de.knowwe.core.utils.progress.ProgressListenerManager;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CIBuildManager implements EventListener {

	private static CIBuildManager instance = null;

	public static CIBuildManager getInstance() {
		if (instance == null) instance = new CIBuildManager();
		return instance;
	}

	private CIBuildManager() {
		EventManager.getInstance().registerListener(this);
	}

	private static final ExecutorService ciBuildExecutor = Executors.newFixedThreadPool(2);

	private static final Map<CIDashboard, CIBuildFuture> ciBuildQueue = new ConcurrentHashMap<>();

	private static class CIBuildFuture extends FutureTask<Void> {

		private final CIBuildCallable ciBuildCallable;

		public CIBuildFuture(CIBuildCallable ciBuildCallable) {
			super(ciBuildCallable);
			this.ciBuildCallable = ciBuildCallable;
		}

	}

	private static class CIBuildCallable implements Callable<Void> {

		private CIDashboard dashboard;
		private TestExecutor testExecutor;
		AjaxProgressListener listener;

		public CIBuildCallable(CIDashboard dashboard) {
			this.dashboard = dashboard;
			List<TestObjectProvider> providers = new ArrayList<>();
			providers.add(DefaultWikiTestObjectProvider.getInstance());
			List<TestObjectProvider> pluggedProviders = TestObjectProviderManager.getTestObjectProviders();
			providers.addAll(pluggedProviders);

			listener = new DefaultAjaxProgressListener(CIBuildManager.class.getSimpleName());
			testExecutor = new TestExecutor(providers, dashboard.getTestSpecifications(), listener);
		}

		@Override
		public Void call() throws Exception {
			Log.info("Executing new CI build for dashboard '" + dashboard.getDashboardName() + "'");
			ProgressListenerManager.getInstance().setProgressListener(Integer.toString(dashboard.hashCode()), listener);
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
			finally {
				ciBuildQueue.remove(dashboard);
				ProgressListenerManager.getInstance().removeProgressListener(
						Integer.toString(dashboard.hashCode()));
			}
			return null;
		}
	}

	/**
	 * Runs a build for the given dashboard, but does not . Waits until the build is registered in
	 * the manager, but does not wait until the build is done.
	 */
	public synchronized void startBuild(final CIDashboard dashboard) {
		// we synchronize on the build manager instance to exclude that new builds are added
		// while we shut down and wait for termination in notify(), because a new compilation frame
		// is opened. notify() also synchronizes on the build manager instance.

		synchronized (ciBuildQueue) {
			// if there already is a running build, we terminate it
			shutDownNow(dashboard);

			CIBuildFuture ciBuildFuture = new CIBuildFuture(new CIBuildCallable(dashboard));
			ciBuildQueue.put(dashboard, ciBuildFuture);
			// the future will remove itself in method done().
			ciBuildExecutor.execute(ciBuildFuture);
		}

	}

	private static void deleteAttachmentTempFiles(BuildResult build) {
		for (TestResult testResult : build.getResults()) {
			testResult.handleAutoDelete();
		}
	}

	private static boolean lookUpVerboseFlag(CIDashboard dashboard) {
		Section<CIDashboardType> ciDashboardSection = dashboard.getDashboardSection();
		String flagString = DefaultMarkupType.getAnnotation(ciDashboardSection,
				CIDashboardType.VERBOSE_PERSISTENCE_KEY);
		return flagString != null && flagString.equalsIgnoreCase("true");
	}

	/**
	 * Terminates the build of the given dashboard (if there is one).
	 */
	public void shutDownNow(CIDashboard dashboard) {
		CIBuildFuture ciBuildFuture = ciBuildQueue.get(dashboard);
		if (ciBuildFuture != null) {
			ciBuildFuture.ciBuildCallable.testExecutor.shutDownNow();
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
				Log.severe("Exception while awaiting CI Build termination", e);
			}
		}
	}

	/**
	 * Blocks/waits until all running tests of all registered dashboards are done. Tests are not
	 * aborted by calling this method.
	 */
	public void awaitTermination() {
		synchronized (ciBuildQueue) {
			for (CIBuildFuture ciBuildFuture : ciBuildQueue.values()) {
				try {
					ciBuildFuture.get();
				}
				catch (InterruptedException | ExecutionException e) {
					Log.severe("Exception while awaiting CI Build termination", e);
				}
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
		awaitTermination();
	}

}
