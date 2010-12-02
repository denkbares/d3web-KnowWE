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
import java.net.URLDecoder;

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.ci4ke.build.CIBuildPersistenceHandler;
import de.d3web.we.ci4ke.build.CIBuilder;
import de.d3web.we.ci4ke.handling.CIDashboardRenderer;

public class CIAction extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {

		String task = String.valueOf(context.getParameter("task"));

		String dashboardName = String.valueOf(context.getParameter("id"));
		dashboardName = URLDecoder.decode(dashboardName, "UTF-8");

		String topic = context.getKnowWEParameterMap().getTopic();

		if (task.equals("null") || dashboardName.equals("null")) {
			throw new IOException(
					"CIAction.execute(): Required parameters not set!");
		}

		StringBuffer buffy = new StringBuffer("");

		if (task.equals("executeNewBuild")) {

			CIBuilder builder = new CIBuilder(topic, dashboardName);
			builder.executeBuild();
			buffy.append(CIDashboardRenderer.renderDashboardContents(dashboardName, topic));

		}// Get the details of one build (wiki changes + test results)
		else if (task.equals("getBuildDetails")) {

			int selectedBuildNumber = Integer.parseInt(context.getParameter("nr"));
			buffy.append(CIDashboardRenderer.renderBuildDetails(dashboardName, topic, selectedBuildNumber));

		}
		else if (task.equals("refreshBuildList")) {

			int indexFromBack =
					Integer.parseInt(context.getParameter("indexFromBack"));
			int numberOfBuilds =
					Integer.parseInt(context.getParameter("numberOfBuilds"));

			CIBuildPersistenceHandler handler = CIBuildPersistenceHandler.getHandler(dashboardName, topic);
			buffy.append(handler.renderBuildList(indexFromBack, numberOfBuilds));
		}

		context.setContentType("text/html; charset=UTF-8");
		context.getWriter().write(buffy.toString());
	}
}
