/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.diaflux;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.d3web.diaFlux.flow.DiaFluxElement;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.Node;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Utility methods to create highlights in flowcharts
 * 
 * @author Reinhard Hatko
 * @created 20.08.2012
 */
public class Highlights {

	public static final String CSS_CLASS = "class";
	public static final String TOOL_TIP = "title";
	public static final String EMPTY_HIGHLIGHT = "<flow></flow>";

	public static void appendFooter(StringBuilder builder) {
		builder.append("</flow>\r");
		builder.append("\r");

	}

	public static void appendHeader(StringBuilder builder, String flowName, String prefix) {

		builder.append("<flow id='");
		builder.append(flowName);
		builder.append("' prefix ='" + prefix + "'>\r");

	}

	public static void addEdgeHighlight(StringBuilder builder, Map<Edge, Map<String, String>> edges) {

		addHighlight(builder, edges, "edge");
	}

	public static void addNodeHighlight(StringBuilder builder, Map<Node, Map<String, String>> nodes) {

		addHighlight(builder, nodes, "node");
	}

	private static <T extends DiaFluxElement> void addHighlight(StringBuilder builder, Map<T, Map<String, String>> objects, String objectType) {
		for (T element : objects.keySet()) {
			builder.append("<" + objectType + " id='");
			builder.append(element.getID());
			builder.append("' ");

			Map<String, String> values = objects.get(element);
			for (String key : values.keySet()) {
				builder.append(key);
				builder.append("='");
				builder.append(values.get(key));
				builder.append("' ");
			}
			builder.append(">");
			builder.append("</" + objectType + ">\r");
		}

	}

	public static <T> void putValue(Map<T, Map<String, String>> map, T object, String key, String value) {
		Map<String, String> values = map.get(object);
		if (values == null) {
			values = new HashMap<String, String>();
			map.put(object, values);
		}
		values.put(key, KnowWEUtils.escapeHTML(value));
	}

	public static void write(UserActionContext context, String output) throws IOException {
		context.setContentType("text/xml");
		context.getWriter().write(output);
	}

}
