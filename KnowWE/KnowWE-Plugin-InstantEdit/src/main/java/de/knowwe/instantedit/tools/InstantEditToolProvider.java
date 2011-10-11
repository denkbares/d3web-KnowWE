/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.instantedit.tools;

import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

/**
 * InstantEdit Tool.
 * 
 * @author Stefan Mark
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.06.2011
 */
public class InstantEditToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(KnowWEArticle article, Section<?> section, UserContext userContext) {
		return new Tool[] { getQuickEditPageTool(article, section, userContext) };
	}

	protected Tool getQuickEditPageTool(KnowWEArticle article, Section<?> section, UserContext userContext) {

		String jsAction = "KNOWWE.plugin.instantEdit.enable("
				+ "'"
				+ section.getID()
				+ "', KNOWWE.plugin.defaultEditTool);";
		return new DefaultEditTool(
				"KnowWEExtension/images/pencil.png",
				"Edit",
				"Edit this section",
				jsAction);
	}
}
