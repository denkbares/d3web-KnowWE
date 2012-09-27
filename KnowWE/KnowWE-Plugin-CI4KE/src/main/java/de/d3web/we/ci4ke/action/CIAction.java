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
import java.util.Collection;

import de.d3web.testing.BuildResult;
import de.d3web.testing.Message.Type;
import de.d3web.we.ci4ke.build.CIBuilder;
import de.d3web.we.ci4ke.build.CIDashboard;
import de.d3web.we.ci4ke.build.CIRenderer;
import de.d3web.we.ci4ke.handling.CIDashboardType;
import de.d3web.we.ci4ke.util.CIUtils;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.Strings;

public class CIAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String task = String.valueOf(context.getParameter("task"));

		String dashboardName = String.valueOf(context.getParameter("name"));
		dashboardName = Strings.decodeURL(dashboardName);

		String topic = context.getTitle();
		String web = context.getWeb();
		int selectedBuildNumber = -1;
		if (context.getParameter("nr") != null) {
			try {
				selectedBuildNumber = Integer.parseInt(context.getParameter("nr"));
			}
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		if (task.equals("null") || dashboardName.equals("null")) {
			throw new IOException(
					"CIAction.execute(): Required parameters not set!");
		}

		String dashBoardArticle = null;
		Collection<Section<CIDashboardType>> dashboardSections = CIUtils.findCIDashboardSection(dashboardName);
		if (dashboardSections != null && dashboardSections.size() > 0) {
			// there can only be one dashboard with this name
			dashBoardArticle = dashboardSections.iterator().next().getTitle();
		}
		CIDashboard dashboard = CIDashboard.getDashboard(web, dashBoardArticle, dashboardName);
		CIRenderer renderer = dashboard.getRenderer();

		String html = null;
		if (task.equals("executeNewBuild")) {
			if (CIUtils.buildRunning(dashboardName)) {
				context.sendError(409, "<message will be inserted in JS>");
				// NOTE: on current ajax handling this message text will will
				// not be
				// shown.
				// but a list mapping error codes to message texts is managed in
				// JS
				return;
			}
			final CIBuilder builder = new CIBuilder(web, topic, dashboardName);
			new Thread(new Runnable() {

				@Override
				public void run() {
					builder.executeBuild();
				}
			}).start();
			try {
				// give some time to initialize build process before rendering
				// it
				Thread.sleep(10);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BuildResult build = dashboard.getBuild(selectedBuildNumber);
			html = renderer.renderDashboardHeader(build);

		}// Get the details of one build (wiki changes + test results)
		else if (task.equals("refreshBuildDetails")) {
			BuildResult build = dashboard.getBuild(selectedBuildNumber);
			html = renderer.renderBuildDetails(build);
		}
		else if (task.equals("refreshBuildStatus")) {
			BuildResult build = dashboard.getLatestBuild();
			html = renderer.renderDashboardHeader(build);
		}
		else if (task.equals("refreshBubble")) {
			BuildResult build = dashboard.getLatestBuild();
			Type overallResult = build.getOverallResult();
			html = renderer.renderBuildStatus(overallResult, true);
		}
		else if (task.equals("refreshBuildList")) {
			String indexFromBackParam = context.getParameter("indexFromBack");
			String numberOfBuildsParameter = context.getParameter("numberOfBuilds");
			int indexFromBack = indexFromBackParam == null
					? 0
					: Integer.parseInt(indexFromBackParam);
			int numberOfBuilds = numberOfBuildsParameter == null
					? -1
					: Integer.parseInt(numberOfBuildsParameter);
			html = renderer.renderBuildList(indexFromBack, numberOfBuilds, selectedBuildNumber);
		}

		// ensure jspwiki markup is rendered in similar way as on full page
		// reload
		html = Environment.getInstance().getWikiConnector().renderWikiSyntax(
				Strings.maskHTML(html), context.getRequest());
		html = Strings.unmaskHTML(html);

		context.setContentType("text/html; charset=UTF-8");
		context.getWriter().write(html);
	}
}
