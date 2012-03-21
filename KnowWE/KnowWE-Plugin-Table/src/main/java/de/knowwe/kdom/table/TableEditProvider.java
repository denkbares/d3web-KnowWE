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
package de.knowwe.kdom.table;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

/**
 * 
 * @author Alexander Strehler
 * @created 01.12.2011
 */
public class TableEditProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		// edit the content of the markup, without %%Table and Annotations
		Section<Table> content = Sections.findSuccessor(section, Table.class);
		if (content == null) {
			return new Tool[0];
		}
		else {

			String editprovider = getEditProviderCall();
			String jsAction = "KNOWWE.plugin.instantEdit.enable("
					+ "'"
					+ content.getID()
					+ "',new KNOWWE.table.edit.Editor(" + editprovider + "));";
			return new Tool[] { new DefaultTool(
					"KnowWEExtension/images/pencil.png",
					"Edit Table",
					"Edit this table",
					jsAction) };
		}

	}

	protected String getEditProviderCall() {
		return "KNOWWE.table.edit.defaultProvider";
	}

}
