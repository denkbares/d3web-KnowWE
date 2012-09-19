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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.d3web.testing.BuildResult;
import de.d3web.testing.Message.Type;
import de.d3web.testing.Test;
import de.d3web.testing.TestManager;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * 
 * @author volker_belli
 * @created 19.05.2012
 */
public class CIBuildRenderer {

	private final CIDashboard dashboard;

	/**
	 * Creates a new CIBuildRenderer for the specified Dashboard.
	 * 
	 */
	public CIBuildRenderer(CIDashboard dashboard) {
		this.dashboard = dashboard;
	}

	/**
	 * Calculates a "quality forecast", based on the 5 last builds
	 * 
	 * @created 27.05.2010
	 * @return
	 */
	public String renderBuildHealthReport(int pixelSize) {

		List<BuildResult> lastBuilds = dashboard.getBuildsByIndex(
				dashboard.getPersistence().getLatestBuildVersion(), 5);
		int count = lastBuilds.size();
		int failed = 0;
		for (BuildResult build : lastBuilds) {
			if (!Type.SUCCESS.equals(build.getOverallResult())) {
				failed++;
			}
		}
		return renderForecastIcon(count, failed, pixelSize);
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
			sb.append(renderResultType(buildResult, 16, dashboard.getDashboardName()));

			sb.append("</td><td>");
			sb.append("<td>");

			// render link
			// sb.append("<a href='Wiki.jsp?page=" +
			// dashboard.getDashboardArticle()
			// + "&build_number=" + buildNr
			// + "&indexFromBack=" + indexFromBack + "#" +
			// dashboard.getDashboardName() + "'>");
			sb.append("<a onclick=\"fctGetBuildDetails('"
					+ dashboardNameEncoded + "','"
					+ buildNr + "','" + indexFromBack + "');\">");

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
		return renderHeaderResultType(build.getOverallResult(), pixelSize,
				dashboard.getDashboardName());
	}

	// ------------ RENDERING ----------------

	/**
	 * Renders out a list of the newest builds in descending order
	 */
	public String renderNewestBuilds(int numberOfBuilds, int shownBuild, int indexFromTo) {
		return renderBuildList(indexFromTo, numberOfBuilds, shownBuild);
	}

	/**
	 * Renders out the test results of a selected Build
	 */
	public String renderBuildDetails(BuildResult build) {

		String dashboardNameEscaped = CIUtilities.utf8Escape(dashboard.getDashboardName());

		StringBuffer buffy = new StringBuffer();

		buffy.append("<div id='" + dashboardNameEscaped
				+ "-column-middle' class='ci-column-middle'>");

		if (build != null) {
			apppendBuildHeadline(build, buffy);

			// sorting results for stable rendering
			List<TestResult> results = build.getResults();
			List<TestResult> resultsSorted = new ArrayList<TestResult>();
			resultsSorted.addAll(results);
			Collections.sort(resultsSorted);

			for (TestResult result : resultsSorted) {
				appendTestResult(buffy, result);
			}
		}
		else {
			buffy.append("<div class='ci-no-details'>No build selected.</div>");
		}
		buffy.append("</div>\n");
		return buffy.toString();
	}

	private void appendTestResult(StringBuffer buffy, TestResult result) {
		buffy.append("<div class='ci-collapsible-box'>");

		// prepare some information
		String name = result.getTestName();
		Type buildResult = result.getType();
		String messageText = generateMessageText(result);

		String[] config = result.getConfiguration();

		// render bullet
		buffy.append(renderResultType(buildResult, 16,
				dashboard.getDashboardName()));

		Test<?> test = TestManager.findTest(name);
		String title = "";
		if (test != null) {
			title = test.getDescription();
		}

		// render test-name
		buffy.append("<span class='ci-test-title' title='" + title + "'>");
		buffy.append(name);

		// render test-configuration (if existent)
		if (config != null && !(config.length == 0)) {
			String configString = "";
			for (String string : config) {
				configString += "\"" + string + "\"; ";
			}
			// cut off last semicolon
			if (configString.trim().endsWith(";")) {
				configString = configString.substring(0, configString.lastIndexOf(";"));
			}
			buffy.append("<span class='ci-configuration'>");
			buffy.append(" (").append(configString).append(")");
			buffy.append("</span>");
		}
		buffy.append("</span>");

		// render test-message (if exists)
		if (!messageText.isEmpty()) {
			buffy.append("<div class='ci-message'>");
			buffy.append(messageText);
			buffy.append("</div>");
		}

		buffy.append("</div>\n");
	}

	private String generateMessageText(TestResult result) {
		StringBuilder messageText = new StringBuilder();
		Collection<String> testObjectNames = result.getTestObjectNames();
		int successes = 0;
		for (String testObject : testObjectNames) {
			de.d3web.testing.Message m = result.getMessage(testObject);
			Type messageType = m.getType();
			if (messageType.equals(Type.SUCCESS)) {
				successes++;
			}
			else {
				String text = m.getText();
				if (text == null) {
					text = "";
				}
				messageText.append(messageType.toString() + ": " + text + " (test object: "
						+ testObject + ")\n");
			}
		}

		messageText.append(successes + " test objects tested successfully\n");
		return messageText.toString();
	}

	private void apppendBuildHeadline(BuildResult build, StringBuffer buffy) {
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		String buildDate = dateFormat.format(build.getBuildDate());
		buffy.append("<H4>Build #").append(build.getBuildNumber())
				.append(" (").append(buildDate).append(") ");

		// get the build duration time
		buffy.append(" in ");
		long duration = build.getBuildDuration();
		if (duration < 1000) {
			buffy.append(duration + " msec.");
		}
		else if (duration >= 1000 && duration < 60000) {
			buffy.append((duration / 1000) + " sec.");
		}
		else {
			long sec = duration / 1000;
			buffy.append(String.format("%d:%02d min.", sec / 60, sec % 60));
		}

		buffy.append("</H4>");
	}

	// RENDER - HELPERS

	public String renderResultType(Type resultType, int pixelSize, String dashboardName) {

		String imageURL = "KnowWEExtension/ci4ke/images/"
				+
				pixelSize + "x" + pixelSize
				+ "/%s.png";

		String imgBulb = "<img class='cideamon_state' width='" + pixelSize
				+ "'id='state_" + dashboardName
				+ "' " +
				"dashboardName='" + dashboardName + "' src='" + imageURL
				+ "' alt='%<s' align='absmiddle' title='%s'>";

		switch (resultType) {
		case SUCCESS:
			imgBulb = String.format(imgBulb, "green", "Build successful!");
		case FAILURE:
			imgBulb = String.format(imgBulb, "red", "Build failed!");
		case ERROR:
			imgBulb = String.format(imgBulb, "grey", "Build has errors!");
		}

		return imgBulb;
	}

	public String renderHeaderResultType(Type resultType, int pixelSize, String dashboardName) {

		if (CIUtilities.buildRunning(dashboardName)) {
			// if currently a build is running show animated icon
			String imageURL = "KnowWEExtension/images/ajax-loader16.gif";
			String imgBulb = "<img class='cideamon_state' running='true' width='"
					+ pixelSize + "'id='state_"
					+ dashboardName
					+ "' " +
					"dashboardName='" + dashboardName + "' src='" + imageURL
					+ "' alt='%<s' align='absmiddle' title='%s'>";
			return imgBulb;
		}

		return renderResultType(resultType, pixelSize, dashboardName);
	}

	public String renderDashboardHeader(BuildResult latestBuild) {
		StringBuilder string = new StringBuilder();
		String dashboardName = dashboard.getDashboardName();

		string.append("<span class='header'>");

		if (latestBuild != null || CIUtilities.buildRunning(dashboardName)) {
			CIBuildRenderer renderer = dashboard.getRenderer();
			string.append(renderer.renderCurrentBuildStatus(22)).append("  ");
			string.append(renderer.renderBuildHealthReport(22)).append("  ");
		}
		string.append(dashboardName);

		// insert tag for progress bar
		// open/close div progress_container
		if (CIUtilities.buildRunning(dashboardName)) {
			renderProgressInfoHTML(string, dashboardName);
		}
		string.append("</span>");
		return string.toString();
	}

	public void renderProgressInfoHTML(StringBuilder string, String dashboardName) {
		string.append("<div class='progressInfo' dashboardname='" + dashboardName
				+ "' dashboardarticle='" + dashboard.getDashboardArticle()
				+ "' onload=\"refreshBuildProgress('"
				+ dashboardName
				+ "', '" + dashboard.getDashboardArticle()
				+ "');\" id='progress_container' style='display:inline;'>");

		string.append("<div style='display:inline;'> <a href=\"javascript:stopRunningBuild('"
				+ dashboardName
				+ "', '"
				+ dashboard.getDashboardArticle()
				+ "', '"
				+ KnowWEUtils.getURLLink(dashboard.getDashboardArticle() + "#" + dashboardName)
				+ "');undefined;\"><img height=\"14\" title=\"Stops the current build\" src=\"KnowWEExtension/images/cross.png\"></img></a></div>     <div style='display:inline' class='prog-meter-wrap' ><div class='prog-meter-value' id='progress_value'>&nbsp;0 %</div>  </div><div class='prog-meter-text' style='display:inline' id='progress_text'>build running...</div>");
		string.append("</div>");
	}

	public String renderForecastIcon(int buildCount, int failedCount, int pixelSize) {

		int score = (buildCount > 0) ? score = (100 * (buildCount - failedCount)) / buildCount : 0;
		String imgForecast = "<img src='KnowWEExtension/ci4ke/images/" +
				pixelSize + "x" + pixelSize + "/%s.png' align='absmiddle' alt='%<s' title='%s'>";

		if (score == 0) {
			imgForecast = String.format(imgForecast, "health-00to19",
					"All recent builds failed.");
		}
		else if (score == 100) {
			imgForecast = String.format(imgForecast, "health-80plus",
					"No recent builds failed.");
		}
		else {
			String summary =
					failedCount + " out of the last " + buildCount + " builds failed.";
			if (score <= 20) {
				imgForecast = String.format(imgForecast, "health-00to19", summary);
			}
			else if (score <= 40) {
				imgForecast = String.format(imgForecast, "health-20to39", summary);
			}
			else if (score <= 60) {
				imgForecast = String.format(imgForecast, "health-40to59", summary);
			}
			else if (score <= 80) {
				imgForecast = String.format(imgForecast, "health-60to79", summary);
			}
			else {
				imgForecast = String.format(imgForecast, "health-80plus", summary);
			}
		}

		return imgForecast;
	}
}
