/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.d3web.core.knowledge.InterviewObject;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.session.CaseObjectSource;
import de.d3web.core.session.Session;
import de.d3web.core.session.interviewmanager.Form;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.EdgeSupport;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.flow.IEdge;
import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.flow.INodeData;
import de.d3web.diaFlux.flow.ISupport;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.diaFlux.inference.IPath;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;


/**
 *
 * @author Reinhard Hatko
 * @created 09.09.2010
 */
public class FlowchartStateRender extends KnowWEDomRenderer<KnowWEObjectType> {

	@Override
	public void render(KnowWEArticle article, Section<KnowWEObjectType> sec, KnowWEUserContext user, StringBuilder string) {

		Session session = D3webUtils.getSession(article.getTitle(), user, article.getWeb());

		if (!DiaFluxUtils.isFlowCase(session)) {
			string.append("No Flowchart found.");
			return;
		}

		List<Section<FlowchartType>> flows = new ArrayList<Section<FlowchartType>>();

		article.getSection().findSuccessorsOfType(FlowchartType.class, flows);



		if (flows.isEmpty()) {
			string.append("No Flowcharts found in KB.");
			return;
		}

		StringBuilder builder = new StringBuilder(2000);
		// surround content with div with kdom-id for rerendering
		builder.append("<div id='").append(sec.getID()).append("'>");

		// Debug
		if (isDebug(user.getUrlParameterMap())) {
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
			
			builder.append(getPathendText(session));
			builder.append(getBlackboardTable(session));
			builder.append("\n");
		}
		//

		for (Section<FlowchartType> section : flows) {

			Map<String, String> attributeMap = AbstractXMLObjectType.getAttributeMapFor(section);
			String name = attributeMap.get("name");

			builder.append("<div>");
			builder.append("<h3>");
			builder.append("Diagnostic Flow '");
			builder.append(name);
			builder.append("'");
			builder.append("</h3>");

			if (isActive(section, session)) {
				builder.append(createPreviewWithHighlightedPath(section, session));
			}

			builder.append("</div>");
			builder.append("<p/><p/>");
			builder.append("\n");

		}

		builder.append("</div>");

		string.append(KnowWEUtils.maskHTML(builder.toString()));



	}

	private boolean isActive(Section section, Session theCase) {

		// TODO
		// String flowID =
		// AbstractXMLObjectType.getAttributeMapFor(section).get("id");
		//
		// CaseObjectSource flowSet = FluxSolver.getFlowSet(theCase);
		//
		// DiaFluxCaseObject caseObject = (DiaFluxCaseObject)
		// theCase.getCaseObject(flowSet);
		//
		return true;
	}

	private String getPathendText(Session session) {

		if (session == null) return "";

		FlowSet set = DiaFluxUtils.getFlowSet(session);

		DiaFluxCaseObject caseObject = (DiaFluxCaseObject) session.getCaseObject(set);
		Collection<IPath> pathes = caseObject.getActivePathes();

		StringBuilder builder = new StringBuilder(2000);

		builder.append("<b>Current Pathes:</b>");
		builder.append("<br/>");
		for (IPath path : pathes) {

			builder.append("<b>" + path.getFlow().getName() + "</b>");
			builder.append("<br/>");
		}

		builder.append("<br/>");
		builder.append("\n");
		int i = 0;

		for (IPath path : pathes) {

			builder.append(++i + ". Path: " + path.getFlow().getName() + "<br/>");
			builder.append("<table>");

			for (INode node : path.getActiveNodes()) {
				builder.append("<tr>");

				INodeData nodeData = DiaFluxUtils.getNodeData(node, session);
				builder.append("<td>" + nodeData.getNode().getName() + "</td>");
				builder.append("<td>");
				builder.append("<ol>");

				List<ISupport> supports = nodeData.getSupports();
				for (ISupport support : supports) {
					builder.append("<li>" + support + "</li>");

				}

				builder.append("</ol>");

				builder.append("</td>");

				builder.append("</tr>");
				builder.append("\n");

			}
			builder.append("</table>");

			builder.append("<br/>");
			builder.append("<br/>");
			builder.append("\n");

		}

		builder.append("<br/>");

		return builder.toString();
	}

	private String getBlackboardTable(Session session) {

		StringBuilder builder = new StringBuilder(1000);

		Collection<TerminologyObject> objects = new ArrayList<TerminologyObject>(
				session.getBlackboard().getValuedObjects());
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

	private String createPreviewWithHighlightedPath(Section section, Session session) {

		String preview = FlowchartUtils.extractPreview(section);

		if (preview == null) return "";

		if (session == null) return preview;

		String flowID = AbstractXMLObjectType.getAttributeMapFor(section).get("fcid");

		CaseObjectSource flowSet = DiaFluxUtils.getFlowSet(session);

		DiaFluxCaseObject caseObject = (DiaFluxCaseObject) session.getCaseObject(flowSet);

		for (IPath path : caseObject.getActivePathes()) {

			if (path.getFlow().getId().equals(flowID)) preview = highlightPath(preview, flowID,
					path, session);

		}

		return FlowchartUtils.createRenderablePreview(preview);
	}

	private String highlightPath(String preview, String flowID, IPath path, Session session) {
		// get all the nodes
		String[] nodes = preview.split("<DIV class=\"Node\" id=\"");
		String[] edges = preview.split("<DIV class=\"Rule\" id=\"");


		for (INode node : path.getActiveNodes()) {

			// if (!node.getFlow().getId().equals(flowID)) return preview;

			String nodeId = node.getID();
			for (int i = 1; i < nodes.length; i++) {
				if (nodes[i].contains(nodeId + "\"")) {
					preview = colorNode(nodes[i], preview);
				}
			}

			INodeData nodeData = DiaFluxUtils.getNodeData(node, session);

			List<ISupport> supports = nodeData.getSupports();
			for (ISupport support : supports) {

				if ((support instanceof EdgeSupport)) {

					IEdge edge = ((EdgeSupport) support).getEdge();
					String edgeId = edge.getID();

					for (int i = 0; i < edges.length; i++) {
						if (edges[i].contains(edgeId + "\"")) {
							preview = colorEdge(edges[i], preview);
						}
					}
				}
			}

		}
		return preview;
	}

	private String colorNode(String node, String preview) {

		// is node in current flowchart?
		// TODO as FC change along PathEntries, the node might not be in the
		// current FC
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
		// // return true;
		// return false;
	}




}
