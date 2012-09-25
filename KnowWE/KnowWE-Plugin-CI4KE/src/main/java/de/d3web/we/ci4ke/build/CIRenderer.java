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
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import de.d3web.testing.BuildResult;
import de.d3web.testing.Message;
import de.d3web.testing.Message.Type;
import de.d3web.testing.MessageObject;
import de.d3web.testing.Test;
import de.d3web.testing.TestManager;
import de.d3web.testing.TestParser;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.rendering.ObjectNameRenderer;
import de.d3web.we.ci4ke.rendering.ObjectNameRendererManager;
import de.d3web.we.ci4ke.util.CIUtils;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Strings;

/**
 * 
 * @author volker_belli
 * @created 19.05.2012
 */
public class CIRenderer {

	private final CIDashboard dashboard;

	private final String dashboardName;

	private final String dashboardNameEncoded;

	/**
	 * Creates a new CIBuildRenderer for the specified Dashboard.
	 * 
	 */
	public CIRenderer(CIDashboard dashboard) {
		this.dashboard = dashboard;
		this.dashboardName = dashboard.getDashboardName();
		this.dashboardNameEncoded = Strings.encodeURL(dashboardName);
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
			sb.append(renderResultType(buildResult, 16));

			sb.append("</td><td>");
			sb.append("<td>");

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
		return renderHeaderResultType(build.getOverallResult(), pixelSize);
	}

	/**
	 * Renders out the test results of a selected Build
	 */
	public String renderBuildDetails(BuildResult build) {

		StringBuffer buffy = new StringBuffer();

		buffy.append("<div id='" + dashboardNameEncoded
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

		// render bullet
		Type type = result.getType();
		buffy.append(renderResultType(type, 16));

		String name = result.getTestName();
		Test<?> test = TestManager.findTest(name);
		String title = "";
		if (test != null) {
			title = test.getDescription();
		}

		// render test-name
		buffy.append("<span class='ci-test-title' title='" + title + "'>");
		buffy.append(name);

		// render test-configuration (if existent)
		String[] config = result.getConfiguration();
		if (config != null && !(config.length == 0)) {
			buffy.append("<span class='ci-configuration'>");
			buffy.append(TestParser.concatParameters(config));
			buffy.append("</span>");
		}
		buffy.append("</span>");

		// render test-message (if exists)
		renderMessage(buffy, result);

		buffy.append("</div>\n");
	}

	private void renderMessage(StringBuffer buffy, TestResult result) {
		String messageText = generateMessageText(result);
		if (!messageText.isEmpty()) {
			buffy.append("<div class='ci-message'>");
			buffy.append(messageText);
			buffy.append("</div>");
		}
	}

	private String generateMessageText(TestResult result) {
		StringBuilder messageText = new StringBuilder();
		StringBuilder toolTip = new StringBuilder();
		Collection<String> testObjectNames = result.getTestObjectNames();
		int successes = 0;
		int maxObjects = 40;
		for (String testObjectName : testObjectNames) {
			de.d3web.testing.Message message = result.getMessage(testObjectName);
			Type messageType = message.getType();
			if (messageType.equals(Type.SUCCESS)) {
				if (successes < maxObjects) {
					toolTip.append(testObjectName + "\n");
				}
				else if (successes == maxObjects) {
					toolTip.append("...");
				}
				successes++;
			}
			else {
				String text = renderMessage(message);
				Test<?> test = TestManager.findTest(result.getTestName());
				Class<?> testObjectClass = test.getTestObjectClass();
				String renderedTestObjectName = renderObjectName(testObjectName,
						testObjectClass);
				messageText.append(messageType.toString() + ": " + text +
						" (test object: " + renderedTestObjectName + ")\n");
			}
		}
		messageText.append("<span"
				+ (toolTip.length() == 0 ? "" : " title='" + toolTip.toString() + "'") + ">"
				+ successes + " test objects tested successfully</span>");
		return messageText.toString();
	}

	private String renderMessage(Message message) {
		String text = message.getText();
		if (text == null) text = "";
		ArrayList<MessageObject> objects = new ArrayList<MessageObject>(message.getObjects());
		Collections.sort(objects, new SizeComparator());
		String[] targets = new String[objects.size()];
		String[] replacements = new String[objects.size()];
		int i = 0;
		for (MessageObject object : objects) {
			String renderedObjectName = renderObjectName(object.getObjectName(),
					object.geObjectClass());
			targets[i] = object.getObjectName();
			replacements[i] = renderedObjectName;
			i++;
		}
		// This is non repeating and since the targets are sorted by length, the
		// replacing will be correct, if all targets are in the text. If not all
		// are in the text, the replacing will only be inaccurate in rare cases
		// (e.g. targets[0].contains(target[1]...)
		text = StringUtils.replaceEach(text, targets, replacements);
		return text;
	}

	public String renderObjectName(String objectName, Class<?> objectClass) {
		ObjectNameRenderer objectRenderer = ObjectNameRendererManager.getObjectNameRenderer(objectClass);
		if (objectRenderer == null) {
			Logger.getLogger(this.getClass().getName()).log(
					Level.WARNING, "No renderer found for " + objectClass);
			return objectName;
		}
		return objectRenderer.render(objectName);
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

	public String renderResultType(Type resultType, int pixelSize) {
		String imageURL = "KnowWEExtension/ci4ke/images/"
				+
				pixelSize + "x" + pixelSize
				+ "/%s.png";

		String imgBulb = "<img class='ci-state' width='" + pixelSize
				+ "'id='state_" + dashboardNameEncoded
				+ "' " +
				"dashboardName='" + dashboardNameEncoded + "' src='" + imageURL
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

	public String renderHeaderResultType(Type resultType, int pixelSize) {

		if (CIUtils.buildRunning(dashboardName)) {
			// if currently a build is running show animated icon
			String imageURL = "KnowWEExtension/images/ajax-loader16.gif";
			String imgBulb = "<img class='ci-state' running='true' width='"
					+ pixelSize + "'id='state_"
					+ dashboardNameEncoded
					+ "' " +
					"dashboardName='" + dashboardNameEncoded + "' src='" + imageURL
					+ "' alt='%<s' align='absmiddle' title='%s'>";
			return imgBulb;
		}

		return renderResultType(resultType, pixelSize);
	}

	public String renderDashboardHeader(BuildResult latestBuild) {
		StringBuilder string = new StringBuilder();

		if (latestBuild != null || CIUtils.buildRunning(dashboardName)) {
			CIRenderer renderer = dashboard.getRenderer();
			string.append(renderer.renderCurrentBuildStatus(22));
			string.append(renderer.renderBuildHealthReport(22));
		}
		string.append("<span class='ci-name'>" + dashboardName + "</span>");

		// insert tag for progress bar
		// open/close div progress_container
		if (CIUtils.buildRunning(dashboardName)) {
			renderProgressInfo(string);
		}
		return string.toString();
	}

	public void renderProgressInfo(StringBuilder string) {
		string.append("<span class='ci-progress-info' id='" + dashboardNameEncoded
				+ "_progress-container'>");
		appendAbortButton(string);
		string.append("<span class='ci-progress-value-wrap'><span class='ci-progress-value' id='"
				+ dashboardNameEncoded + "_progress-value'>0 %");
		string.append("</span></span>");
		string.append("<span class='ci-progess-text' id='"
				+ dashboardNameEncoded + "_progress-text'>Build running...</span>");
		string.append("</span>");
	}

	private void appendAbortButton(StringBuilder string) {
		string.append("<a href=\"javascript:stopRunningBuild('"
				+ dashboardNameEncoded
				+ "', '"
				+ dashboard.getDashboardArticle()
				+ "', '"
				+ KnowWEUtils.getURLLink(dashboard.getDashboardArticle() + "#"
						+ dashboardNameEncoded)
				+ "')\"><img class='ci-abort-build' height='16' title='Stops the current build' " +
				"src='KnowWEExtension/images/cross.png'></img></a>");

	}

	public String renderForecastIcon(int buildCount, int failedCount, int pixelSize) {

		int score = (buildCount > 0) ? score = (100 * (buildCount - failedCount)) / buildCount : 0;
		String imgForecast = "<img class='ci-forecast' src='KnowWEExtension/ci4ke/images/" +
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

	private class SizeComparator implements Comparator<MessageObject> {

		@Override
		public int compare(MessageObject o1, MessageObject o2) {
			if (o1 == o2) return 0;
			if (o1 == null) return -1;
			if (o2 == null) return 1;
			String objectName1 = o1.getObjectName();
			String objectName2 = o2.getObjectName();
			if (objectName1 == objectName2) return 0; // if both are null
			if (objectName1 == null) return -1;
			if (objectName2 == null) return 1;
			return -(new Integer(objectName1.length()).compareTo(objectName2.length()));
		}
	}
}
