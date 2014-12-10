/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.d3web.we.solutionpanel;

import java.util.ResourceBundle;

import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * A tool provider to add some appropriate actions to the
 * {@link ShowSolutionsRenderer}, e.g., clear case, refresh.
 *
 * @author Joachim Baumeister (denkbares GmbH)
 * @created 19.10.2010
 */
public class StyledSolutionPanelToolProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		Tool clearSession = getSessionTool(section, userContext);
		return new Tool[] { clearSession };
	}

	protected Tool getSessionTool(Section<?> section, UserContext userContext) {
		ResourceBundle rb = D3webUtils.getD3webBundle(userContext);

		// +
		// "<img src='KnowWEExtension/images/progress_stop.gif' id='sstate-clear' class='pointer' title='"
		String label = rb.getString("KnowWE.Solutions.clear");

		String jsAction = "";

		return new DefaultTool(
				Icon.REFRESH,
				label,
				"Resets the values of the current session.",
				jsAction,
				Tool.CATEGORY_EXECUTE);
	}

}
