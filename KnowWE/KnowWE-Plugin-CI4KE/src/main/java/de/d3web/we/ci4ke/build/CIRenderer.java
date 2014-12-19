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
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import de.d3web.collections.DefaultMultiMap;
import de.d3web.collections.MultiMap;
import de.d3web.collections.MultiMaps;
import de.d3web.strings.Strings;
import de.d3web.testing.BuildResult;
import de.d3web.testing.Message;
import de.d3web.testing.Message.Type;
import de.d3web.testing.MessageObject;
import de.d3web.testing.Test;
import de.d3web.testing.TestGroup;
import de.d3web.testing.TestManager;
import de.d3web.testing.TestParser;
import de.d3web.testing.TestResult;
import de.d3web.utils.Log;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.rendering.ObjectNameRenderer;
import de.d3web.we.ci4ke.dashboard.rendering.ObjectNameRendererManager;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.util.Icon;

/**
 * @author volker_belli
 * @created 19.05.2012
 */
public class CIRenderer {

	private final CIDashboard dashboard;

	private final String dashboardName;

	private final String dashboardNameEncoded;

	/**
	 * Creates a new CIBuildRenderer for the specified Dashboard.
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
	 */
	public void renderBuildHealthReport(RenderResult result) {

		List<BuildResult> lastBuilds = dashboard.getBuilds(
				-1, 5);
		int count = lastBuilds.size();
		int failed = 0;
		for (BuildResult build : lastBuilds) {
			if (!Type.SUCCESS.equals(build.getOverallResult())) {
				failed++;
			}
		}
		renderForecastIcon(count, failed, result);
	}

	public void renderBuildList(int indexFromBack, int numberOfBuilds, int shownBuild, RenderResult sb) {

		int latestBuildNumber = dashboard.getLatestBuildNumber();

		if (indexFromBack == 0) indexFromBack = latestBuildNumber;
		if (numberOfBuilds < 1) numberOfBuilds = 10;

		List<BuildResult> builds = dashboard.getBuilds(indexFromBack, numberOfBuilds);

		sb.appendHtml("<H4>Builds</H4>");
		sb.appendHtml("<table width=\"100%\" border='1' class=\"build-table\">");

		// reverse order to have the most current builds on top
		for (BuildResult build : builds) {
			int buildNr = build.getBuildNumber();

			// mark currently shown build number
			String cssClass = "";
			if (buildNr == shownBuild) {
				cssClass = "selectedBuildNR";
			}
			sb.appendHtml("<tr class='" + cssClass + "'><td>");
			// starting with a nice image...
			Type buildResult = build.getOverallResult();
			renderBuildStatus(buildResult, false, Icon.BULB, sb);

			sb.appendHtml("</td><td>");
			sb.appendHtml("<td>");

			sb.appendHtml("<a onclick=\"_CI.refreshBuildDetails('"
					+ dashboardNameEncoded + "','"
					+ buildNr + "','" + indexFromBack + "');_CI.refreshBuildList('"
					+ dashboardNameEncoded + "', " + buildNr + ",'"
					+ indexFromBack + "','" + numberOfBuilds + "');\">");

			// _CI.refreshBuildList('"+ dashboardNameEncoded + "', " + buildNr +
			// ");

			// actual shown content:
			sb.append("#" + buildNr);
			sb.appendHtml("</a>");

			sb.appendHtml("</tr>");
		}
		sb.appendHtml("</table>");

		int latestDisplayedBuildNumber = indexFromBack;

		if (!builds.isEmpty()) {
			latestDisplayedBuildNumber = builds.get(0).getBuildNumber();
		}

		sb.appendHtmlTag("div", "class", "ci-test-nav-outer");
		sb.appendHtmlTag("div", "class", "ci-test-nav-inner");

		String buttonLeft = makeNavButton(numberOfBuilds, latestDisplayedBuildNumber, "-", Icon.LEFT, (latestDisplayedBuildNumber - numberOfBuilds > 0));
		String buttonRight = makeNavButton(numberOfBuilds, latestDisplayedBuildNumber, "+", Icon.RIGHT, latestDisplayedBuildNumber < latestBuildNumber);
		sb.appendHtml(buttonLeft);
		sb.appendHtml(buttonRight);

		sb.appendHtml("</div>");
		sb.appendHtml("</div>");

	}

	private String makeNavButton(int numberOfBuilds, int latestDisplayedBuildNumber, String sign, Icon icon, boolean visible) {
		String display = visible ? "visible" : "hidden";
		return "<button display='" + display + "' "
				+ "onclick=\"_CI.refreshBuildList('"
				+ dashboardNameEncoded + "', -1 ,"
				+ (latestDisplayedBuildNumber + sign + numberOfBuilds)
				+ ",'" + numberOfBuilds
				+ "');\" style=\"visibility:" + display + ";\">"
				+ icon.addClasses("knowwe-blue").toHtml()
				+ "</button>";
	}

	/**
	 * Renders the current build status (status of the last build)
	 *
	 * @created 27.05.2010
	 */
	public void renderCurrentBuildStatus(RenderResult result) {
		BuildResult build = dashboard.getLatestBuild();
		if (build != null) renderBuildStatus(build.getOverallResult(), true, Icon.BULB, result);
	}

	/**
	 * Renders out the test results of a selected Build
	 */
	public void renderBuildDetails(String web, BuildResult build, RenderResult result) {

		result.appendHtml("<div id='" + dashboardNameEncoded
				+ "-column-middle' class='ci-column-middle'>");

		if (build != null) {
			apppendBuildHeadline(build, result);
			MultiMap<String, TestResult> groups = getTestGroups(build.getResults());
			for (String group : groups.keySet()) {
				Set<TestResult> groupResults = groups.getValues(group);
				// open group if available
				if (group != null) {
					Type type = BuildResult.getOverallResult(groupResults);
					openCollapse(type, result);
					result.appendHtml("<span class='ci-test-title ci-test-group'>")
							.append(group)
							.appendHtml("</span>");
					openMessageBlock(type, result);
				}
				// render the particular tests
				for (TestResult testResult : groupResults) {
					appendTestResult(web, testResult, result);
				}
				// close group if available
				if (group != null) {
					closeMessageBlock(result);
					closeCollapse(result);
				}
			}
		}
		else {
			result.appendHtml("<div class='ci-no-details'>No build found.</div>");
		}
		result.appendHtml("</div>\n");
	}

	private MultiMap<String, TestResult> getTestGroups(List<TestResult> testResults) {
		MultiMap<String, TestResult> groups = new DefaultMultiMap<String, TestResult>(
				MultiMaps.<String>linkedFactory(), MultiMaps.<TestResult>linkedFactory());
		String currentGroup = null;
		for (TestResult testResult : testResults) {
			if (TestGroup.TEST_GROUP_NAME.equals(testResult.getTestName())) {
				currentGroup = testResult.getConfiguration()[1];
			}
			else {
				groups.put(currentGroup, testResult);
			}
		}
		return groups;
	}

	private void appendTestResult(String web, TestResult testResult, RenderResult renderResult) {

		// ruling out special characters (which are causing problems)
		String name = testResult.getTestName();

		// prepare some information
		Message summary = testResult.getSummary();
		Type type = (summary == null) ? Type.ERROR : summary.getType();
		String text = (summary == null) ? null : summary.getText();

		Test<?> test = TestManager.findTest(name);
		String title = "";
		if (test != null) {
			title = test.getDescription();
		}

		// render buttons
		openCollapse(type, renderResult);

		// render test name
		renderResult.appendHtml("<span class='ci-test-title' title='" + title + "'>");
		renderResult.append(name);

		// render test configuration (if exists)
		String[] config = testResult.getConfiguration();
		boolean hasConfig = config != null && !(config.length == 0);
		boolean hasText = !Strings.isBlank(text);
		if (hasConfig || hasText) {
			renderResult.appendHtml("<span class='ci-configuration'>");
			if (hasConfig) {
				renderResult.append("( ")
						.appendJSPWikiMarkup(TestParser.concatParameters(config))
						.append(" )");
			}
			if (hasText) {
				renderResult.appendHtml(": ").appendJSPWikiMarkup(text);
			}
			renderResult.appendHtml("</span>");
		}
		renderResult.appendHtml("</span>");

		// render test-message (if exists)
		openMessageBlock(type, renderResult);
		appendMessage(web, testResult, renderResult);
		closeMessageBlock(renderResult);
		closeCollapse(renderResult);
	}

	private void openCollapse(Type type, RenderResult renderResult) {
		renderResult.appendHtml("<div class='ci-collapsible-box'>");

		String styleExpand = type == Type.SUCCESS ? "" : "style='display:none' ";
		renderResult.appendHtml("<span " + styleExpand
				+ "class='expandCIMessage' onclick='KNOWWE.plugin.ci4ke.expandMessage(this)'>");
		renderBuildStatus(type, false, Icon.EXPAND, renderResult);
		renderResult.appendHtml("</span>");

		String styleCollapse = type == Type.SUCCESS ? "style='display:none' " : "";
		renderResult.appendHtml("<span " + styleCollapse
				+ "class='collapseCIMessage' onclick='KNOWWE.plugin.ci4ke.collapseMessage(this)'>");
		renderBuildStatus(type, false, Icon.COLLAPSE, renderResult);
		renderResult.appendHtml("</span>");
	}

	private void closeCollapse(RenderResult renderResult) {
		renderResult.appendHtml("</div>\n");
	}

	private void openMessageBlock(Type type, RenderResult renderResult) {
		// not visible at beginning
		String styleCollapse = (type == null || type == Type.SUCCESS)
				? "style='display:none' " : "";
		renderResult.appendHtml("<div " + styleCollapse + "class='ci-message'>");
	}

	private void closeMessageBlock(RenderResult renderResult) {
		renderResult.appendHtml("</div>");
	}

	private void appendMessage(String web, TestResult testResult, RenderResult renderResult) {
		Collection<String> testObjectNames = testResult.getTestObjectsWithUnexpectedOutcome();
		int successes = testResult.getSuccessfullyTestedObjects();
		for (String testObjectName : testObjectNames) {
			de.d3web.testing.Message message = testResult.getMessageForTestObject(testObjectName);
			if (message == null) continue;
			Type messageType = message.getType();
			Test<?> test = TestManager.findTest(testResult.getTestName());
			Class<?> testObjectClass = null;
			if (test != null) {
				testObjectClass = test.getTestObjectClass();
			}
			else {
				Log.warning("No class found for test: " + testResult.getTestName());
			}
			renderResult.appendHtml("<p></p>");
			renderResult.append("__" + messageType.toString() + "__: ");
			appendMessageText(web, message, renderResult);
			renderResult.appendHtml("<br>\n");
			if (!message.getText().contains(testObjectName)) {
				renderResult.appendHtml("(test object: ");
				renderObjectName(web, testObjectName, testObjectClass, renderResult);
				renderResult.appendHtml(")<br>\n");
			}
		}
		renderResult.appendHtml("<span>" + successes + " test objects tested successfully</span>");
	}

	private void appendMessageText(String web, Message message, RenderResult result) {
		String text = message.getText();
		if (text == null) text = "";
		ArrayList<MessageObject> objects = new ArrayList<MessageObject>(message.getObjects());
		Collections.sort(objects, new SizeComparator());
		String[] targets = new String[objects.size()];
		String[] replacements = new String[objects.size()];
		int i = 0;
		for (MessageObject object : objects) {
			RenderResult temp = new RenderResult(result);
			renderObjectName(web,
					object.getObjectName(), object.geObjectClass(), temp);
			String renderedObjectName = temp.toStringRaw();
			targets[i] = object.getObjectName();
			replacements[i] = renderedObjectName;
			i++;
		}
		// This is non repeating and since the targets are sorted by length, the
		// replacing will be correct, if all targets are in the text. If not all
		// are in the text, the replacing will only be inaccurate in rare cases
		// (e.g. targets[0].contains(target[1]...)
		text = StringUtils.replaceEach(text, targets, replacements);
		String lb = new RenderResult(result).appendHtml("<br>\n").toStringRaw();
		text = text.replaceAll("\\r?\\n", lb);
		result.appendJSPWikiMarkup(text);
	}

	public void renderObjectName(String web, String objectName, Class<?> objectClass, RenderResult result) {
		if (objectClass == null) {
			// the case on old build version with outdated test names
			result.append(objectName);
			return;
		}
		ObjectNameRenderer objectRenderer = ObjectNameRendererManager.getObjectNameRenderer(objectClass);
		if (objectRenderer == null) {
			Log.warning("No renderer found for " + objectClass);
			result.append(objectName);
			return;
		}
		objectRenderer.render(web, objectName, result);
	}

	private void apppendBuildHeadline(BuildResult build, RenderResult buffy) {
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		String buildDate = dateFormat.format(build.getBuildDate());
		buffy.appendHtml("<H4>Build #").append(build.getBuildNumber())
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

		buffy.appendHtml("</H4>");
	}

	public void renderBuildStatus(Type resultType, boolean checkRunning, Icon icon, RenderResult result) {

		boolean showRunning = checkRunning && CIBuildManager.isRunning(dashboard);

		String imgBulb = "<i class='fa %s %s ci-state' dashboardName='" + dashboardNameEncoded + "'"
				+ " running='%s'"
				+ " title='%s'></i>";

		if (showRunning) {

			imgBulb = String.format(imgBulb, "fa-spinner", "", "true", "Build running!");
		}
		else {
			switch (resultType) {
				case SUCCESS:
					imgBulb = String.format(imgBulb, icon.getCssClass(), "knowwe-ok", "false", "Build successful: " + Strings
							.encodeHtml(dashboardName));
				case FAILURE:
					imgBulb = String.format(imgBulb, icon.getCssClass(), "knowwe-error", "false", "Build failed: " + Strings
							.encodeHtml(dashboardName));
				case ERROR:
					imgBulb = String.format(imgBulb, icon.getCssClass(), "knowwe-gray", "false", "Build has errors: " + Strings
							.encodeHtml(dashboardName));
			}
		}

		result.appendHtml(imgBulb);
	}

	public void renderDashboardHeader(BuildResult latestBuild, RenderResult result) {
		result.appendHtml("<div class='ci-header' id='ci-header_"
				+ dashboard.getDashboardName() + "'>");

		if (latestBuild != null || CIBuildManager.isRunning(dashboard)) {
			CIRenderer renderer = dashboard.getRenderer();
			renderer.renderBuildHealthReport(result);
			renderer.renderCurrentBuildStatus(result);
		}
		result.appendHtml("<span class='ci-name'>" + dashboardName + "</span>");

		renderProgressInfo(result);

		result.appendHtml("</div>");
	}

	public void renderProgressInfo(RenderResult string) {

		string.appendHtml("<span " +
				"class='ci-progress-info' id='" + dashboardNameEncoded + "_progress-container'>");
		appendAbortButton(string);
		string.appendHtml("<span class='ci-progress-value-wrap'><span class='ci-progress-value' id='"
				+ dashboardNameEncoded + "_progress-value'>0 %");
		string.appendHtml("</span></span>");
		string.appendHtml("<span class='ci-progess-text' id='"
				+ dashboardNameEncoded + "_progress-text'>Build running...</span>");
		string.appendHtml("</span>");

	}

	private void appendAbortButton(RenderResult string) {
		string.appendHtml("<a href=\"javascript:_CI.stopRunningBuild('"
				+ dashboardNameEncoded
				+ "', '"
				+ dashboard.getDashboardArticle()
				+ "', '"
				+ KnowWEUtils.getURLLink(dashboard.getDashboardArticle() + "#"
				+ dashboardNameEncoded)
				+ "')\"><img class='ci-abort-build' height='16' title='Stops the current build' " +
				"src='KnowWEExtension/images/cross.png' /></a>");

	}

	public void renderForecastIcon(int buildCount, int failedCount, RenderResult result) {

		int score = (buildCount > 0) ? (100 * (buildCount - failedCount)) / buildCount : 0;
		String imgForecast = "<img class='ci-forecast' src='KnowWEExtension/ci4ke/images/22x22/%s.png' "
				+ "align='absmiddle' alt='%<s' title='%s'>";

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

		result.appendHtml(imgForecast);
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
