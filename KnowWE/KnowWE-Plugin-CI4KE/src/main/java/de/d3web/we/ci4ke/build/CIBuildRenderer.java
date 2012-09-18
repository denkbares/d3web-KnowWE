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

import java.util.List;

import de.d3web.testing.BuildResult;
import de.d3web.testing.Message.Type;
import de.d3web.we.ci4ke.util.CIUtilities;

/**
 * 
 * @author volker_belli
 * @created 19.05.2012
 */
public class CIBuildRenderer {

	private final Dashboard dashboard;

	/**
	 * Creates a new CIBuildRenderer for the specified Dashboard.
	 * 
	 */
	public CIBuildRenderer(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	/**
	 * Calculates a "quality forecast", based on the 5 last builds
	 * 
	 * @created 27.05.2010
	 * @return
	 */
	public String renderBuildHealthReport(int pixelSize) {

		List<BuildResult> lastBuilds = dashboard.getBuildsByIndex(Integer.MAX_VALUE, 5);
		int count = lastBuilds.size();
		int failed = 0;
		for (BuildResult build : lastBuilds) {
			if (!Type.SUCCESS.equals(build.getOverallResult())) {
				failed++;
			}
		}
		return CIUtilities.renderForecastIcon(count, failed, pixelSize);
	}

	public String renderBuildList(int indexFromBack, int numberOfBuilds, int shownBuild) {
		BuildResult latestBuild = dashboard.getLatestBuild();
		int latestBuildNumber = indexFromBack;
		if (latestBuild != null) {
			latestBuildNumber = latestBuild.getBuildNumber();
		}
		if (indexFromBack == 0) indexFromBack = latestBuildNumber;

		String dashboardNameEncoded = CIUtilities.utf8Escape(dashboard.getDashboardName());

		List<BuildResult> builds = dashboard.getBuildsByIndex(indexFromBack, numberOfBuilds);

		StringBuffer sb = new StringBuffer();
		sb.append("<H4>Builds</H4>");
		sb.append("<table width=\"100%\" border='1' class=\"build-table\">");

		// reverse order to have the most current builds on top
		for (BuildResult build : builds) {
			int buildNr = build.getBuildNumber();

			// mark currently shown build number
			String cssClass = "";
			if (buildNr == shownBuild) {
				cssClass = "selectedBuildNR";
			}
			sb.append("<tr class='" + cssClass + "'><td>");
			// starting with a nice image...
			Type buildResult = build.getOverallResult();
			sb.append(CIUtilities.renderResultType(buildResult, 16, dashboard.getDashboardName()));

			sb.append("</td><td>");
			sb.append("<td>");

			// render link
			sb.append("<a href='Wiki.jsp?page=" + dashboard.getDashboardArticle()
					+ "&build_number=" + buildNr
					+ "&indexFromBack=" + indexFromBack + "#" + dashboard.getDashboardName() + "'>");

			// actual shown content:
			sb.append("#" + buildNr);
			sb.append("</a>");

			sb.append("</tr>");
		}
		sb.append("</table>");

		int latestDisplayedBuildNumber = indexFromBack;

		if (!builds.isEmpty()) {
			latestDisplayedBuildNumber = builds.get(0).getBuildNumber();
		}

		// wenn man noch weiter zurückblättern kann, rendere einen Button
		if (latestDisplayedBuildNumber - numberOfBuilds > 0) {
			String buttonLeft = "<button onclick=\"fctRefreshBuildList('"
					+ dashboardNameEncoded + "','"
					+ (latestDisplayedBuildNumber - numberOfBuilds)
					+ "','" + numberOfBuilds + "');\" style=\"margin-top: 4px; float: left;\">"
					+ "<img src=\"KnowWEExtension/ci4ke/images/16x16/left.png\" "
					+ "style=\"vertical-align: middle; margin-right: 5px;\">"
					+ "</button>";
			sb.append(buttonLeft);
		}

		// wenn man noch weiter vorblättern kann, rendere einen Button
		if (latestDisplayedBuildNumber < latestBuildNumber) {
			String buttonRight = "<button onclick=\"fctRefreshBuildList('"
					+ dashboardNameEncoded + "','"
					+ (latestDisplayedBuildNumber + numberOfBuilds)
					+ "','" + numberOfBuilds
					+ "');\" style=\"margin-top: 4px; float: right;\">"
					+ "<img src=\"KnowWEExtension/ci4ke/images/16x16/right.png\" "
					+ "style=\"vertical-align: middle; margin-left: 5px;\"></button>";
			sb.append(buttonRight);
		}

		return sb.toString();
	}

	/**
	 * Renders the current build status (status of the last build)
	 * 
	 * @created 27.05.2010
	 * @return
	 */
	public String renderCurrentBuildStatus(int pixelSize) {
		BuildResult build = dashboard.getLatestBuild();
		if (build == null) return "";
		return CIUtilities.renderHeaderResultType(build.getOverallResult(), pixelSize,
				dashboard.getDashboardName());
	}

	// ------------ RENDERING ----------------

	/**
	 * Renders out a list of the newest builds in descending order
	 */
	public String renderNewestBuilds(int numberOfBuilds, int shownBuild, int indexFromTo) {
		return renderBuildList(indexFromTo, numberOfBuilds, shownBuild);
	}
}
