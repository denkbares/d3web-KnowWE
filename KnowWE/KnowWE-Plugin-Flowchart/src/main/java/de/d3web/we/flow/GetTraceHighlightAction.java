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
package de.d3web.we.flow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.d3web.core.session.Session;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowRun;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.diaFlux.inference.FluxSolver;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.UserActionContext;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.packaging.PackageRenderUtils;
import de.d3web.we.flow.type.DiaFluxType;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.utils.D3webUtils;

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
		String web = context.getWeb();

		KnowWEArticleManager articleManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		Section<DiaFluxType> diaFluxSec = (Section<DiaFluxType>) articleManager.getSection(kdomid);

		Section<FlowchartType> flowchart = Sections.findSuccessor(diaFluxSec, FlowchartType.class);
		if (flowchart == null) {
			context.getWriter().write("<flow></flow>");
			return;
		}
		String flowName = FlowchartType.getFlowchartName(flowchart);

		KnowWEArticle article = PackageRenderUtils.checkArticlesCompiling(diaFluxSec.getArticle(),
				diaFluxSec, new StringBuilder());

		Session session = D3webUtils.getSession(article.getTitle(), context, article.getWeb());

		StringBuilder builder = new StringBuilder();
		appendHeader(builder, flowName, PREFIX);

		DiaFluxCaseObject diaFluxCaseObject = DiaFluxUtils.getDiaFluxCaseObject(session);
		Flow flow = DiaFluxUtils.getFlowSet(session).get(flowName);


		List<Edge> snappedEdges = new LinkedList<Edge>();
		List<Node> snappedNodes = new LinkedList<Node>();

		List<Edge> activeEdges = new LinkedList<Edge>();
		List<Node> activeNodes = new LinkedList<Node>();

		// first highlight traced nodes/edges to yellow
		for (Node node : diaFluxCaseObject.getTracedNodes()) {
			if (node.getFlow().getName().equals(flowName)) {
				snappedNodes.add(node);
			}
		}
		for (Edge edge : diaFluxCaseObject.getTracedEdges()) {
			if (edge.getStartNode().getFlow().getName().equals(flowName)) {
				snappedEdges.add(edge);
			}
		}
		// then highlight all currently active nodes/edges to green
		for (FlowRun run : diaFluxCaseObject.getRuns()) {
			for (Node node : run.getActiveNodes()) {
				if (node.getFlow().getName().equals(flowName)) {
					activeNodes.add(node);
					for (Edge edge : node.getOutgoingEdges()) {
						if (FluxSolver.evalEdge(session, edge)) {
							activeEdges.add(edge);
						}
					}
				}
			}
		}

		snappedNodes.removeAll(activeNodes);
		snappedEdges.removeAll(activeEdges);

		addNodeHighlight(builder, snappedNodes, TRACE_SNAP_CLASS);
		addEdgeHighlight(builder, snappedEdges, TRACE_SNAP_CLASS);

		addNodeHighlight(builder, activeNodes, TRACE_ACTIVE_CLASS);
		addEdgeHighlight(builder, activeEdges, TRACE_ACTIVE_CLASS);

		List<Edge> remainingEdges = new ArrayList<Edge>(flow.getEdges());
		List<Node> remainingNodes = new ArrayList<Node>(flow.getNodes());
		remainingEdges.removeAll(activeEdges);
		remainingEdges.removeAll(snappedEdges);

		remainingNodes.removeAll(activeNodes);
		remainingNodes.removeAll(snappedNodes);

		// clear classes on all remaining nodes and edges
		addNodeHighlight(builder, remainingNodes, "");
		addEdgeHighlight(builder, remainingEdges, "");

		appendFooter(builder);

		context.setContentType("text/xml");
		context.getWriter().write(builder.toString());

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
		builder.append("' prefix ='" + PREFIX + "'>\r");

	}

	public static void addEdgeHighlight(StringBuilder builder, List<Edge> edges, String cssclass) {

		for (Edge edge : edges) {
			builder.append("<edge id='");
			builder.append(edge.getID());
			builder.append("'>");
			builder.append(cssclass);
			builder.append("</edge>\r");
		}

	}

	public static void addNodeHighlight(StringBuilder builder, List<Node> nodes, String cssclass) {

		for (Node node : nodes) {
			builder.append("<node id='");
			builder.append(node.getID());
			builder.append("'>");
			builder.append(cssclass);
			builder.append("</node>\r");
		}
	}

}
