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

import java.util.List;

import org.jdom.Element;

import de.d3web.we.ci4ke.build.CIBuildPersistenceHandler;
import de.d3web.we.ci4ke.testing.CITestResult.Type;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.knowwe.core.KnowWERessourceLoader;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * 
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 01.12.2010
 */
public class CIDashboardRenderer extends DefaultMarkupRenderer<CIDashboardType> {

	public CIDashboardRenderer() {
		super("KnowWEExtension/ci4ke/images/22x22/ci.png", false);
	}

	@Override
	protected void renderContents(KnowWEArticle article, Section<CIDashboardType> section, UserContext user, StringBuilder string) {

		KnowWERessourceLoader.getInstance().add("CI4KE.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);
		KnowWERessourceLoader.getInstance().add("CIPlugin.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);

		boolean sectionHasError = Messages.getErrors(article, section).size() > 0;

		if (!sectionHasError) {
			String dashboardName = DefaultMarkupType.getAnnotation(section,
					CIDashboardType.NAME_KEY);
			String dashboardNameEscaped = CIUtilities.utf8Escape(dashboardName);
			string.append(KnowWEUtils.maskHTML("<div id='" + dashboardNameEscaped
					+ "' class='ci-title'>"));
			string.append(KnowWEUtils.maskHTML(renderDashboardContents(dashboardName,
					article.getTitle())));
			string.append(KnowWEUtils.maskHTML("</div>"));
		}
	}

	/**
	 * Renders out the content of a CIDashboard (build-history and rest-result
	 * pane)
	 * 
	 * @created 02.12.2010
	 * @param dashboardName
	 * @param dashboardArticleTitle
	 * @param string
	 */
	public static String renderDashboardContents(String dashboardName, String dashboardArticleTitle) {

		StringBuilder string = new StringBuilder();
		CIBuildPersistenceHandler handler = CIBuildPersistenceHandler.
				getHandler(dashboardName, dashboardArticleTitle);
		if (handler == null) {
			return "";
		}
		String dashboardNameEscaped = CIUtilities.utf8Escape(dashboardName);

		string.append("<h3>");
		// if at least one build has been executed: Render forecast icons:
		if (handler.getCurrentBuildNumber() > 0) {
			string.append(handler.renderCurrentBuildStatus(22) + "  ");
			string.append(handler.renderBuildHealthReport(22) + "  ");
		}
		string.append(dashboardName + "</h3>");

		// render the last five builds:
		string.append("<div id='" + dashboardNameEscaped
				+ "-column-left' class='ci-column-left'>");
		string.append("<div id='" + dashboardNameEscaped
				+ "-build-table'>");
		string.append(handler.renderNewestBuilds(10));
		string.append("</div></div>");

		// render the build-details pane
		string.append("<div id='" + dashboardNameEscaped
				+ "-build-details-wrapper' class='ci-build-details-wrapper'>");
		string.append(renderBuildDetails(dashboardName,
				dashboardArticleTitle, handler.getCurrentBuildNumber()));
		string.append("</div>");

		return string.toString();
	}

	/**
	 * Renders out the test results of a selected Build
	 */
	public static String renderBuildDetails(String dashboardName, String dashboardArticleTitle, int selectedBuildNumber) {

		String dashboardNameEscaped = CIUtilities.utf8Escape(dashboardName);

		CIBuildPersistenceHandler handler = CIBuildPersistenceHandler.getHandler(dashboardName,
				dashboardArticleTitle);
		StringBuffer buffy = new StringBuffer();

		// ------------------------------------------------------------------------
		// Render the build details in the middle column
		// (ci-column-middle)
		// ------------------------------------------------------------------------

		buffy.append("<div id='" + dashboardNameEscaped
				+ "-column-middle' class='ci-column-middle'>");

		String xPath = "builds/build[@nr=%s]/tests/test";
		List<?> tests = handler.selectNodes(String.format(xPath, selectedBuildNumber));

		Element buildNr = (Element) handler.selectSingleNode("builds/build[@nr="
				+ selectedBuildNumber + "]"); // e.getAttributeValue("executed")
		if (buildNr != null) {
			String buildDate = buildNr.getAttributeValue("executed");
			if (buildDate == null) buildDate = "";
			buffy.append("<H4>Build #" + selectedBuildNumber +
					" (" + buildDate + ") ");

			// get the build duration time
			String buildDuration = buildNr.getAttributeValue("duration");
			if (buildDuration != null) {
				buffy.append(" in ");
				long buildD = Long.parseLong(buildDuration);
				if (buildD < 1000) buffy.append(buildD + " msec.");
				else if (buildD >= 1000 && buildD < 60000) buffy.append((buildD / 1000)
						+ " sec.");
				else buffy.append((buildD / 60000) + "min.");
			}

			buffy.append("</H4>");

			for (Object o : tests) {
				if (o instanceof Element) {
					Element e = (Element) o;

					buffy.append("<div class='ci-collapsible-box'>");
					// buffy.append("<b>";

					// prepare some information
					String name = e.getAttributeValue("name");
					String result = e.getAttributeValue("result");
					String message = e.getAttributeValue("message");
					String config = e.getAttributeValue("configuration");

					// render bullet
					if (result != null && !result.isEmpty()) {
						Type buildResult = Type.valueOf(result);
						buffy.append(CIUtilities.renderResultType(buildResult, 16));
					}

					buffy.append("<span class='ci-test-title'>");
					// render test-name
					if (name != null && !name.isEmpty()) {
						buffy.append(name);
					}

					// render test-configuration (if existent)
					if (config != null && !config.isEmpty()) {
						buffy.append("<span class='ci-configuration'>");
						buffy.append(" (").append(config).append(")");
						buffy.append("</span>");
					}
					buffy.append("</span>");

					// render test-message (if existent)
					if (message != null && !message.isEmpty()) {
						buffy.append("<div class='ci-message'>"); // style=\"display: none;\">");
						buffy.append(message);
						buffy.append("</div>");
					}

					buffy.append("</div>\n");
				}
			}
		}
		else {
			buffy.append("<div class='ci-no-details'>No build selected.</div>");
		}
		buffy.append("</div>\n");
		return buffy.toString();
	}
}