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

import de.d3web.we.ci4ke.util.CIUtilities;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.tools.DefaultTool;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolProvider;
import de.d3web.we.user.UserContext;

/**
 * A provider for adding a "Start a new build"-Tool for the CIDashboardType
 *
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 01.12.2010
 */
public class CIDashboardToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(KnowWEArticle article, Section<?> section, UserContext userContext) {
		String dashboardName = DefaultMarkupType.getAnnotation(section, CIDashboardType.NAME_KEY);
		return new Tool[] { getStartNewBuildTool(dashboardName) };
	}

	protected Tool getStartNewBuildTool(String dashboardName) {
		// Tool which starts a new build
		String jsAction = "fctExecuteNewBuild('" + CIUtilities.utf8Escape(dashboardName) + "')";
		return new DefaultTool(
				"KnowWEExtension/ci4ke/images/16x16/clock.png",
				"Start a new build",
				"Starts a new build.",
				jsAction);
	}
}
