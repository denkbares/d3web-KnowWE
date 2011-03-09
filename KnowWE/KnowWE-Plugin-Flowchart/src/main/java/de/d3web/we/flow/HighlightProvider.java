/*
 * Copyright (C) 2011 denkbares GmbH
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
package de.d3web.we.flow;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.tools.DefaultTool;
import de.d3web.we.tools.Tool;
import de.d3web.we.tools.ToolProvider;
import de.d3web.we.user.UserContext;

/**
 * Enables highlighting of active nodes and edges
 *
 * @author Markus Friedrich (denkbares GmbH)
 * @created 23.02.2011
 */
public class HighlightProvider implements ToolProvider {

	@Override
	public Tool[] getTools(KnowWEArticle article, Section<?> section, UserContext userContext) {
		Tool refresh = getHighlightTool(article, section, userContext);
		return new Tool[] { refresh };
	}

	protected Tool getHighlightTool(KnowWEArticle article, Section<?> section, UserContext userContext) {
		String highlight = userContext.getParameters().get("highlight");
		if (highlight != null && highlight.equals("true")) {
			String jsAction = "var url = window.location.href;" +
					"url = url.replace(/&highlight=true/g, '');" +
					"window.location = url;";
			return new DefaultTool(
					"KnowWEExtension/flowchart/icon/debug16.png",
					"Hide Trace",
					"Highlights active nodes and edges in the flowchart.",
					jsAction);
		}
		else {
			String jsAction = "var url = window.location.href;" +
					"if (url.indexOf('?') == -1) {url += '?';}" +
					"url += '&highlight=true';" +
					"window.location = url;";
			return new DefaultTool(
					"KnowWEExtension/flowchart/icon/debug16.png",
					"Show Trace",
					"Highlights active nodes and edges in the flowchart.",
					jsAction);
		}
	}

}
