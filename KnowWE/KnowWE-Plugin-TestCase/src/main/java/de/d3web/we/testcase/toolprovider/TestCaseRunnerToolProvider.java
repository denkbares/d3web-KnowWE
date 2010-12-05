/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.testcase.toolprovider;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.testcase.kdom.TestCaseRunnerType;
import de.d3web.we.tools.DefaultTool;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolProvider;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * ToolProvider which provides some download links for the TestCaseResultType.
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created 25/10/2010
 */
public class TestCaseRunnerToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(KnowWEArticle article, Section<?> section, KnowWEUserContext userContext) {
		String topic = TestCaseRunnerType.getTestCase(section);
		Tool downloadPDF = getDownloadPDFTool(topic, article.getWeb(), section.getID());
		Tool downloadGraphViz = getDownloadGraphVizTool(topic, article.getWeb(),
				section.getID());
		Tool refresh = getRefreshTool();
		return new Tool[] {
				downloadPDF, downloadGraphViz, refresh };
	}

	protected Tool getRefreshTool() {
		// Tool which enables rerunning the test suite
		String jsAction = "runTestSuite()";
		return new DefaultTool(
				"KnowWEExtension/d3web/icon/refresh16.png",
				"Re-Run",
				"Re-Runs the test suite.",
				jsAction);
	}

	protected Tool getDownloadPDFTool(String topic, String web, String id) {
		// tool to provide download capability
		String jsAction = "window.location='action/TestCaseServlet?type=visualization&KWiki_Topic="
				+ topic + "&web=" + web + "&filename=" + topic.replaceAll(" ", "_")
				+ "_Visualization.pdf'";
		return new DefaultTool(
				"KnowWEExtension/images/pdf.png",
				"Download PDF",
				"Download the test suite visualized in a pdf file.",
				jsAction);
	}

	protected Tool getDownloadGraphVizTool(String topic, String web, String id) {
		// tool to provide download capability
		String jsAction = "window.location='action/TestCaseServlet?type=visualization&KWiki_Topic="
				+ topic + "&web=" + web + "&filename=" + topic.replaceAll(" ", "_")
				+ "_Visualization.dot'";
		return new DefaultTool(
				"KnowWEExtension/images/txt.png",
				"Download GraphViz",
				"Download the test suite visualized in a GraphViz (.dot) file.",
				jsAction);
	}

}
