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

package de.d3web.we.ci4ke.action;

import java.io.IOException;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.ci4ke.build.CIBuildPersistenceHandler;
import de.d3web.we.ci4ke.build.CIBuilder;
import de.d3web.we.ci4ke.diff.DiffEngine;
import de.d3web.we.ci4ke.diff.DiffFactory;
import de.d3web.we.ci4ke.handling.CITestResult.TestResultType;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

public class CIAction extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {

		// Logger.getLogger(this.getClass().getName()).info(
		// ">>> execute Action angekommen! >>>");

		String task = String.valueOf(context.getParameter("task"));
		String dashboardID = String.valueOf(context.getParameter("id"));
		String topic = context.getKnowWEParameterMap().getTopic();

		if (task.equals("null") || dashboardID.equals("null")) throw new IOException(
				"CIAction.execute(): Required parameters not set!");

		StringBuffer buffy = new StringBuffer();

		if (task.equals("executeNewBuild")) {

			// Logger.getLogger(this.getClass().getName()).log(Level.INFO,
			// ">>> executeNewBuild angekommen! >>>");

			CIBuilder builder = new CIBuilder(topic, dashboardID);
			builder.executeBuild();

			CIBuildPersistenceHandler handler = new CIBuildPersistenceHandler(dashboardID);
			buffy.append(handler.renderNewestBuilds(5));

		}// Get the details of one build (wiki changes + test results)
		else if (task.equals("getBuildDetails")) {

			int selectedBuildNumber = Integer.parseInt(context.getParameter("nr"));
			buffy.append(renderBuildDetails(dashboardID, selectedBuildNumber));

		}
		else if (task.equals("refreshBuildList")) {

			int indexFromBack = Integer.parseInt(context.getParameter("indexFromBack"));
			int numberOfBuilds = Integer.parseInt(context.getParameter("numberOfBuilds"));

			CIBuildPersistenceHandler handler = new CIBuildPersistenceHandler(dashboardID);
			context.getWriter().write(handler.renderBuildList(indexFromBack, numberOfBuilds));
		}
		else {
			buffy.append("@info@CIAction says: Hello World!");
		}

		context.getWriter().write(buffy.toString());
	}

	/**
	 * Renders out the wiki changes (middle column) and the test results (right
	 * column) of a selected build
	 * 
	 * @return
	 */
	public static String renderBuildDetails(String dashboardID, int selectedBuildNumber) {

		CIBuildPersistenceHandler handler = new CIBuildPersistenceHandler(dashboardID);
		StringBuffer buffy = new StringBuffer();

		// -------------------------------------------------------------------------
		// At First: Render the wiki-changes in the middle column
		// (ci-column-middle)
		// -------------------------------------------------------------------------

		buffy.append("<div id='" + dashboardID + "-column-middle' class='ci-column-middle'>");

		// the version of the article of the selected build
		int articleVersionSelected = 1;
		// the version of the article of the previous build
		int articleVersionPrevious = 1;

		// Number of build to compare to (the previous build number)
		int previousBuildNr = selectedBuildNumber > 1 ? selectedBuildNumber - 1 : 1;

		String monitoredArticleTitle = "";
		Object attrib = handler.selectSingleNode("builds/@monitoredArticle");
		if (attrib instanceof Attribute) {
			monitoredArticleTitle = ((Attribute) attrib).getValue();
		}
		attrib = null;

		// xPath to select the article version of a buildNumber
		String xPath = "builds/build[@nr=%s]/@articleVersion";

		// try to parse the selected build article version
		attrib = handler.selectSingleNode(String.format(xPath, selectedBuildNumber));
		if (attrib instanceof Attribute) {
			String attrValue = ((Attribute) attrib).getValue();
			if (attrValue != null && !attrValue.isEmpty()) articleVersionSelected = Integer.parseInt(attrValue);
		}
		attrib = null;

		// try to parse the selected build article version
		attrib = handler.selectSingleNode(String.format(xPath, previousBuildNr));
		if (attrib instanceof Attribute) {
			String attrValue = ((Attribute) attrib).getValue();
			if (attrValue != null && !attrValue.isEmpty()) articleVersionPrevious = Integer.parseInt(attrValue);
		}
		attrib = null;

		// buffy.append("<h4>Unterschiede zwischen <b>Build " + buildNr +
		// "</b> (Article Version " + articleVersionSelected +
		// ") und <b>Build " + previousBuildNr + "</b> (Article " +
		// "Version " + articleVersionPrevious + ")</h4>");
		buffy.append("<h3 style=\"background-color: #CCCCCC;\">Differences between Build #" +
				selectedBuildNumber + " and Build #" + previousBuildNr + "</h3>");

		KnowWEWikiConnector conny = KnowWEEnvironment.getInstance().getWikiConnector();
		DiffEngine diff = DiffFactory.defaultDiffEngine();
		buffy.append(diff.makeDiffHtml(
						conny.getArticleSource(monitoredArticleTitle,
								articleVersionPrevious),
						conny.getArticleSource(monitoredArticleTitle,
								articleVersionSelected)));

		buffy.append("</div>");

		// ------------------------------------------------------------------------
		// At Second: Render the build details in the right colum
		// (ci-column-right)
		// ------------------------------------------------------------------------

		buffy.append("<div id='" + dashboardID + "-column-right' class='ci-column-right'>");

		xPath = "builds/build[@nr=%s]/test";
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
				if (s != null && !s.isEmpty()) buffy.append(s);

				buffy.append("</h4>\n");

				// Render Test Message (if existent)
				buffy.append("<table style=\"display: none;\">\n");
				s = e.getAttributeValue("message");
				if (s != null && !s.isEmpty()) {
					buffy.append("<tr><td>Result Message: " + s + "</td></tr>");
				}

				buffy.append("</table>\n");

				buffy.append("</div>\n");
			}
		}
		buffy.append("</table></div>");

		// ------------------------------------------------------------------------

		return buffy.toString();
	}
}
