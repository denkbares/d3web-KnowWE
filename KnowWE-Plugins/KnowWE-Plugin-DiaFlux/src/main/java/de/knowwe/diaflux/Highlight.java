/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.diaflux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.d3web.diaFlux.flow.DiaFluxElement;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.Node;
import de.d3web.strings.Strings;
import de.knowwe.core.action.UserActionContext;

/**
 * 
 * @author Reinhard Hatko
 * @created 22.08.2012
 */
public class Highlight {

	private final String parentId;
	private final String prefix;
	private final Map<String, Map<String, String>> edges;
	private final Map<String, Map<String, String>> nodes;
	public static final String EMPTY_HIGHLIGHT = "<flow></flow>";
	public static final String TOOL_TIP = "title";
	public static final String CSS_STYLE = "style";
	public static final String CSS_CLASS = "class";

	public Highlight(String parentId, String prefix) {
		this.parentId = parentId;
		this.prefix = prefix;
		this.edges = new HashMap<>();
		this.nodes = new HashMap<>();
	}

	public void add(Edge edge, String key, String value) {
		addEdge(edge.getID(), key, value);
	}

	public void addEdge(String id, String key, String value) {
		putValue(edges, id, key, value);
	}

	public void add(Node node, String key, String value) {
		addNode(node.getID(), key, value);
	}

	public void addNode(String id, String key, String value) {
		putValue(nodes, id, key, value);

	}

	private void createHighlights(StringBuilder bob, Map<String, Map<String, String>> objects, String objectType) {
		for (String element : objects.keySet()) {
			bob.append("<").append(objectType).append(" id='");
			bob.append(element);
			bob.append("' ");

			Map<String, String> values = objects.get(element);
			for (String key : values.keySet()) {
				bob.append(key);
				bob.append("='");
				bob.append(values.get(key));
				bob.append("' ");
			}
			bob.append(">");
			bob.append("</").append(objectType).append(">\r");
		}

	}

	private <T> void putValue(Map<T, Map<String, String>> map, T object, String key, String value) {
		Map<String, String> values = map.get(object);
		if (values == null) {
			values = new HashMap<>();
			map.put(object, values);
		}
		values.put(key, Strings.encodeHtml(value));
	}

	private void appendHeader(StringBuilder bob) {
		bob.append("<flow id='");
		bob.append(parentId);
		bob.append("' cssprefix ='").append(prefix).append("'>\r");

	}

	private void appendFooter(StringBuilder bob) {
		bob.append("</flow>\r");
	}

	public String getXML() {
		StringBuilder bob = new StringBuilder();
		appendHeader(bob);
		createHighlights(bob, edges, "edge");
		createHighlights(bob, nodes, "node");
		appendFooter(bob);
		return bob.toString();
	}

	public void removeClassFromRemainingNodes(Flow flow) {
		Collection<String> remainingNodes = getRemainingIds(flow.getNodes(), nodes.keySet());
		Collection<String> remainingEdges = getRemainingIds(flow.getEdges(), edges.keySet());

		// clear classes on all remaining nodes and edges
		for (String node : remainingNodes) {
			addNode(node, Highlight.CSS_CLASS, "");
		}

		for (String edge : remainingEdges) {
			addEdge(edge, Highlight.CSS_CLASS, "");
		}
	}

	Collection<String> getRemainingIds(Collection<? extends DiaFluxElement> elements, Collection<String> ids) {
		Collection<String> result = new ArrayList<>(elements.size());

		for (DiaFluxElement element : elements) {
			result.add(element.getID());
		}
		result.removeAll(ids);
		return result;

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
