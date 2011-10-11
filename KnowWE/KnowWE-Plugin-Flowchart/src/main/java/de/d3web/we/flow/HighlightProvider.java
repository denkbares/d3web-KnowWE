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

import javax.servlet.http.HttpServletRequest;

import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

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
		boolean dohighlighting = false;
		// if there is no parameter, use the parameter from the map
		if (highlight == null) {
			HttpServletRequest request = userContext.getRequest();
			if (request != null) {
				Boolean temp = (Boolean) request.getSession().getAttribute(
							FlowchartRenderer.HIGHLIGHT_KEY);
				if (temp != null) {
					dohighlighting = temp.booleanValue();
				}
			}
		}
		else if (highlight.equals("true")) {
			dohighlighting = true;
		}
		if (dohighlighting) {
			String jsAction = "var url = window.location.href;" +
					"if (url.search('highlight')!=-1)" +
					"{url = url.replace(/highlight=true/g, 'highlight=false');}" +
					"else {" +
					"if (url.indexOf('?') == -1) {url += '?';}" +
					"url = url.replace(/\\?/g,'?highlight=false&');}" +
					"window.location = url;";
			return new DefaultTool(
					"KnowWEExtension/flowchart/icon/debug16.png",
					"Hide Trace",
					"Highlights active nodes and edges in the flowchart.",
					jsAction);
		}
		else {
			String jsAction = "var url = window.location.href;" +
					"if (url.search('highlight')!=-1)" +
					"{url = url.replace(/highlight=false/g, 'highlight=true');}" +
					"else {" +
					"if (url.indexOf('?') == -1) {url += '?';}" +
					"url = url.replace(/\\?/g,'?highlight=true&');}" +
					"window.location = url;";
			return new DefaultTool(
					"KnowWEExtension/flowchart/icon/debug16.png",
					"Show Trace",
					"Highlights active nodes and edges in the flowchart.",
					jsAction);
		}
	}
}
