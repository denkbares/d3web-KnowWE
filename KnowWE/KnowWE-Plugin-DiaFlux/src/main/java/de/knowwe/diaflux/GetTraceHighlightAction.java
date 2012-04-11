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
package de.knowwe.diaflux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.DiaFluxElement;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowRun;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.inference.DiaFluxTrace;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.diaFlux.inference.FluxSolver;
import de.d3web.we.basic.SessionProvider;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.diaflux.type.DiaFluxType;
import de.knowwe.diaflux.type.FlowchartType;

/**
 * 
 * @author Reinhard Hatko
 * @created 08.06.2011
 */
public class GetTraceHighlightAction extends AbstractAction {

	private static final String PREFIX = "trace";
	private static final String TRACE_ACTIVE_CLASS = PREFIX + "Active";
	private static final String TRACE_SNAP_CLASS = PREFIX + "Snap";

	public static final String CSS_CLASS = "class";
	public static final String TOOL_TIP = "title";
	public static final String EMPTY_HIGHLIGHT = "<flow></flow>";

	@Override
	public void execute(UserActionContext context) throws IOException {

		String kdomid = context.getParameter("kdomid");

		Section<DiaFluxType> diaFluxSec = Sections.getSection(kdomid, DiaFluxType.class);
		Section<FlowchartType> flowchart = Sections.findSuccessor(diaFluxSec, FlowchartType.class);

		KnowledgeBase kb = FlowchartUtils.getKB(diaFluxSec);
		Session session = SessionProvider.getSession(context, kb);

		if (flowchart == null || session == null) {
			context.getWriter().write(EMPTY_HIGHLIGHT);
			return;
		}
		String flowName = FlowchartType.getFlowchartName(flowchart);

		StringBuilder builder = new StringBuilder();
		appendHeader(builder, FlowchartUtils.escapeHtmlId(flowName), PREFIX);

		DiaFluxCaseObject diaFluxCaseObject = DiaFluxUtils.getDiaFluxCaseObject(session);
		DiaFluxTrace trace = FlowchartUtils.getTrace(session);
		
		Flow flow = DiaFluxUtils.getFlowSet(session).get(flowName);

		Map<Edge, Map<String, String>> edges = new HashMap<Edge, Map<String, String>>();
		Map<Node, Map<String, String>> nodes = new HashMap<Node, Map<String, String>>();

		// first highlight traced nodes/edges to yellow
		for (Node node : trace.getTracedNodes()) {
			if (node.getFlow().getName().equals(flowName)) {
				putValue(nodes, node, CSS_CLASS, TRACE_SNAP_CLASS);
			}
		}
		for (Edge edge : trace.getTracedEdges()) {
			if (edge.getStartNode().getFlow().getName().equals(flowName)) {
				putValue(edges, edge, CSS_CLASS, TRACE_SNAP_CLASS);
			}
		}

		// then highlight all currently active nodes/edges to green
		for (FlowRun run : diaFluxCaseObject.getRuns()) {
			for (Node node : run.getActiveNodes()) {
				if (node.getFlow().getName().equals(flowName)) {
					putValue(nodes, node, CSS_CLASS, TRACE_ACTIVE_CLASS);

					for (Edge edge : node.getOutgoingEdges()) {
						if (FluxSolver.evalEdge(session, edge)) {
							putValue(edges, edge, CSS_CLASS, TRACE_ACTIVE_CLASS);
						}
					}
				}
			}
		}

		List<Edge> remainingEdges = new ArrayList<Edge>(flow.getEdges());
		List<Node> remainingNodes = new ArrayList<Node>(flow.getNodes());
		remainingEdges.removeAll(edges.keySet());
		remainingNodes.removeAll(nodes.keySet());

		// clear classes on all remaining nodes and edges
		for (Node node : remainingNodes) {
			putValue(nodes, node, CSS_CLASS, "");
		}

		for (Edge edge : remainingEdges) {
			putValue(edges, edge, CSS_CLASS, "");
		}
		
		for (Node node : flow.getNodes()) {
			addValueTooltip(session, nodes, node);
		}

		addNodeHighlight(builder, nodes);
		addEdgeHighlight(builder, edges);

		appendFooter(builder);

		context.setContentType("text/xml");
		context.getWriter().write(builder.toString());

	}


	private void addValueTooltip(Session session, Map<Node, Map<String, String>> nodes, Node node) {
		String tooltip = FlowchartUtils.getValueTrace(session).getValueString(node);
		if (tooltip != null) {
			putValue(nodes, node, TOOL_TIP, tooltip);
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

	/**
	 * 
	 * @created 08.06.2011
	 * @param builder
	 */
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

	public static <T extends DiaFluxElement> void addHighlight(StringBuilder builder, Map<T, Map<String, String>> objects, String objectType) {
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
}
