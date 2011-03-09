/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.d3web.core.knowledge.InterviewObject;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.session.CaseObjectSource;
import de.d3web.core.session.Session;
import de.d3web.core.session.interviewmanager.Form;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowRun;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.flow.type.DiaFluxStateType;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLType;
import de.d3web.we.user.UserContext;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;

/**
 *
 * @author Reinhard Hatko
 * @created 09.09.2010
 */
public class FlowchartStateRender extends KnowWEDomRenderer<DiaFluxStateType> {

	@Override
	public void render(KnowWEArticle article, Section<DiaFluxStateType> sec, UserContext user, StringBuilder string) {

		String master = DiaFluxStateType.getMaster(sec);

		if (master == null) {
			master = article.getTitle();
		}

		Session session = D3webUtils.getSession(master, user, article.getWeb());

		if (!DiaFluxUtils.isFlowCase(session)) {
			string.append("No Flowchart found.");
			return;
		}

		List<Section<FlowchartType>> flows = new ArrayList<Section<FlowchartType>>();

		FlowSet flowSet = DiaFluxUtils.getFlowSet(session);

		for (Flow flow : flowSet) {

			String origin = flow.getInfoStore().getValue(
					Property.getProperty(FlowchartSubTreeHandler.ORIGIN, String.class));

			if (origin == null) continue;

			Section<FlowchartType> node = (Section<FlowchartType>) KnowWEEnvironment.getInstance().getArticleManager(
					article.getWeb()).findNode(origin);
			flows.add(node);
		}

		if (flows.isEmpty()) {
			string.append("No active flowcharts found in KB.");
			return;
		}

		StringBuilder builder = new StringBuilder(2000);

		// surround content with div with kdom-id for rerendering
		builder.append("<div id='").append(sec.getID()).append("'>");

		for (FlowRun run : DiaFluxUtils.getDiaFluxCaseObject(session).getRuns()) {
			for (Node n : run.getActiveNodes()) {
				builder.append(n + ", ");
			}
			builder.append("\n----------------\n");
		}
		// Debug
		if (isDebug(user.getParameters())) {
			builder.append("<b>active object:</b><br>");
			List<InterviewObject> activeObjects = session.getInterview().getInterviewAgenda().getCurrentlyActiveObjects();
			for (InterviewObject object : activeObjects) {
				builder.append(object.getName()).append("<br>");
			}

			Form form = session.getInterview().nextForm();

			InterviewObject nextForm = form.getInterviewObject();

			builder.append("<b>Next InterviewObject:</b>  " + (nextForm != null
					? nextForm.getName()
					: "null") + "<br>");

			builder.append(getBlackboardTable(session));
			builder.append("\n");
		}
		//

		for (Section<FlowchartType> section : flows) {

			Map<String, String> attributeMap = AbstractXMLType.getAttributeMapFor(section);
			String name = attributeMap.get("name");

			builder.append("<div>");
			builder.append("<h4>");
			builder.append("DiaFlux '");
			builder.append(name);
			builder.append("'");
			builder.append("</h4>");

			builder.append(createPreviewWithHighlightedPath(section, session));

			builder.append("</div>");
			builder.append("<p/><p/>");
			builder.append("\n");

		}

		builder.append("</div>");

		string.append(KnowWEUtils.maskHTML(builder.toString()));

	}

	private String getBlackboardTable(Session session) {

		StringBuilder builder = new StringBuilder(1000);

		List<TerminologyObject> objects = new ArrayList<TerminologyObject>(
				session.getBlackboard().getValuedObjects());

		Collections.sort(objects, new Comparator<TerminologyObject>() {

			@Override
			public int compare(TerminologyObject o1, TerminologyObject o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		builder.append("Valued objects: " + objects.size());
		builder.append("<table>");
		builder.append("<tr>");
		builder.append("<th>Object</th><th>Value</th>");

		builder.append("</tr>");

		for (TerminologyObject terminologyObject : objects) {

			builder.append("<tr>");
			builder.append("<td>" + terminologyObject.getName() + "</td>");
			builder.append("<td>"
					+ session.getBlackboard().getValue((ValueObject) terminologyObject) + "</td>");
			builder.append("</tr>");

		}

		builder.append("</table>");

		return builder.toString();
	}

	private String createPreviewWithHighlightedPath(Section<FlowchartType> section, Session session) {

		String preview = FlowchartUtils.extractPreview(section);

		if (preview == null) return "";

		if (session == null) return preview;

		String flowID = AbstractXMLType.getAttributeMapFor(section).get("fcid");

		CaseObjectSource flowSet = DiaFluxUtils.getFlowSet(session);

		DiaFluxCaseObject caseObject = (DiaFluxCaseObject) session.getCaseObject(flowSet);

		preview = "\n" + preview + "\n";

		for (FlowRun run : caseObject.getRuns()) {

			preview = highlightPath(preview, flowID, run, session);

		}

		return FlowchartUtils.createRenderablePreview(preview);
	}

	private String highlightPath(String preview, String flowID, FlowRun run, Session session) {
		// get all the nodes
		String[] nodes = preview.split("<DIV class=\"Node\" id=\"");
		String[] edges = preview.split("<DIV class=\"Rule\" id=\"");

		for (Node node : run.getActiveNodes()) {

			// if (!node.getFlow().getId().equals(flowID)) return preview;

			String nodeId = node.getID();
			for (int i = 1; i < nodes.length; i++) {
				if (nodes[i].contains(nodeId + "\"")) {
					preview = colorNode(nodes[i], preview);
				}
			}

			List<Edge> outgoingEdges = node.getOutgoingEdges();

			for (Edge edge : outgoingEdges) {

				String edgeId = edge.getID();

				// if (!DiaFluxUtils.getEdgeData(edge, session).hasFired()) {
				// continue;
				// }
				//
				// for (int i = 0; i < edges.length; i++) {
				// if (edges[i].contains(edgeId + "\"")) {
				// preview = colorEdge(edges[i], preview);
				// }
				// }
			}

		}
		return preview;
	}

	private String colorNode(String node, String preview) {

		int nodeIndex = preview.indexOf(node);

		if (nodeIndex == -1) return preview;

		// if yes, add the additional class
		String inputHelper1 = preview.substring(0, nodeIndex - 6);
		String inputHelper2 = preview.substring(preview.indexOf(node));
		preview = inputHelper1 + " added" + "\" id=\"" + inputHelper2;

		return preview;
	}

	private String colorEdge(String edge, String preview) {
		// set the additional class of the yet to be colored nodes
		String alteration = "added";

		String temp = preview;

		String[] parts = edge.split("<DIV class=\"");
		for (String s : parts) {
			String type = s.substring(0, s.indexOf("\""));

			// for simple lines
			if (type.equals("h_line") || type.equals("v_line") || type.equals("no_arrow")) {
				String inputHelper1 = temp.substring(0, temp.indexOf(s));
				String inputHelper2 = temp.substring(temp.indexOf(s));
				temp = inputHelper1 + alteration + "\" id=\"" + inputHelper2;

				// for arrows
			}
			else if (type.equals("arrow_up") || type.equals("arrow_down")
					|| type.equals("arrow_left") || type.equals("arrow_right")) {
				int size = type.length();
				String arrowAlteration = "_" + alteration;
				String inputHelper1 = temp.substring(0, temp.indexOf(s) + size);
				String inputHelper2 = temp.substring(temp.indexOf(s));
				temp = inputHelper1 + arrowAlteration + "\" id=\"" + inputHelper2;

				// for the rest
			}
			else if (type.equals("GuardPane") || type.equals("value")) {
				// Logging.getInstance().log(Level.INFO, "type: " + type);
				String inputHelper1 = temp.substring(0, temp.indexOf(s));
				String inputHelper2 = temp.substring(temp.indexOf(s));
				temp = inputHelper1 + alteration + "Text\" id=\"" + inputHelper2;
			}
		}

		return temp;
	}

	private boolean isDebug(Map<String, String> urlParameterMap) {
		String debug = urlParameterMap.get("debug");
		return debug != null && debug.equals("true");
	}

}
