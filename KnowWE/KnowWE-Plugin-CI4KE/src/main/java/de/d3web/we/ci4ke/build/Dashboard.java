/*
 * Copyright (C) 2012 denkbares GmbH, Germany
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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.testing.BuildResult;
import de.d3web.testing.Message;
import de.d3web.testing.Message.Type;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.handling.CIDashboardType;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.Strings;

/**
 * This class represents a dashboard data structure, managing the build results,
 * their persistence and provides an appropriate renderer.
 * 
 * @author volker_belli
 * @created 19.05.2012
 */
public class Dashboard {

	private final String web;
	private final String dashboardArticle;
	private final String dashboardName;
	private final CIBuildRenderer renderer;
	private final CIBuildPersistence persistence;

	/**
	 * Internal cache for already available build results. When build result is
	 * loaded it is stored in this map by its build number.
	 */
	private final Map<Integer, BuildResult> buildCache = new HashMap<Integer, BuildResult>();

	/**
	 * Stores the list of build-numbers currently available in the persistence.
	 * The build numbers are ordered naturally, so the first one is the lowest
	 * and the last one is the highest build number.
	 */
	private int[] availableBuilds = new int[0];

	private Dashboard(String web, String dashboardArticle, String dashboardName) {
		this.web = web;
		this.dashboardArticle = dashboardArticle;
		this.dashboardName = dashboardName;
		this.renderer = new CIBuildRenderer(this);
		this.persistence = new CIBuildPersistence(this);
	}

	public String getWeb() {
		return web;
	}

	public String getDashboardArticle() {
		return dashboardArticle;
	}

	public String getDashboardName() {
		return dashboardName;
	}

	public CIBuildRenderer getRenderer() {
		return renderer;
	}

	public CIBuildPersistence getPersistence() {
		return persistence;
	}

	/**
	 * Returns the latest build stored in this dashboard or null if there is no
	 * build stored.
	 * 
	 * @created 19.05.2012
	 * @return the latest build
	 */
	public BuildResult getLatestBuild() {
		update();
		int buildNumber = getLatestAvailableBuildNumber();
		if (buildNumber == 0) return null;
		return accessBuild(buildNumber);
	}

	/**
	 * Returns the number of builds currently stored in this dashboard. Please
	 * note that this number may be less than the build number of the latest
	 * build if some older builds have been deleted.
	 * 
	 * @created 19.05.2012
	 * @return the number of builds
	 */
	public int getBuildCount() {
		update();
		return availableBuilds.length;
	}

	/**
	 * Returns the build of the specified buildNumber stored in this dashboard
	 * or null if there is no build stored.
	 * 
	 * @created 19.05.2012
	 * @param buildNumber the build to be returned
	 * @return the specified build
	 */
	public BuildResult getBuild(int buildNumber) {
		update();
		if (!hasAvailableBuild(buildNumber)) return null;
		return accessBuild(buildNumber);
	}

	/**
	 * Returns an array of a number of builds before (older than) the specified
	 * buildNumber stored in this dashboard. The maximum number of builds to be
	 * accessed is specified by the count parameter. If there are less build
	 * available before the specified build number, the array contains less than
	 * "count" values. If there is no such build an empty array is returned.
	 * <p>
	 * Please note that the build number specified may not exist. The method
	 * nevertheless returns the most recent builds below that number. Especially
	 * to access the n latest builds you can use
	 * <code>getBuildsBefore(Integer.MAX_VALUE, n)</code>.
	 * 
	 * @created 19.05.2012
	 * @param buildNumber the build to be returned
	 * @return the specified build
	 */
	public BuildResult[] getBuildsBefore(int buildNumber, int count) {
		update();
		BuildResult[] result;
		while ((result = getBuildsBeforeChecked(buildNumber, count)) == null) {
		}
		return result;
	}

	/**
	 * See {@link #getBuildsBefore(int, int)}. in addition this method returns
	 * null if the cache has detected to be invalid to signal that the action
	 * should be repeated.
	 * 
	 * @created 21.05.2012
	 */
	private synchronized BuildResult[] getBuildsBeforeChecked(int buildNumber, int count) {
		// find the position of the buildNumber
		// or where it would be if exists
		int index = Arrays.binarySearch(availableBuilds, buildNumber);
		if (index < 0) index = -index - 1;

		// determine sub-array
		int from = index - count;
		if (from < 0) from = 0;

		// and access the builds
		BuildResult[] result = new BuildResult[index - from];
		for (int i = 0; i < result.length; i++) {
			result[i] = accessBuild(availableBuilds[from + i]);
		}

		// check if we have cached invalid versions
		// and try again if so
		if (cleanWrongAvailables(from, index)) {
			result = null;
		}

		return result;
	}

	/**
	 * Returns an array of builds that have the specified index in the list of
	 * versions available in this dashboard. The first build to be accessed
	 * starts at fromIndex, the latest build is before toIndex (exclusively). If
	 * the specified indexes exceed the list of history, the array contains less
	 * than "toIndex - fromIndex" values. If there is no build between the
	 * indexes, an empty array is returned.
	 * 
	 * @created 19.05.2012
	 * @param buildNumber the build to be returned
	 * @return the specified build
	 */
	public BuildResult[] getBuildsByIndex(int fromIndex, int toIndex) {
		update();
		BuildResult[] result;
		while ((result = getBuildsByIndexChecked(fromIndex, toIndex)) == null) {
		}
		return result;
	}

	/**
	 * See {@link #getBuildsByIndex(int, int)}. in addition this method returns
	 * null if the cache has detected to be invalid to signal that the action
	 * should be repeated.
	 * 
	 * @created 21.05.2012
	 */
	private synchronized BuildResult[] getBuildsByIndexChecked(int fromIndex, int toIndex) {

		// correct indexes if required
		if (toIndex > availableBuilds.length) {
			fromIndex -= (toIndex - availableBuilds.length);
			toIndex = availableBuilds.length;
		}
		if (fromIndex < 0) fromIndex = 0;

		// determine sub-array
		if (fromIndex >= toIndex) return new BuildResult[0];

		// and access the builds
		BuildResult[] result = new BuildResult[toIndex - fromIndex];
		for (int i = 0; i < result.length; i++) {
			result[i] = accessBuild(availableBuilds[fromIndex + i]);
		}

		// check if we have cached invalid versions
		// and try again if so
		if (cleanWrongAvailables(fromIndex, toIndex)) {
			result = null;
		}

		return result;
	}

	/**
	 * Adds a new build to the dashboard and makes the underlying persistence to
	 * store that build. If the build is lower or equal than the latest build
	 * number, an {@link IllegalArgumentException} is thrown.
	 * 
	 * @created 19.05.2012
	 * @param build the build to be added
	 * @throws IllegalArgumentException the build number is not higher than the
	 *         existing ones
	 */
	public synchronized void addBuild(BuildResult build) throws IllegalArgumentException {
		if(build == null) throw new IllegalArgumentException("build is null!");
		update();

		int latestAvailable = getLatestAvailableBuildNumber();
		if (latestAvailable >= build.getBuildNumber()) {
			throw new IllegalArgumentException("cannot add old builds to dashboard");
		}

		// add to cache structure
		int buildNumber = build.getBuildNumber();
		buildCache.put(buildNumber, build);

		// add to list of available builds
		int length = availableBuilds.length;
		availableBuilds = Arrays.copyOfRange(availableBuilds, 0, length + 1);
		availableBuilds[length] = buildNumber;

		// and attach to wiki if possible
		try {
			persistence.write(build);
		}
		catch (IOException e) {
			// we cannot store the build as attachment
			// so log this and continue as usual
			Logger.getLogger(getClass().getName()).log(Level.SEVERE,
					"cannot attached build information due to internal error", e);
		}
	}

	/**
	 * Gets the latest build number that is available in the current cache
	 * structure. If no build is available this method returns "0".
	 * 
	 * @created 21.05.2012
	 * @return the latest known build number
	 */
	private synchronized int getLatestAvailableBuildNumber() {
		if (availableBuilds.length == 0) return 0;
		return availableBuilds[availableBuilds.length - 1];
	}

	/**
	 * Checks the underlying wiki infrastructure for the latest build and
	 * eventually update our caches if necessary. After this method returns, you
	 * can be sure that this {@link Dashboard} is up-to-date.
	 * 
	 * @created 19.05.2012
	 */
	private synchronized void update() {
		try {
			int latest = persistence.getLatestBuildNumber();
			int lastAvailable = getLatestAvailableBuildNumber();
			if (latest > 0 && latest > lastAvailable) {
				availableBuilds = persistence.getBuildNumbers();
				cleanWrongAvailables(0, availableBuilds.length);
			}
		}
		catch (IOException e) {
			// we cannot check if we are up to date
			// so log this and continue as usual
			Logger.getLogger(getClass().getName()).log(Level.SEVERE,
					"cannot update attached build information due to internal error", e);
		}
	}

	/**
	 * Removes those available build from the caches, that have a different
	 * build number as their attachment version number. Returns true if any
	 * items have been cleaned, false otherwise.
	 * <p>
	 * The main reason why this is required if, because JSPWiki is not capable
	 * to delete the attachments yet. Therefore we might get attachments with
	 * wrong version numbers, differing from build number.
	 * 
	 * @param fromIndex the index to start the check (inclusively)
	 * @param toIndex the index to stop the check (exclusively)
	 * @return if any items have been cleaned
	 * 
	 * @created 21.05.2012
	 */
	private synchronized boolean cleanWrongAvailables(int fromIndex, int toIndex) {
		int writeIndex = fromIndex;
		for (int readIndex = fromIndex; readIndex < toIndex; readIndex++) {
			int buildNumber = availableBuilds[readIndex];
			// if we have already loaded a build that has a wrong number
			BuildResult build = buildCache.get(buildNumber);
			if (build != null && build.getBuildNumber() != buildNumber) {
				// remove the build from the cache
				buildCache.remove(buildNumber);
				// and also from the available list
				continue;
			}
			// but keep on available list otherwise
			availableBuilds[writeIndex++] = buildNumber;
		}
		// if we have removed some items
		// create a sub-array of the existing one
		int countCleaned = toIndex - writeIndex;
		if (countCleaned > 0) {
			int[] cleaned = new int[availableBuilds.length - countCleaned];
			// copy first part (before fromIndex + the part that has been
			// iterated)
			System.arraycopy(availableBuilds, 0, cleaned, 0, writeIndex);
			// copy second part (not checked)
			System.arraycopy(
					availableBuilds, toIndex, cleaned, writeIndex, availableBuilds.length - toIndex);
			availableBuilds = cleaned;
			return true;
		}
		return false;
	}

	/**
	 * Returns a build from the cache or read it from the persistence if not
	 * been accessed before. Before this method is called, you must be sure that
	 * the build number exists in the persistence. If not, call
	 * {@link #update()} and {@link #hasAvailableBuild(int)} before.
	 * 
	 * @created 19.05.2012
	 * @param buildNumber the build number to be accessed
	 * @return the build of the specified number
	 */
	private BuildResult accessBuild(int buildNumber) {
		BuildResult build = buildCache.get(buildNumber);
		if (build == null) {
			synchronized (this) {
				try {
					build = persistence.read(buildNumber);
				}
				catch (IOException e) {
					// if we cannot read the requested build
					// we create a new one signaling the error
					TestResult badResult = new TestResult(null, null);
					Message message = new Message(Type.ERROR,
							"error while loading build informtion: " + e.getMessage() +
									"\n" + Strings.stackTrace(e));
					badResult.addMessage(null, message);
					build = new BuildResult(buildNumber);
					build.addTestResult(badResult);
				}
				buildCache.put(buildNumber, build);
			}
		}
		return build;
	}

	private boolean hasAvailableBuild(int buildNumber) {
		return Arrays.binarySearch(availableBuilds, buildNumber) >= 0;
	}

	private static final Map<String, Dashboard> dashboards = new HashMap<String, Dashboard>();

	/**
	 * Get the {@link Dashboard} instance responsible for a specific
	 * dashboardName-dashboardArticle-combination. If no handler exists for this
	 * combination, a new handler is created.
	 */
	public static synchronized Dashboard getDashboard(String web, String dashboardArticleTitle, String dashboardName) {
		String key = web + "/" + dashboardArticleTitle + "/" + dashboardName;
		Dashboard dashboard = dashboards.get(key);
		if (dashboard == null) {
			dashboard = new Dashboard(web, dashboardArticleTitle, dashboardName);
			dashboards.put(key, dashboard);
		}
		return dashboard;
	}

	/**
	 * Checks if there is a {@link Dashboard} instance responsible for a
	 * specific dashboardName-dashboardArticle-combination. If no dashboard
	 * exists for this combination, false is returned.
	 * 
	 * @param dashboardArticleTitle the article where the dashboard is located
	 * @param dashboardName the name of the dashboard
	 */
	public static boolean hasDashboard(String web, String dashboardArticleTitle, String dashboardName) {
		ArticleManager articleManager = Environment.getInstance().getArticleManager(web);
		Article article = articleManager.getArticle(dashboardArticleTitle);
		if (article == null) {
			return false;
		}
		List<Section<CIDashboardType>> sections =
				Sections.findSuccessorsOfType(article.getRootSection(), CIDashboardType.class);
		for (Section<CIDashboardType> section : sections) {
			String name = CIDashboardType.getDashboardName(section);
			if (name != null && name.equalsIgnoreCase(dashboardName)) {
				return true;
			}
		}
		return false;
	}

}
