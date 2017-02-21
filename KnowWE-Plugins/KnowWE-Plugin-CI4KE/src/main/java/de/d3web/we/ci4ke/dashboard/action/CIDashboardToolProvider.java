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
package de.d3web.we.ci4ke.dashboard.action;

import com.denkbares.strings.Strings;
import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * A provider for adding a "Start a new build"-Tool for the CIDashboardType
 *
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 01.12.2010
 */
public class CIDashboardToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		String dashboardName = DefaultMarkupType.getAnnotation(section,
				CIDashboardType.NAME_KEY);

		if (dashboardName == null) {
			return new Tool[0];
		}
		else {
			return new Tool[] { getStartNewBuildTool(dashboardName, section.getTitle()),
			getReportDownloadTool(dashboardName, section.getTitle())};
		}
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return DefaultMarkupType.getAnnotation(section, CIDashboardType.NAME_KEY) != null;
	}

	public static Tool getReportDownloadTool(String dashboardName, String title) {

		String jsAction = "action/CIReportDownloadAction" +
				"?name=" + dashboardName;
		// assemble download tool
		return new DefaultTool(
				Icon.DOWNLOAD,
				"Download as HTML",
				"Download current build result as HTML.",
				jsAction,
				Tool.ActionType.HREF,
				Tool.CATEGORY_DOWNLOAD);
	}

	public static Tool getStartNewBuildTool(String dashboardName, String title) {
		// Tool which starts a new build
		String jsAction = "_CI.executeNewBuild('" + Strings.encodeURL(dashboardName) + "','"
				+ title + "')";
		return new DefaultTool(
				Icon.CLOCK,
				"Start a new build",
				"Starts a new build.",
				jsAction, Tool.CATEGORY_EXECUTE);
	}
}
