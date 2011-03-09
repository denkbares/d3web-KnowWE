/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.toolprovider;

import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.tools.DefaultTool;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolProvider;
import de.d3web.we.user.UserContext;
import de.knowwe.core.taghandler.ObjectInfoTagHandler;

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
	public Tool[] getTools(KnowWEArticle article, Section<?> section, UserContext userContext) {

		Tool homepage = getCreateHomepageTool(article.getTitle(), article.getWeb(), section.getID());
		return new Tool[] { homepage };
	}

	protected Tool getCreateHomepageTool(String topic, String web, String id) {
		String jsAction = "KNOWWE.core.plugin.objectinfo.createHomePage()";
		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle();
		return new DefaultTool(
				"KnowWEExtension/images/new_file.gif",
				rb.getString("KnowWE.ObjectInfoTagHandler.newPage"),
				rb.getString("KnowWE.ObjectInfoTagHandler.newPageDetail"),
				jsAction);
	}


}
