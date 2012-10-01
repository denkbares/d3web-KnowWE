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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.diaFlux.flow.DiaFluxElement;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.Node;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * 
 * @author Reinhard Hatko
 * @created 22.08.2012
 */
public class Highlight {

	private final Flow flow;
	private final String prefix;
	private final Map<Edge, Map<String, String>> edges;
	private final Map<Node, Map<String, String>> nodes;
	public static final String EMPTY_HIGHLIGHT = "<flow></flow>";
	public static final String TOOL_TIP = "title";
	public static final String CSS_STYLE = "style";
	public static final String CSS_CLASS = "class";

	public Highlight(Flow flow, String prefix) {
		this.flow = flow;
		this.prefix = prefix;
		this.edges = new HashMap<Edge, Map<String, String>>();
		this.nodes = new HashMap<Node, Map<String, String>>();
	}

	public Flow getFlow() {
		return flow;
	}

	public void add(Edge edge, String key, String value) {
		putValue(edges, edge, key, value);
	}

	public void add(Node node, String key, String value) {
		putValue(nodes, node, key, value);
	}

	private <T extends DiaFluxElement> void createHighlights(StringBuilder bob, Map<T, Map<String, String>> objects, String objectType) {
		for (T element : objects.keySet()) {
			bob.append("<" + objectType + " id='");
			bob.append(element.getID());
			bob.append("' ");

			Map<String, String> values = objects.get(element);
			for (String key : values.keySet()) {
				bob.append(key);
				bob.append("='");
				bob.append(values.get(key));
				bob.append("' ");
			}
			bob.append(">");
			bob.append("</" + objectType + ">\r");
		}

	}

	private <T> void putValue(Map<T, Map<String, String>> map, T object, String key, String value) {
		Map<String, String> values = map.get(object);
		if (values == null) {
			values = new HashMap<String, String>();
			map.put(object, values);
		}
		values.put(key, KnowWEUtils.escapeHTML(value));
	}

	private void appendHeader(StringBuilder bob) {
		bob.append("<flow id='");
		bob.append(FlowchartUtils.escapeHtmlId(getFlow().getName()));
		bob.append("' prefix ='" + prefix + "'>\r");

	}

	private void appendFooter(StringBuilder bob) {
		bob.append("</flow>\r");
		bob.append("\r");

	}

	private String getXML() {
		StringBuilder bob = new StringBuilder();
		appendHeader(bob);
		createHighlights(bob, edges, "edge");
		createHighlights(bob, nodes, "node");
		appendFooter(bob);
		return bob.toString();
	}

	public void removeClassFromRemainingNodes() {
		List<Edge> remainingEdges = new ArrayList<Edge>(flow.getEdges());
		List<Node> remainingNodes = new ArrayList<Node>(flow.getNodes());
		remainingEdges.removeAll(edges.keySet());
		remainingNodes.removeAll(nodes.keySet());

		// clear classes on all remaining nodes and edges
		for (Node node : remainingNodes) {
			putValue(nodes, node, Highlight.CSS_CLASS, "");
		}

		for (Edge edge : remainingEdges) {
			putValue(edges, edge, Highlight.CSS_CLASS, "");
		}
	}

	public String getPrefix() {
		return prefix;
	}

	public void write(UserActionContext context) throws IOException {
		Highlight.write(context, getXML());
	}

	public static void write(UserActionContext context, String output) throws IOException {
		context.setContentType("text/xml");
		context.getWriter().write(output);

	}

	public static void writeEmpty(UserActionContext context) throws IOException {
		write(context, EMPTY_HIGHLIGHT);
	}

}
