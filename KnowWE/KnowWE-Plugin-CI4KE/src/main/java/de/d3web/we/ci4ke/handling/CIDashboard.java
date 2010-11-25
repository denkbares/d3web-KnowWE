/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.jdom.Element;

import de.d3web.we.ci4ke.build.CIBuildPersistenceHandler;
import de.d3web.we.ci4ke.handling.CITestResult.TestResultType;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public class CIDashboard {

	private final CIConfig config;

	private final CIBuildPersistenceHandler persistenceHandler;

	public CIDashboard(Section<CIDashboardType> section) {
		this.config = (CIConfig) KnowWEUtils.getStoredObject(section.
				getArticle(), section, CIConfig.CICONFIG_STORE_KEY);
		persistenceHandler = CIBuildPersistenceHandler.getHandler(
				this.config.getDashboardName(), this.config.getDashboardArticleTitle());
	}

	public String render() {

		KnowWERessourceLoader.getInstance().add("CI4KE.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);
		KnowWERessourceLoader.getInstance().add("CIPlugin.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);

		StringBuffer html = new StringBuffer();

		html.append("<div id='" + config.getDashboardNameEscaped() + "' class='panel'>");
		html.append(renderDashboardContents());
		html.append("</div>");

		return html.toString();
	}

	public String renderDashboardContents() {

		StringBuffer html = new StringBuffer();

		html.append("<h3>");
		html.append(persistenceHandler.renderCurrentBuildStatus() + "&nbsp;&nbsp;&nbsp;&nbsp;");
		html.append(persistenceHandler.renderBuildHealthReport() + "&nbsp;&nbsp;&nbsp;&nbsp;");
		html.append(config.getDashboardName());

		html.append("</h3>\n");

		html.append("<div id='" + config.getDashboardNameEscaped() +
				"-content-wrapper' class='ci-content-wrapper'>");// Main content
																	// wrapper

		// Left Column: Lists all the knowledge-base Builds of the targeted
		// article
		html.append("<div id='" + config.getDashboardNameEscaped()
				+ "-column-left' class='ci-column-left'>");

		html.append("<h3 style=\"background-color: #CCCCCC;\">Build History");

		if (config.getTrigger().equals(CIDashboardType.CIBuildTriggers.onDemand)) {
			html.append("<a href=\"#\" onclick=\"fctExecuteNewBuild('"
					+ config.getDashboardNameEscaped() + "');\"");
			html.append("<img border=\"0\" align=\"right\" src='KnowWEExtension/ci4ke/images/22x22/clock.png' "
					+ "alt='Schedule a build' title='Schedule a build'></a>");
		}
		html.append("</h3>");
		// render Builds

		html.append("<div id=\"" + config.getDashboardNameEscaped() + "-build-table\">\n");
		html.append(persistenceHandler.renderNewestBuilds(5));
		html.append("</div>");

		html.append("</div>");

		html.append("<div id='" + config.getDashboardNameEscaped()
				+ "-build-details-wrapper' class='ci-build-details-wrapper'>");

		// render details of newest build in a separate <div>
		html.append(CIDashboard.renderBuildDetails(this.config.getDashboardName(),
				this.config.getDashboardArticleTitle(),
				persistenceHandler.getCurrentBuildNumber()));

		html.append("</div></div>");

		return html.toString();
	}

	/**
	 * Renders out the wiki changes (middle column) and the test results (right
	 * column) of a selected build
	 * 
	 * @return
	 */
	public static String renderBuildDetails(String dashboardName, String dashboardArticleTitle, int selectedBuildNumber) {
	
		String dashboardNameEscaped = dashboardName;
		try {
			dashboardNameEscaped = URLEncoder.encode(dashboardName, "UTF-8");
		}
		catch (UnsupportedEncodingException e1) {
		}

		// KnowWEWikiConnector conny =
		// KnowWEEnvironment.getInstance().getWikiConnector();
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
	
		buffy.append("<h3 style=\"background-color: #CCCCCC;\">" +
				"Results of Build #" + selectedBuildNumber + "</h3>");
	
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
	
				buffy.append("</h4>\n");
	
				// Render Test Message (if existent)
				buffy.append("<span style=\"display: none;\">\n");
				s = e.getAttributeValue("message");
				if (s != null && !s.isEmpty()) {
					buffy.append(s);
				}
	
				buffy.append("</span>\n");
	
				buffy.append("</div>\n");
			}
		}
		buffy.append("</div>");
	
		// ------------------------------------------------------------------------
	
		// -------------------------------------------------------------------------
		// Render the wiki-changes in the right column
		// (ci-column-right)
		// -------------------------------------------------------------------------
	

		// buffy.append("<div id='" + dashboardName +
		// "-column-right' class='ci-column-right'>");
		//
		// // the version of the article of the selected build
		// int articleVersionSelected = 1;
		// // the version of the article of the previous build
		// int articleVersionPrevious = 1;
		//
		// // Number of build to compare to (the previous build number)
		// int previousBuildNr = selectedBuildNumber > 1 ? selectedBuildNumber -
		// 1 : 1;
		//
		// String monitoredArticleTitle = "";
		// Object attrib = handler.selectSingleNode("builds/@monitoredArticle");
		// if (attrib instanceof Attribute) {
		// monitoredArticleTitle = ((Attribute) attrib).getValue();
		// }
		// attrib = null;
		//
		// // ---------------
		//
		// // xPath to select the article version of a buildNumber
		// xPath = "builds/build[@nr=%s]/@articleVersion";
		//
		// // try to parse the selected build article version
		// attrib = handler.selectSingleNode(String.format(xPath,
		// selectedBuildNumber));
		// if (attrib instanceof Attribute) {
		// String attrValue = ((Attribute) attrib).getValue();
		// if (attrValue != null && !attrValue.isEmpty()) {
		// articleVersionSelected = Integer.parseInt(attrValue);
		// }
		// }
		// attrib = null;
		//
		// // try to parse the selected build article version
		// attrib = handler.selectSingleNode(String.format(xPath,
		// previousBuildNr));
		// if (attrib instanceof Attribute) {
		// String attrValue = ((Attribute) attrib).getValue();
		// if (attrValue != null && !attrValue.isEmpty()) {
		// articleVersionPrevious = Integer.parseInt(attrValue);
		// }
		// }
		// attrib = null;
		//
		// // buffy.append("<h4>Unterschiede zwischen <b>Build " + buildNr +
		// // "</b> (Article Version " + articleVersionSelected +
		// // ") und <b>Build " + previousBuildNr + "</b> (Article " +
		// // "Version " + articleVersionPrevious + ")</h4>");
		//
		// //
		// buffy.append("<h3 style=\"background-color: #CCCCCC;\">Differences between Build #"
		// // +
		// // selectedBuildNumber + " and Build #" + previousBuildNr + "</h3>");
		// buffy.append("<h3 style=\"background-color: #CCCCCC;\">Changes in Build #"
		// +
		// selectedBuildNumber + "</h3>");
		//
		// String author = conny.getAuthor(monitoredArticleTitle,
		// articleVersionSelected);
		//
		// if (author != null && !author.isEmpty()) {
		// buffy.append("<div id=\"last-author-changed\"><b>Last change:</b> " +
		// author + "</div>");
		// }
		//
		// xPath = "builds/build[@nr=%s]/modifiedArticles/modifiedArticle";
		// List<?> articles = handler.selectNodes(String.format(xPath,
		// selectedBuildNumber));
		// DiffEngine diff = DiffFactory.defaultDiffEngine();
		//
		// for (Object o : articles) {
		// if (o instanceof Element) {
		// Element e = (Element) o;
		// String title = e.getAttributeValue("title");
		// Integer rangeFrom =
		// Integer.parseInt(e.getAttributeValue("rangeFrom"));
		// Integer rangeTo = Integer.parseInt(e.getAttributeValue("rangeTo"));
		//
		// buffy.append("Changes in " + title + ":<br/>");
		// buffy.append(diff.makeDiffHtml(
		// conny.getArticleSource(title, rangeFrom),
		// conny.getArticleSource(title, rangeTo)));
		// }
		// }
		//
		// buffy.append(diff.makeDiffHtml(
		// conny.getArticleSource(monitoredArticleTitle,
		// articleVersionPrevious),
		// conny.getArticleSource(monitoredArticleTitle,
		// articleVersionSelected)));
		//
		// buffy.append("</div>");
	
		return buffy.toString();
	}
}
