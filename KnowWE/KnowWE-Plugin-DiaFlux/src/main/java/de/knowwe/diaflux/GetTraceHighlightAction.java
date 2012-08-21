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
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowRun;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.diaFlux.inference.FluxSolver;
import de.d3web.we.basic.SessionProvider;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
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

	@Override
	public void execute(UserActionContext context) throws IOException {

		String kdomid = context.getParameter("kdomid");

		Section<FlowchartType> flowchart = Sections.getSection(kdomid, FlowchartType.class);
		Section<DiaFluxType> diaFluxSec = Sections.findAncestorOfExactType(flowchart,
				DiaFluxType.class);

		KnowledgeBase kb = FlowchartUtils.getKB(diaFluxSec);
		Session session = SessionProvider.getSession(context, kb);

		if (flowchart == null || session == null) {
			Highlights.write(context, Highlights.EMPTY_HIGHLIGHT);
			return;
		}
		String flowName = FlowchartType.getFlowchartName(flowchart);

		StringBuilder builder = new StringBuilder();
		Highlights.appendHeader(builder, FlowchartUtils.escapeHtmlId(flowName), PREFIX);

		DiaFluxCaseObject diaFluxCaseObject = DiaFluxUtils.getDiaFluxCaseObject(session);
		DiaFluxTrace trace = FlowchartUtils.getTrace(session);
		
		Flow flow = DiaFluxUtils.getFlowSet(session).get(flowName);

		Map<Edge, Map<String, String>> edges = new HashMap<Edge, Map<String, String>>();
		Map<Node, Map<String, String>> nodes = new HashMap<Node, Map<String, String>>();

		// first highlight traced nodes/edges to yellow
		for (Node node : trace.getTracedNodes()) {
			if (node.getFlow().getName().equals(flowName)) {
				Highlights.putValue(nodes, node, Highlights.CSS_CLASS, TRACE_SNAP_CLASS);
			}
		}
		for (Edge edge : trace.getTracedEdges()) {
			if (edge.getStartNode().getFlow().getName().equals(flowName)) {
				Highlights.putValue(edges, edge, Highlights.CSS_CLASS, TRACE_SNAP_CLASS);
			}
		}

		// then highlight all currently active nodes/edges to green
		for (FlowRun run : diaFluxCaseObject.getRuns()) {
			for (Node node : run.getActiveNodes()) {
				if (node.getFlow().getName().equals(flowName)) {
					Highlights.putValue(nodes, node, Highlights.CSS_CLASS, TRACE_ACTIVE_CLASS);

					for (Edge edge : node.getOutgoingEdges()) {
						if (FluxSolver.evalEdge(session, edge)) {
							Highlights.putValue(edges, edge, Highlights.CSS_CLASS, TRACE_ACTIVE_CLASS);
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
			Highlights.putValue(nodes, node, Highlights.CSS_CLASS, "");
		}

		for (Edge edge : remainingEdges) {
			Highlights.putValue(edges, edge, Highlights.CSS_CLASS, "");
		}
		
		for (Node node : flow.getNodes()) {
			addValueTooltip(session, nodes, node);
		}

		Highlights.addNodeHighlight(builder, nodes);
		Highlights.addEdgeHighlight(builder, edges);

		Highlights.appendFooter(builder);

		Highlights.write(context, builder.toString());

	}


	private void addValueTooltip(Session session, Map<Node, Map<String, String>> nodes, Node node) {
		String tooltip = FlowchartUtils.getValueTrace(session).getValueString(node);
		if (tooltip != null) {
			Highlights.putValue(nodes, node, Highlights.TOOL_TIP, tooltip);
		}

	}



}
