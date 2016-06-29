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
import java.util.Date;

import de.d3web.core.session.Session;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowRun;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.we.basic.SessionProvider;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.diaflux.type.FlowchartType;

/**
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

		Collection<Session> allSessions = SessionProvider.getSessions(context);
		Collection<Session> sessions = new ArrayList<>();
		// ignore sessions that do not even contain the flow
		for (Session session : allSessions) {
			if (findFlow(flowchart, session.getKnowledgeBase()) != null) {
				sessions.add(session);
			}
		}

		// if there are still multiple sessions, use the on with the newest change
		Session session = null;
		Date latest = null;
		boolean untouched = true;
		for (Session s : sessions) {
			Date tempLatest = s.getLastChangeDate();
			boolean tempUntouched = s.getCreationDate().equals(tempLatest);
			// untouched sessions are only chosen, if there is non that is
			// touched (changed)
			if (latest == null || ((!tempUntouched || untouched) && tempLatest.after(latest))) {
				latest = tempLatest;
				untouched = tempUntouched;
				session = s;
			}
		}

		if (session == null) {
			return;
		}

		Flow flow = findFlow(flowchart, session.getKnowledgeBase());

		// might happen, if flow contains errors and is not contained in kb
		if (flow == null) {
			// TODO error handling
			return;
		}

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
						if (run.isActivated(edge)) {
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

}
