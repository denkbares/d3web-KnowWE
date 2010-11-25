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
package de.d3web.we.flow.type;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.tools.DefaultTool;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolProvider;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * 
 * @author volker_belli
 * @created 17.11.2010
 */
public class FlowchartEditProvider implements ToolProvider {

	@Override
	public Tool[] getTools(KnowWEArticle article, Section<?> section, KnowWEUserContext userContext) {
		Tool edit = getEditTool(article, section, userContext);
		return edit != null ? new Tool[] { edit } : new Tool[] {};
	}

	private Tool getEditTool(KnowWEArticle article, Section<?> section, KnowWEUserContext userContext) {
		// tool to execute a full-parse onto the knowledge base
		// may be removed in later releases (after moneypenny)

		Section<FlowchartType> flowchart = section.findSuccessor(FlowchartType.class);

		String id = section.getID();
		String url =
				"FlowEditor.jsp?kdomID=" + id + "&" +
						KnowWEAttributes.TOPIC + "=" + userContext.getTopic();
		String winID = id.replaceAll("[^\\w]", "_");
		String jsAction = "window.open('" + url + "', '" + winID + "');";
		return new DefaultTool(
				"KnowWEExtension/flowchart/icon/edit16.png",
				"Visual Editor",
				"Opens the visual editor for this flowchart to edit its content grapically.",
				jsAction);
	}

}
