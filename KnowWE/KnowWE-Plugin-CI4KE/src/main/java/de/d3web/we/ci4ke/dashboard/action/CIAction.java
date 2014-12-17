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

package de.d3web.we.ci4ke.dashboard.action;

import java.io.IOException;

import de.d3web.strings.Strings;
import de.d3web.testing.BuildResult;
import de.d3web.testing.Message.Type;
import de.d3web.utils.Log;
import de.d3web.we.ci4ke.build.CIBuildManager;
import de.d3web.we.ci4ke.build.CIRenderer;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.util.Icon;

public class CIAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String task = String.valueOf(context.getParameter("task"));

		String dashboardName = String.valueOf(context.getParameter("name"));
		dashboardName = Strings.decodeURL(dashboardName);
		if (task.equals("null") || dashboardName.equals("null")) {
			throw new IOException(
					"CIAction.execute(): Required parameters not set!");
		}
		String web = context.getWeb();

		CIDashboard dashboard = CIDashboardManager.getDashboard(
				KnowWEUtils.getArticleManager(web),
				dashboardName);
		int selectedBuildNumber = -1;
		if (context.getParameter("nr") != null) {
			try {
				selectedBuildNumber = Integer.parseInt(context.getParameter("nr"));
			}
			catch (NumberFormatException e) {
				Log.warning(e.toString());
			}
		}
		if (selectedBuildNumber < 1) {
			selectedBuildNumber = dashboard.getLatestBuildNumber();
		}

		CIRenderer renderer = dashboard.getRenderer();

		RenderResult html = new RenderResult(context);
		if (task.equals("executeNewBuild")) {
			if (CIBuildManager.isRunning(dashboard)) {
				context.sendError(409, "<message will be inserted in JS>");
				// NOTE: on current ajax handling this message text will will
				// not be shown. but a list mapping error codes to message texts
				// is managed in JS
				return;
			}
			CIBuildManager.startBuild(CIDashboardManager.getDashboard(
					KnowWEUtils.getArticleManager(web),
					dashboardName));
			// TODO: Why are we rendering the old build? Necessary?
			BuildResult build = dashboard.getBuild(selectedBuildNumber);
			renderer.renderDashboardHeader(build, html);

		}// Get the details of one build (wiki changes + test results)
		else if (task.equals("refreshBuildDetails")) {
			BuildResult build = dashboard.getBuild(selectedBuildNumber);
			renderer.renderBuildDetails(context.getWeb(), build, html);
		}
		else if (task.equals("refreshBuildStatus")) {
			BuildResult build = dashboard.getLatestBuild();
			renderer.renderDashboardHeader(build, html);
		}
		else if (task.equals("refreshBubble")) {
			BuildResult build = dashboard.getLatestBuild();
			if (build != null) {
				Type overallResult = build.getOverallResult();
				renderer.renderBuildStatus(overallResult, true, Icon.BULB, html);
			}
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
			renderer.renderBuildList(indexFromBack, numberOfBuilds, selectedBuildNumber, html);
		}

		// ensure jspwiki markup is rendered in similar way as on full page
		// reload
		RenderResult temp = new RenderResult(html);
		temp.append(Environment.getInstance().getWikiConnector().renderWikiSyntax(
				html.toStringRaw(),
				context.getRequest()));

		context.setContentType("text/html; charset=UTF-8");
		context.getWriter().write(temp.toString());
	}
}
