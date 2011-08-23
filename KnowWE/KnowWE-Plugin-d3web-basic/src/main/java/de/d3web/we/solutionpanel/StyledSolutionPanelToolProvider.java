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

import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.tools.DefaultTool;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolProvider;
import de.d3web.we.user.UserContext;

/**
 * A tool provider to add some appropriate actions to the
 * {@link ShowSolutionsRenderer}, e.g., clear case, refresh.
 * 
 * @author Joachim Baumeister (denkbares GmbH)
 * @created 19.10.2010
 */
public class StyledSolutionPanelToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(KnowWEArticle article, Section<?> section, UserContext userContext) {
		Tool clearSession = getSessionTool(article, section, userContext);
		return new Tool[] { clearSession };
	}

	protected Tool getSessionTool(KnowWEArticle article, Section<?> section, UserContext userContext) {
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(userContext);

		// +
		// "<img src='KnowWEExtension/images/progress_stop.gif' id='sstate-clear' class='pointer' title='"
		String label = rb.getString("KnowWE.Solutions.clear");

		String jsAction = "";

		return new DefaultTool(
				"KnowWEExtension/d3web/icon/refresh16.png",
				label,
				"Resets the values of the current session.",
				jsAction);
	}

}
