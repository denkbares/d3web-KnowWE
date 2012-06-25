/*
 * Copyright (C) 2010 denkbares GmbH, Wuerzburg
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
package de.d3web.we.ci4ke.handling;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;

import de.d3web.testing.BuildResult;
import de.d3web.testing.Message.Type;
import de.d3web.testing.TestResult;
import de.d3web.we.ci4ke.build.CIBuildRenderer;
import de.d3web.we.ci4ke.build.Dashboard;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.knowwe.core.Environment;
import de.knowwe.core.RessourceLoader;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Strings;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * 
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 01.12.2010
 */
public class CIDashboardRenderer extends DefaultMarkupRenderer {

	public CIDashboardRenderer() {
		super("KnowWEExtension/ci4ke/images/22x22/ci.png");
	}

	@Override
	protected void renderContents(Section<?> section, UserContext user, StringBuilder string) {

		RessourceLoader.getInstance().add("CI4KE.css",
				RessourceLoader.RESOURCE_STYLESHEET);
		RessourceLoader.getInstance().add("CIPlugin.js",
				RessourceLoader.RESOURCE_SCRIPT);

		boolean sectionHasError = Messages.getMessages(section, Message.Type.ERROR).size() > 0;

		if (!sectionHasError) {
			String dashboardName = DefaultMarkupType.getAnnotation(section,
					CIDashboardType.NAME_KEY);
			String dashboardNameEscaped = CIUtilities.utf8Escape(dashboardName);

			string.append(Strings.maskHTML("<div id='" + dashboardNameEscaped
					+ "' class='ci-title'>"));
			string.append(Strings.maskHTML(renderDashboardContents(user,
					section.getTitle(), dashboardName)));
			string.append(Strings.maskHTML("</div>"));
		}
	}

	/**
	 * 
	 * @created 25.06.2012
	 * @return
	 */
	private static boolean checkDashBoardEditedAfterLatestBuild(Section<?> section, UserContext user, String dashboardName) {
		String title = section.getTitle();
		String currentDashboardSourcetext = section.getText();
		Dashboard dashboard = Dashboard.getDashboard(user.getWeb(), title,
				dashboardName);
		BuildResult latestBuild = dashboard.getLatestBuild();
		Date buildDate = latestBuild.getBuildDate();
		int versionAtBuildDate = -1;

		try {
			versionAtBuildDate = Environment.getInstance().getWikiConnector().getVersionAtDate(
					title, buildDate);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String sourceTextAtBuildTime = Environment.getInstance().getWikiConnector().getVersion(
				section.getTitle(),
				versionAtBuildDate);

		if (sourceTextAtBuildTime.contains(currentDashboardSourcetext)) {
			return false; // this is only safe for one single dashboard per
							// article
		}

		return true;
	}

	/**
	 * Renders out the content of a CIDashboard (build-history and rest-result
	 * pane)
	 * 
	 * @created 02.12.2010
	 * @param web the web of the dashboard
	 * @param dashboardArticleTitle the name of the article of the dashboard
	 * @param dashboardName the name of the dashboard
	 */
	public static String renderDashboardContents(UserContext user, String dashboardArticleTitle, String dashboardName) {

		Section<CIDashboardType> dashboardSection = CIUtilities.findCIDashboardSection(
				dashboardArticleTitle, dashboardName);

		StringBuilder string = new StringBuilder();

		boolean buildOutdated = checkDashBoardEditedAfterLatestBuild(dashboardSection, user,
				dashboardName);
		if (buildOutdated) {
			String warningString = "Dashboard has been modified. Latest build is not up to date. (Consider to trigger new build)";
			renderMessagesOfType(Message.Type.WARNING,
					Messages.asList(Messages.warning(warningString)),
					string);
		}

		Dashboard dashboard = Dashboard.getDashboard(user.getWeb(), dashboardArticleTitle,
				dashboardName);
		CIBuildRenderer renderer = dashboard.getRenderer();
		String dashboardNameEscaped = CIUtilities.utf8Escape(dashboardName);

		string.append("<h3>");
		// if at least one build has been executed: Render forecast icons:
		BuildResult latestBuild = dashboard.getLatestBuild();
		if (latestBuild != null) {
			string.append(renderer.renderCurrentBuildStatus(22)).append("  ");
			string.append(renderer.renderBuildHealthReport(22)).append("  ");
		}
		string.append(dashboardName + "</h3>");

		// render the last five builds:
		string.append("<div id='")
				.append(dashboardNameEscaped)
				.append("-column-left' class='ci-column-left'>");
		string.append("<div id='")
				.append(dashboardNameEscaped)
				.append("-build-table'>");
		string.append(renderer.renderNewestBuilds(10));
		string.append("</div></div>");

		// render the build-details pane
		string.append("<div id='")
				.append(dashboardNameEscaped)
				.append("-build-details-wrapper' class='ci-build-details-wrapper'>");
		string.append(renderBuildDetails(dashboard, latestBuild));
		string.append("</div>");

		return string.toString();
	}

	/**
	 * Renders out the test results of a selected Build
	 */
	public static String renderBuildDetails(Dashboard dashboard, BuildResult build) {

		String dashboardNameEscaped = CIUtilities.utf8Escape(dashboard.getDashboardName());
		DateFormat dateFormat = DateFormat.getDateTimeInstance();

		StringBuffer buffy = new StringBuffer();

		// ------------------------------------------------------------------------
		// Render the build details in the middle column
		// (ci-column-middle)
		// ------------------------------------------------------------------------

		buffy.append("<div id='" + dashboardNameEscaped
				+ "-column-middle' class='ci-column-middle'>");

		if (build != null) {
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

			for (TestResult result : build.getResults()) {
				buffy.append("<div class='ci-collapsible-box'>");

				// prepare some information
				String name = result.getTestName();
				Type buildResult = result.getType();
				String messageText = "";
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
						messageText += messageType.toString() + ": " + text + " (test object: "
								+ testObject + ")\n";
					}
				}

				messageText = messageText + successes + " test objects tested successfully\n";

				String[] config = result.getArguments();

				// render bullet
				buffy.append(CIUtilities.renderResultType(buildResult, 16));

				// render test-name
				buffy.append("<span class='ci-test-title'>");
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
				if (messageText != null && !messageText.isEmpty()) {
					buffy.append("<div class='ci-message'>");
					buffy.append(messageText);
					buffy.append("</div>");
				}

				buffy.append("</div>\n");
			}
		}
		else {
			buffy.append("<div class='ci-no-details'>No build selected.</div>");
		}
		buffy.append("</div>\n");
		return buffy.toString();
	}
}