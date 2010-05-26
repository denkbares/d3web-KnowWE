/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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

import java.util.List;

import org.jdom.Element;

import de.d3web.we.ci4ke.build.CIBuildPersistenceHandler;
import de.d3web.we.ci4ke.build.CIBuilder.CIBuildTriggers;
import de.d3web.we.ci4ke.handling.CITestResult.TestResultType;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

public class CIDashboard {

	private CIConfig config;

	public CIDashboard(Section<CIDashboardType> section) {
		this.config = (CIConfig) KnowWEUtils.getStoredObject(section,
				CIConfig.CICONFIG_STORE_KEY);
	}

	public String render() {

		KnowWERessourceLoader.getInstance().add("CI4KE.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);
		KnowWERessourceLoader.getInstance().add("CIPlugin.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);

		StringBuffer html = new StringBuffer();

		String title = config.getMonitoredArticleTitle();
		html
				.append("<div id='ci-panel' class='panel'><h3>Continuous Integration Dashboard - "
						+ title + " - Status: " + /*
												 * CIUtilities.renderResultType(builder
												 * .getCurrentBuildStatus(),24)
												 * +
												 */"</h3>\n");

		html.append("<div id='ci-content-wrapper'>");// Main content wrapper

		// Left Column: Lists all the knowledge-base Builds of the targeted
		// article
		html.append("<div id='"+config.getDashboardID()+"-column-left' class='ci-column-left'>");

		html.append("<h3 style=\"background-color: #CCCCCC;\">CurrentBuilds</h3>");			
		
		if (config.getTrigger().equals(CIBuildTriggers.onDemand)) {
			html.append("<div style=\"text-align: center;\"><form name=\"CIExecuteBuildForm\">");
			html.append("<input type=\"button\" value=\"Neuen Build Starten!\" "
							+ "name=\"submit\" class=\"button\" onclick=\"fctExecuteNewBuild('"
							+ config.getDashboardID() + "');\"/>");
			html.append("</form></div>");
		}

		// render Builds
		html.append(renderTenNewestBuilds());
		
		html.append("</div>");
		
		html.append("<div id='"+config.getDashboardID()+"-build-details-wrapper' class='ci-build-details-wrapper'>");

//		html.append("<div id='"+config.getDashboardID()+"-column-middle' class='ci-column-middle'>");

//		html.append("</div>");

//		html.append("<div id='"+config.getDashboardID()+"-column-right' class='ci-column-right'>");

//		html.append("</div>");

		html.append("</div></div></div>");

		return html.toString();
	}

	private String renderTenNewestBuilds() {
		CIBuildPersistenceHandler persi = new CIBuildPersistenceHandler(
				this.config.getDashboardID());
		StringBuffer sb = new StringBuffer(
				"<table id=\"buildList\" width=\"100%\">\n");
		List<?> builds = persi
				.selectNodes("builds/build[position() > last() - 10]");
		String s;
		for (Object o : builds) {
			if (o instanceof Element) {
				Element e = (Element) o;

				// TODO Check for null
				String buildNr = e.getAttributeValue("nr");
				sb.append("<tr onclick=\"");
				sb.append("fctGetBuildDetails('" + this.config.getDashboardID()
						+ "','" + buildNr + "');");
				sb.append("\"><td>");

				// starting with a nice image...
				s = e.getAttributeValue("result");
				if (s != null && !s.equals("")) {
					TestResultType buildResult = TestResultType.valueOf(s);
					sb.append(CIUtilities.renderResultType(buildResult, 16));
				}
				sb.append("</td><td>");
				// followed by the Build Number...
				if (buildNr != null && !buildNr.equals(""))
					sb.append("#" + buildNr);
				sb.append("</td><td>");
				// and the build date/time
				s = e.getAttributeValue("executed");
				if (s != null && !s.equals(""))
					sb.append(s);
				// close table-cell
				sb.append("</td></tr>\n");
			}
		}
		sb.append("</table>\n");
		return sb.toString();
	}

}
