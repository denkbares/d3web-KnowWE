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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.d3web.core.inference.PSAction;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.session.Session;
import de.d3web.core.utilities.Pair;
import de.d3web.diaFlux.flow.ActionNode;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.DiaFluxElement;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowRun;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.diaFlux.inference.FluxSolver;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.KnowWEArticle;
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

	@Override
	public void execute(UserActionContext context) throws IOException {

		String kdomid = context.getParameter("kdomid");

		@SuppressWarnings("unchecked")
		Section<DiaFluxType> diaFluxSec = (Section<DiaFluxType>) Sections.getSection(kdomid);

		Section<FlowchartType> flowchart = Sections.findSuccessor(diaFluxSec, FlowchartType.class);
		String flowName = FlowchartType.getFlowchartName(flowchart);

		KnowWEArticle article = KnowWEUtils.getCompilingArticles(diaFluxSec).iterator().next();

		KnowledgeBase kb = D3webUtils.getKnowledgeBase(context.getWeb(), article.getTitle());
		Session session = SessionProvider.getSession(context, kb);

		if (flowchart == null || session == null) {
			context.getWriter().write("<flow></flow>");
			return;
		}

		StringBuilder builder = new StringBuilder();
		appendHeader(builder, FlowchartUtils.escapeHtmlId(flowName), PREFIX);

		DiaFluxCaseObject diaFluxCaseObject = DiaFluxUtils.getDiaFluxCaseObject(session);
		Flow flow = DiaFluxUtils.getFlowSet(session).get(flowName);

		Map<Edge, Collection<Pair<String, String>>> snappedEdges = new HashMap<Edge, Collection<Pair<String, String>>>();
		Map<Node, Collection<Pair<String, String>>> snappedNodes = new HashMap<Node, Collection<Pair<String, String>>>();

		Map<Edge, Collection<Pair<String, String>>> activeEdges = new HashMap<Edge, Collection<Pair<String, String>>>();
		Map<Node, Collection<Pair<String, String>>> activeNodes = new HashMap<Node, Collection<Pair<String, String>>>();

		// first highlight traced nodes/edges to yellow
		for (Node node : diaFluxCaseObject.getTracedNodes()) {
			if (node.getFlow().getName().equals(flowName)) {
				putValue(snappedNodes, node, CSS_CLASS, TRACE_SNAP_CLASS);
				if (node instanceof ActionNode) {
					PSAction action = ((ActionNode) node).getAction();
					List<? extends TerminologyObject> objects = action.getBackwardObjects();
					if (!objects.isEmpty()) {
						TerminologyObject object = objects.get(0);
						putValue(
								snappedNodes,
								node,
								TOOL_TIP,
								session.getBlackboard().getValue((ValueObject) object).toString());

					}
				}

			}
		}
		for (Edge edge : diaFluxCaseObject.getTracedEdges()) {
			if (edge.getStartNode().getFlow().getName().equals(flowName)) {
				putValue(snappedEdges, edge, CSS_CLASS, TRACE_SNAP_CLASS);
			}
		}
		// then highlight all currently active nodes/edges to green
		for (FlowRun run : diaFluxCaseObject.getRuns()) {
			for (Node node : run.getActiveNodes()) {
				if (node.getFlow().getName().equals(flowName)) {
					putValue(activeNodes, node, CSS_CLASS, TRACE_ACTIVE_CLASS);

					if (node instanceof ActionNode) {
						PSAction action = ((ActionNode) node).getAction();
						List<? extends TerminologyObject> objects = action.getForwardObjects();
						if (!objects.isEmpty()) {
							TerminologyObject object = objects.get(0);
							putValue(
									activeNodes,
									node,
									TOOL_TIP,
									session.getBlackboard().getValue((ValueObject) object).toString());

						}
					}

					for (Edge edge : node.getOutgoingEdges()) {
						if (FluxSolver.evalEdge(session, edge)) {
							putValue(activeEdges, edge, CSS_CLASS, TRACE_ACTIVE_CLASS);
						}
					}
				}
			}
		}

		for (Edge edge : activeEdges.keySet()) {
			snappedEdges.remove(edge);
		}

		for (Node node : activeNodes.keySet()) {
			snappedNodes.remove(node);
		}

		addNodeHighlight(builder, snappedNodes);
		addEdgeHighlight(builder, snappedEdges);

		addNodeHighlight(builder, activeNodes);
		addEdgeHighlight(builder, activeEdges);

		List<Edge> remainingEdges = new ArrayList<Edge>(flow.getEdges());
		List<Node> remainingNodes = new ArrayList<Node>(flow.getNodes());
		remainingEdges.removeAll(activeEdges.keySet());
		remainingEdges.removeAll(snappedEdges.keySet());

		remainingNodes.removeAll(activeNodes.keySet());
		remainingNodes.removeAll(snappedNodes.keySet());

		Map<Edge, Collection<Pair<String, String>>> otherEdges = new HashMap<Edge, Collection<Pair<String, String>>>();
		Map<Node, Collection<Pair<String, String>>> otherNodes = new HashMap<Node, Collection<Pair<String, String>>>();

		// clear classes on all remaining nodes and edges
		for (Node node : remainingNodes) {
			putValue(otherNodes, node, CSS_CLASS, "");
		}

		for (Edge edge : remainingEdges) {
			putValue(otherEdges, edge, CSS_CLASS, "");
		}
		
		addNodeHighlight(builder, otherNodes);
		addEdgeHighlight(builder, otherEdges);

		appendFooter(builder);

		context.setContentType("text/xml");
		context.getWriter().write(builder.toString());

	}

	public static <T> void putValue(Map<T, Collection<Pair<String, String>>> map, T object, String key, String value) {
		Collection<Pair<String, String>> values = map.get(object);
		if (values == null) {
			values = new HashSet<Pair<String, String>>();
			map.put(object, values);
		}
		values.add(new Pair<String, String>(key, value));
	}

	/**
	 * 
	 * @created 08.06.2011
	 * @param builder
	 */
	public static void appendFooter(StringBuilder builder) {
		builder.append("</flow>");
		builder.append("\r");

	}

	public static void appendHeader(StringBuilder builder, String flowName, String prefix) {

		builder.append("<flow id='");
		builder.append(flowName);
		builder.append("' prefix ='" + prefix + "'>\r");

	}

	public static void addEdgeHighlight(StringBuilder builder, Map<Edge, Collection<Pair<String, String>>> edges) {

		addHighlight(builder, edges, "edge");
	}

	public static void addNodeHighlight(StringBuilder builder, Map<Node, Collection<Pair<String, String>>> nodes) {

		addHighlight(builder, nodes, "node");
	}

	public static <T extends DiaFluxElement> void addHighlight(StringBuilder builder, Map<T, Collection<Pair<String, String>>> objects, String objectType) {
		for (T element : objects.keySet()) {
			builder.append("<" + objectType + " id='");
			builder.append(element.getID());
			builder.append("' ");

			for (Pair<String, String> pair : objects.get(element)) {
				builder.append(pair.getA());
				builder.append("='");
				builder.append(pair.getB());
				builder.append("' ");
			}
			builder.append(">");
			builder.append("</" + objectType + ">\r");
		}

	}
}
