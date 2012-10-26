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
public class GetTraceHighlightAction extends AbstractHighlightAction {

	static final String PREFIX = "trace";
	static final String TRACE_ACTIVE_CLASS = PREFIX + "Active";
	static final String TRACE_SNAP_CLASS = PREFIX + "Snap";


	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public void insertHighlighting(Section<FlowchartType> flowchart, Highlight highlight, UserActionContext context) throws IOException {
		Section<DiaFluxType> diaFluxSec = Sections.findAncestorOfExactType(flowchart,
				DiaFluxType.class);

		KnowledgeBase kb = FlowchartUtils.getKB(diaFluxSec);
		Session session = SessionProvider.getSession(context, kb);

		if (session == null) {
			return;
		}

		Flow flow = findFlow(flowchart, kb);

		DiaFluxTrace trace = FlowchartUtils.getTrace(session);
		// first highlight traced nodes/edges to yellow
		for (Node node : trace.getTracedNodes()) {
			if (node.getFlow().equals(flow)) {
				highlight.add(node, Highlight.CSS_CLASS, TRACE_SNAP_CLASS);
			}
		}
		for (Edge edge : trace.getTracedEdges()) {
			if (edge.getStartNode().getFlow().equals(flow)) {
				highlight.add(edge, Highlight.CSS_CLASS, TRACE_SNAP_CLASS);
			}
		}

		DiaFluxCaseObject diaFluxCaseObject = DiaFluxUtils.getDiaFluxCaseObject(session);
		// then highlight all currently active nodes/edges to green
		for (FlowRun run : diaFluxCaseObject.getRuns()) {
			for (Node node : run.getActiveNodes()) {
				if (node.getFlow().equals(flow)) {
					highlight.add(node, Highlight.CSS_CLASS, TRACE_ACTIVE_CLASS);

					for (Edge edge : node.getOutgoingEdges()) {
						if (FluxSolver.evalEdge(session, edge)) {
							highlight.add(edge, Highlight.CSS_CLASS, TRACE_ACTIVE_CLASS);
						}
					}
				}
			}
		}

		highlight.removeClassFromRemainingNodes(flow);

		for (Node node : flow.getNodes()) {
			addValueTooltip(session, highlight, node);
		}
	}

	public static void addValueTooltip(Session session, Highlight highlight, Node node) {
		String tooltip = FlowchartUtils.getValueTrace(session).getValueString(node);
		if (tooltip != null) {
			highlight.add(node, Highlight.TOOL_TIP, tooltip);
		}

	}

	public static Flow findFlow(Section<FlowchartType> flowchart, KnowledgeBase kb) {
		String flowchartName = FlowchartType.getFlowchartName(flowchart);
		return DiaFluxUtils.getFlowSet(kb).get(flowchartName);
	}



}
