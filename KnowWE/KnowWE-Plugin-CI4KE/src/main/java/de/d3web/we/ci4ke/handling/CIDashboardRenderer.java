/*
 * Copyright (C) 2010 denkbares GmbH, Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.ci4ke.handling;

import java.util.Collection;
import java.util.List;

import org.jdom.Element;

import de.d3web.report.Message;
import de.d3web.we.ci4ke.build.CIBuildPersistenceHandler;
import de.d3web.we.ci4ke.testing.CITestResult.TestResultType;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;


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
	protected void renderContents(KnowWEArticle article, Section<CIDashboardType> section, KnowWEUserContext user, StringBuilder string) {

		KnowWERessourceLoader.getInstance().add("CI4KE.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);
		KnowWERessourceLoader.getInstance().add("CIPlugin.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);

		boolean sectionHasError = false;

		Collection<Message> messages = DefaultMarkupType.getMessages(article, section);
		for (Message m : messages) {
			if (m.getMessageType().equals(Message.ERROR)) {
				sectionHasError = true;
				break;
			}
		}
		if (!sectionHasError) {
			String dashboardName = DefaultMarkupType.getAnnotation(section,
					CIDashboardType.NAME_KEY);
			String dashboardNameEscaped = CIUtilities.utf8Escape(dashboardName);
			string.append(KnowWEUtils.maskHTML("<div id='" + dashboardNameEscaped + "'>"));
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

		string.append("<h3>" + dashboardName);
		// if at least one build has been executed: Render forecast icons:
		if (handler.getCurrentBuildNumber() > 0) {
			string.append(" - Current build status: " +
					handler.renderCurrentBuildStatus(22));
			string.append(" - Build health report: "
					+ handler.renderBuildHealthReport(22));
		}
		string.append("</h3>");

		// render the last five builds:
		string.append("<div id='" + dashboardNameEscaped
				+ "-column-left' class='ci-column-left'>");
		string.append("<div id='" + dashboardNameEscaped
				+ "-build-table'>");
		string.append(handler.renderNewestBuilds(5));
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
		// Render the build details in the middle colum
		// (ci-column-middle)
		// ------------------------------------------------------------------------
	
		buffy.append("<div id='" + dashboardNameEscaped
				+ "-column-middle' class='ci-column-middle'>");
	
		String xPath = "builds/build[@nr=%s]/tests/test";
		List<?> tests = handler.selectNodes(String.format(xPath, selectedBuildNumber));
	
		buffy.append("<h3>Results of Build #" + selectedBuildNumber + "</h3>");
	
		for (Object o : tests) {
			if (o instanceof Element) {
				Element e = (Element) o;
	
				buffy.append("<div class='ci-collapsible-box'><h4>");
	
				// Render Test Result
				String s = e.getAttributeValue("result");
				if (s != null && !s.isEmpty()) {
					TestResultType buildResult = TestResultType.valueOf(s);
					buffy.append(CIUtilities.renderResultType(buildResult, 16));
				}
				// Render Test-Name
				s = e.getAttributeValue("name");
				if (s != null && !s.isEmpty()) {
					buffy.append(s);
				}
	
				buffy.append("</h4>");
	
				// Render Test Message (if existent)
				buffy.append("<span style=\"display: none;\">");
				s = e.getAttributeValue("message");
				if (s != null && !s.isEmpty()) {
					buffy.append(s);
				}
	
				buffy.append("</span>");
	
				buffy.append("</div>\n");
			}
		}
		buffy.append("</div>");
		return buffy.toString();
	}
}