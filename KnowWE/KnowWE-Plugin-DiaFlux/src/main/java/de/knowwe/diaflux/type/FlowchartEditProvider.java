/*
 * Copyright (C) 2010 denkbares GmbH, Germany
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
package de.knowwe.diaflux.type;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;
import de.knowwe.util.Icon;

/**
 * @author volker_belli
 * @created 17.11.2010
 */
public class FlowchartEditProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return KnowWEUtils.canWrite(section, userContext);
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		if (!KnowWEUtils.canWrite(section, userContext)) {
			return ToolUtils.emptyToolArray();
		}
		Tool edit = getEditTool(section, userContext);
		return new Tool[] { edit };
	}

	private Tool getEditTool(Section<?> section, UserContext userContext) {
		String jsAction = createEditAction(section, userContext);

		return new DefaultTool(
				Icon.EDIT,
				"Visual Editor",
				"Opens the visual editor for this flowchart.",
				jsAction,
				Tool.CATEGORY_EDIT);
	}

	private static String createEditAction(Section<?> section, UserContext userContext) {
		String id = section.getID();
		String url = "DiaFluxEditor.jsp?kdomID=" + id;
		String winID = id.replaceAll("[^\\w]", "_");
		String jsAction = "window.open('" + url + "', '" + winID + "');";
		return jsAction;
	}

	/**
	 * Creates a JS-Edit link for a DiaFlux-Section.
	 *
	 * @created 29.11.2010
	 */
	public static String createEditLink(Section<?> section, UserContext userContext) {
		return "javascript:" + createEditAction(section, userContext) + "undefined;";
	}

}
