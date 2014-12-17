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
package de.d3web.we.ci4ke.dashboard.rendering;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import de.d3web.strings.Strings;
import de.d3web.testing.BuildResult;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.d3web.we.ci4ke.dashboard.action.CIDashboardToolProvider;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolUtils;

/**
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 01.12.2010
 */
public class CIDashboardRenderer extends DefaultMarkupRenderer {

	public CIDashboardRenderer() {
		super();
	}

	@Override
	protected void renderContents(Section<?> section, UserContext user, RenderResult string) {

		CIDashboard dashboard = CIDashboardManager.getDashboard(Sections.cast(section,
				CIDashboardType.class));

		string.append(renderDashboardContents(user, dashboard));

	}

	private static boolean isDashBoardModifiedAfterLatestBuild(Section<CIDashboardType> section) {

		String title = section.getTitle();
		String currentDashboardSourcetext = section.getText();
		CIDashboard dashboard = CIDashboardManager.getDashboard(section);
		BuildResult latestBuild = dashboard.getLatestBuild();
		if (latestBuild == null) return false; // nothing to do

		Date buildDate = latestBuild.getBuildDate();
		int versionAtBuildDate = -1;

		try {
			versionAtBuildDate = KnowWEUtils.getArticleVersionAtDate(
					title, buildDate);
			// case for invalid buildDates (before corresponding page existed)
			if (versionAtBuildDate < -1) return true;
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		String sourceTextAtBuildTime = Environment.getInstance().getWikiConnector().getArticleText(
				section.getTitle(), versionAtBuildDate);

		if (sourceTextAtBuildTime.contains(currentDashboardSourcetext)) {
			// this is only safe for one single dashboard per article
			return false;
		}

		return true;
	}

	/**
	 * Renders out the content of a CIDashboard (build-history and rest-result
	 * pane)
	 *
	 * @param user      the current user context to render the dashboard for
	 * @param dashboard the dashboard to be rendered
	 * @created 02.12.2010
	 */
	public static String renderDashboardContents(UserContext user, CIDashboard dashboard) {
		String dashboardName = dashboard.getDashboardName();
		Section<CIDashboardType> dashboardSection = dashboard.getDashboardSection();

		RenderResult string = new RenderResult(user);

		string.appendHtml("<div name='" + Strings.encodeURL(dashboardName)
				+ "' class='ci-title'>");

		isUniqueDashboardName(dashboard, string);

		checkForOutdatedBuild(user, dashboardName, dashboardSection, string);

		appendDashboard(dashboard, string);

		string.appendHtml("</div>");
		return string.toStringRaw();
	}

	private static void appendDashboard(CIDashboard dashboard, RenderResult string) {
		BuildResult latestBuild = dashboard.getLatestBuild();

		dashboard.getRenderer().renderDashboardHeader(latestBuild, string);

		// start table (only a single row)
		string.appendHtml("<table><tr>");

		appendBuildListCell(dashboard, latestBuild, string);

		appendBuildDetailsCell(dashboard, latestBuild, string);

		// close table
		string.appendHtml("</tr></table>");
	}

	private static void appendBuildListCell(CIDashboard dashboard, BuildResult shownBuild, RenderResult string) {
		String dashboardNameEscaped = Strings.encodeURL(dashboard.getDashboardName());
		string.appendHtml("<td valign='top' style='border-right: 1px solid #DDDDDD;'>");
		string.appendHtml("<div id='")
				.append(dashboardNameEscaped)
				.appendHtml("-column-left' class='ci-column-left'>");
		string.appendHtml("<div id='")
				.append(dashboardNameEscaped)
				.appendHtml("-build-table'>");
		if (shownBuild != null) {
			// render build history
			dashboard.getRenderer().renderBuildList(0, 10,
					shownBuild.getBuildNumber(), string);
		}
		string.appendHtml("</div></div>");
		string.appendHtml("</td>");
	}

	private static void appendBuildDetailsCell(CIDashboard dashboard, BuildResult shownBuild, RenderResult string) {

		string.appendHtml("<td valign='top'>");
		string.appendHtml("<div id='")
				.append(Strings.encodeURL(dashboard.getDashboardName()))
				.appendHtml(
						"-build-details-wrapper' class='ci-build-details-wrapper'>");
		dashboard.getRenderer().renderBuildDetails(dashboard.getWeb(), shownBuild, string);
		string.appendHtml("</div>");
		string.appendHtml("</td>");
	}

	private static void checkForOutdatedBuild(UserContext user, String dashboardName, Section<CIDashboardType> dashboardSection, RenderResult string) {
		// check whether dashboard definition has been changed
		// if so render outdated-warning
		boolean buildOutdated = isDashBoardModifiedAfterLatestBuild(dashboardSection);
		if (buildOutdated) {
			RenderResult warningString = new RenderResult(string);
			warningString.append("Dashboard has been modified. Latest build is not up to date. (Consider to trigger new build: ");
			Tool buildTool = CIDashboardToolProvider.getStartNewBuildTool(dashboardName,
					dashboardSection.getTitle());
			String id = "modified-warning_" + dashboardName;
			// insert build button/link into warning message
			warningString.appendHtml("<div id='" + id
					+ "' style='display:inline;' class=\""
					+ buildTool.getClass().getSimpleName() + "\" >" +
					"<a " + ToolUtils.getActionAttribute(buildTool) + ">" +
					"<img height='14'" +
					"title=\"" + Strings.encodeHtml(buildTool.getDescription()) + "\" " +
					"src=\"" + buildTool.getIcon() + "\" />" +
					"</a></div>");

			warningString.append(")");

			renderMessagesOfType(Message.Type.WARNING,
					Messages.asList(Messages.warning(warningString.toStringRaw())),
					string);
		}
	}

	private static boolean isUniqueDashboardName(CIDashboard dashboard, RenderResult string) {
		// check unique dashboard names and create error in case of
		// duplicates
		Collection<Section<CIDashboardType>> ciDashboardSections = CIDashboardManager.getDashboardSections(
				dashboard.getDashboardSection().getArticleManager(), dashboard.getDashboardName());
		if (ciDashboardSections.size() > 1) {
			TreeSet<String> articleTitles = new TreeSet<String>();
			for (Section<CIDashboardType> section : ciDashboardSections) {
				articleTitles.add(section.getTitle());
			}
			StringBuilder articleLinks = new StringBuilder();
			boolean first = true;
			for (String articleTitle : articleTitles) {
				if (first) {
					first = false;
				}
				else {
					articleLinks.append(", ");
				}
				articleLinks.append(KnowWEUtils.getLinkHTMLToArticle(articleTitle));
			}

			RenderResult errorText = new RenderResult(string);
			errorText.appendHtml("Multiple dashboards with same name on the following articles: "
					+ articleLinks.toString()
					+ ". Make sure every dashboard has a wiki-wide unique name!");
			Collection<Message> errorMsgs = Messages.asList(Messages.error(errorText.toStringRaw()));
			renderMessagesOfType(Message.Type.ERROR, errorMsgs, string);
			return false;
		}
		return true;
	}

}