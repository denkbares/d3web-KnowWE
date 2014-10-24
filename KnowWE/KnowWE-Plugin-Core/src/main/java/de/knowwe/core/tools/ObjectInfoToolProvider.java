/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.tools;

import java.util.ResourceBundle;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;
import de.knowwe.core.taghandler.ObjectInfoTagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

/**
 * ToolProvider for the ObjectInfoTagHandler.
 * 
 * @see ObjectInfoTagHandler
 * 
 * @author Sebastian Furth
 * @created 01/12/2010
 */
public class ObjectInfoToolProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		Tool homepage = getCreateHomepageTool();
		return new Tool[] { homepage };
	}

	protected Tool getCreateHomepageTool() {
		String jsAction = "KNOWWE.core.plugin.objectinfo.createHomePage()";
		ResourceBundle rb = Messages.getMessageBundle();
		return new DefaultTool(
				"KnowWEExtension/images/new_file.gif",
				rb.getString("KnowWE.ObjectInfoTagHandler.newPage"),
				rb.getString("KnowWE.ObjectInfoTagHandler.newPageDetail"),
				jsAction,
				Tool.CATEGORY_INFO);
	}

}
