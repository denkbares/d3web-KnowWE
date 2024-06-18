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
package de.d3web.we.ci4ke.dashboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.EventManager;
import de.d3web.testing.BuildResult;
import de.d3web.testing.TestSpecification;
import de.d3web.we.ci4ke.build.CIBuildCache;
import de.d3web.we.ci4ke.build.CIPersistence;
import de.d3web.we.ci4ke.build.CIRenderer;
import de.d3web.we.ci4ke.dashboard.event.UserDefaultDashboardUpdateEvent;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.WikiAttachment;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * This class represents a dashboard data structure, managing the build results,
 * their persistence and provides an appropriate renderer.
 *
 * @author volker_belli
 * @created 19.05.2012
 */
public class CIDashboard {
	private static final Logger LOGGER = LoggerFactory.getLogger(CIDashboard.class);
	private static final String DEFAULT_DASHBOARD = "DEFAULT_DASHBOARD";

	private final String dashboardName;
	private final CIRenderer renderer;
	private final CIPersistence persistence;
	private final CIBuildCache buildCache;
	private final Section<CIDashboardType> dashboardSection;
	private final List<TestSpecification<?>> testSpecifications;
	private final double priority;

	protected CIDashboard(Section<CIDashboardType> dashboardSection, List<TestSpecification<?>> specifications) {
		this.dashboardSection = dashboardSection;
		this.dashboardName = CIDashboardType.getDashboardName(dashboardSection);
		this.testSpecifications = specifications;
		this.buildCache = new CIBuildCache();
		this.renderer = new CIRenderer(this);
		this.persistence = new CIPersistence(this, buildCache);
		this.priority = dashboardSection.get().getPriority(dashboardSection);
	}

	public Set<String> getMonitoredArticles() {
		return dashboardSection.get().getMonitoredArticles(dashboardSection);
	}

	public Section<CIDashboardType> getDashboardSection() {
		return this.dashboardSection;
	}

	public String getWeb() {
		return dashboardSection.getWeb();
	}

	public String getDashboardArticle() {
		return dashboardSection.getTitle();
	}

	public String getDashboardName() {
		return dashboardName;
	}

	public CIRenderer getRenderer() {
		return renderer;
	}

	public int getLatestBuildNumber() {
		int buildNumber = 0;
		BuildResult latestBuild = getLatestBuild();
		if (latestBuild != null) {
			return latestBuild.getBuildNumber();
		}
		return buildNumber;
	}

	/**
	 * Returns the latest build stored in this dashboard or null if there is no
	 * build stored.
	 *
	 * @return the latest build
	 * @created 19.05.2012
	 */
	@Nullable
	public BuildResult getLatestBuild() {
		return getBuildIfPossible(-1, true);
	}

	/**
	 * Returns the wiki attachment that stores the results of this CIDashboard.
	 * The method returns null if the attachment does not exist (yet), e.g. if
	 * no build has been created/written yet.
	 *
	 * @return the attachment storing the results
	 * @throws IOException if the attachment cannot be accessed, should usually
	 *                     not happen
	 * @created 04.10.2013
	 */
	@Nullable
	public WikiAttachment getBuildAttachment() throws IOException {
		return persistence.getAttachment();
	}

	/**
	 * Returns the build of the specified buildNumber stored in this dashboard
	 * or null if this build does not exist.
	 *
	 * @param buildNumber the build to be returned, you can use -1 to get the
	 *                    latest build
	 * @return the specified build
	 * @created 19.05.2012
	 */
	@Nullable
	public BuildResult getBuild(int buildNumber) {
		return getBuildIfPossible(buildNumber, true);
	}

	/**
	 * Returns an array of builds that have the specified index in the list of
	 * versions available in this dashboard. The first build to be accessed
	 * starts at fromIndex, the latest build is before toIndex (exclusively). If
	 * the specified indexes exceed the list of history, the array contains less
	 * than "toIndex - fromIndex" values. If there is no build between the
	 * indexes, an empty array is returned.
	 *
	 * @param fromIndex      the newest build to be returned, you can use -1 to
	 *                       start with the latest build
	 * @param numberOfBuilds the amount of builds you want to get (the next
	 *                       older ones)
	 * @return the specified builds
	 * @created 19.05.2012
	 */
	@NotNull
	public List<BuildResult> getBuilds(int fromIndex, int numberOfBuilds) {
		fromIndex = cap(fromIndex);
		List<BuildResult> results = new ArrayList<>(numberOfBuilds);
		int index = fromIndex;
		while (results.size() < numberOfBuilds && index > 0) {
			BuildResult build = getBuildIfPossible(index, false);
			if (build != null && build.getBuildNumber() <= fromIndex) {
				results.add(build);
			}
			index--;
		}
		return results;
	}

	/**
	 * If the buildNumber is to high or low, the highest available buildNumber
	 * is returned;
	 *
	 * @return the highest available build number if the given is higher that
	 * that
	 * @created 18.09.2012
	 */
	private int cap(int buildIndex) {
		int latestBuildNumber = getLatestBuildNumber();
		if (latestBuildNumber < buildIndex) return latestBuildNumber;
		if (buildIndex < 1) return latestBuildNumber;
		return buildIndex;
	}

	/**
	 * Adds a new build to the dashboard and makes the underlying persistence to
	 * store that build. The given build will always be considered as the latest
	 * build
	 *
	 * @param build the build to be added
	 * @created 19.05.2012
	 */
	public synchronized void addNewBuild(BuildResult build) {
		if (build == null) throw new IllegalArgumentException("build is null!");

		// also store here for quick access and elements not covered by the
		// persistence
		int buildNumber = getLatestBuildNumber() + 1;
		build.setBuildNumber(buildNumber);
		// the latest build is either from persistence and has the version of
		// the attachment as the build number or it is already set from previous
		// calls to this method

		buildCache.setLatestBuild(build);
		buildCache.addBuild(build);

		try {
			persistence.write(build);
		}
		catch (IOException e) {
			// we cannot store the build as attachment
			// so log this and continue as usual
			LOGGER.error("Cannot attached build information due to internal error", e);
		}
	}

	private BuildResult getBuildIfPossible(final int buildVersion, boolean logging) {
		BuildResult build;
		build = buildCache.getBuild(buildVersion);
		if (build != null) return build;
		try {
			build = persistence.read(buildVersion);
		}
		catch (Exception e) {
			if (logging) {
				LOGGER.warn("Unable to access build " + buildVersion);
			}
		}
		if (build != null) {
			if (buildVersion < 1) buildCache.setLatestBuild(build);
			buildCache.addBuild(build);
		}
		return build;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " (" + dashboardName + ")";
	}

	public List<TestSpecification<?>> getTestSpecifications() {
		return this.testSpecifications;
	}

	public double getPriority() {
		return this.priority;
	}

	public void updateDefaultDashboardOfUser(UserContext user) {
		for (String monitoredArticle : getMonitoredArticles()) {
			for (Section<PackageCompileType> compileSection : $(user.getArticleManager()
					.getArticle(monitoredArticle)).successor(PackageCompileType.class)) {
				PackageCompiler compiler = Compilers.getCompiler(compileSection, PackageCompiler.class);
				Set<String> defaultDashboards = getDefaultDashboards(user);
				Set<String> before = Set.copyOf(defaultDashboards);
				if (compiler != null && Compilers.isDefaultCompiler(user, compiler)) {
					defaultDashboards.add(getDashboardName());
				}
				else {
					defaultDashboards.remove(getDashboardName());
				}
				if (!before.equals(defaultDashboards)) {
					EventManager.getInstance().fireEvent(new UserDefaultDashboardUpdateEvent(Set.copyOf(defaultDashboards), user.getUserName()));
				}
			}
		}
	}

	public boolean isDefaultDashboardOfUser(UserContext userContext) {
		return getDefaultDashboards(userContext).contains(getDashboardName());
	}

	@NotNull
	private static Set<String> getDefaultDashboards(UserContext user) {
		//noinspection unchecked
		Set<String> defaultDashboards = (Set<String>) user.getSession().getAttribute(DEFAULT_DASHBOARD);
		if (defaultDashboards == null) {
			defaultDashboards = Collections.newSetFromMap(new ConcurrentHashMap<>());
		}
		user.getSession().setAttribute(DEFAULT_DASHBOARD, defaultDashboards);
		return defaultDashboards;
	}
}
