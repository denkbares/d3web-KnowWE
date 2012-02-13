/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.diaFlux.flow.ComposedNode;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowRun;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.diaFlux.inference.FluxSolver;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.KnowWEDomRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * 
 * @author Reinhard Hatko
 */
public class FlowchartRenderer extends KnowWEDomRenderer<FlowchartType> {


	@Override
	public void render(KnowWEArticle article, Section<FlowchartType> sec, UserContext user, StringBuilder string) {

		// render anchor to be able to link to that flowchart
		String anchorName = KnowWEUtils.getAnchor(sec);
		string.append(KnowWEUtils.maskHTML("<a name='" + anchorName + "'></a>"));

		// render preview
		String topic = sec.getArticle().getTitle();
		String web = sec.getArticle().getWeb();
		string.append(createPreview(article, sec, user, web, topic, string));

		ResourceBundle wikiConfig = ResourceBundle.getBundle("KnowWE_config");
		boolean render = Boolean.valueOf(wikiConfig.getString("knowweplugin.diaflux.render"));

		// if rendering is enabled, the highlighting is done via AJAX
		if (render) {
			return;
		}

		// render debug highlighting into the existing flowchart
		// we do this by applying some additional styling
		// to the preview nodes using javascript/dhtml
		String secID = sec.getFather().getFather().getID();
		String thisFlowchartName = FlowchartType.getFlowchartName(sec);
		String highlight = user.getParameters().get("highlight");
		boolean dohighlighting = false;
		HttpServletRequest request = user.getRequest();
		// can be null when booting KnowWE
		if (request != null) {
			HttpSession httpSession = request.getSession();
			// if highlight is null, use highlight from session
			if (highlight == null) {
				String temp = (String) httpSession.getAttribute(DiaFluxTraceHighlight.HIGHLIGHT_KEY);
				if (temp != null) {
					dohighlighting = temp.equalsIgnoreCase(DiaFluxTraceHighlight.TRACE_HIGHLIGHT);
				}
			}
			else if (highlight.equals(DiaFluxTraceHighlight.TRACE_HIGHLIGHT)) {
				dohighlighting = true;
				// save in session
				httpSession.setAttribute(DiaFluxTraceHighlight.HIGHLIGHT_KEY,
						DiaFluxTraceHighlight.TRACE_HIGHLIGHT);
			}
			else if (highlight.equals(DiaFluxTraceHighlight.NO_HIGHLIGHT)) {
				// save false in session
				httpSession.setAttribute(DiaFluxTraceHighlight.HIGHLIGHT_KEY,
						DiaFluxTraceHighlight.NO_HIGHLIGHT);
			}
		}
		if (dohighlighting) {
			// prepare some basic information
			Session session = D3webUtils.getSession(article.getTitle(), user, article.getWeb());
			DiaFluxCaseObject diaFluxCaseObject = DiaFluxUtils.getDiaFluxCaseObject(session);

			// first highlight traced nodes/edges to yellow
			for (Node node : diaFluxCaseObject.getTracedNodes()) {
				if (node.getFlow().getName().equals(thisFlowchartName)) {
					addNodeHighlight(string, secID, node, "#BB0");
				}
			}
			for (Edge edge : diaFluxCaseObject.getTracedEdges()) {
				if (edge.getStartNode().getFlow().getName().equals(thisFlowchartName)) {
					addEdgeHighlight(string, secID, edge, "#BB0");
				}
			}
			// then highlight all currently active nodes/edges to green
			for (FlowRun run : diaFluxCaseObject.getRuns()) {
				for (Node node : run.getActiveNodes()) {
					if (node.getFlow().getName().equals(thisFlowchartName)) {
						addNodeHighlight(string, secID, node, "green");
						for (Edge edge : node.getOutgoingEdges()) {
							if (FluxSolver.evalEdge(session, edge)) {
								addEdgeHighlight(string, secID, edge, "green");
							}
						}
					}
				}
			}
		}

		addSubFlowLinks(string, article, sec);
	}

	private void addSubFlowLinks(StringBuilder result, KnowWEArticle article, Section<FlowchartType> section) {
		// make sub-flowcharts links to be able to go to their definition
		String secID = section.getFather().getFather().getID();
		String thisFlowchartName = FlowchartType.getFlowchartName(section);
		KnowledgeBase kb = D3webUtils.getKnowledgeRepresentationHandler(
				article.getWeb()).getKB(article.getTitle());
		if (kb == null) return;
		FlowSet flowSet = DiaFluxUtils.getFlowSet(kb);
		if (flowSet == null) return;
		Flow flow = flowSet.get(thisFlowchartName);
		if (flow == null) return;
		for (ComposedNode node : flow.getNodesOfClass(ComposedNode.class)) {
			// link to flowchart definition
			Section<FlowchartType> calledSection = FlowchartUtils.findFlowchartSection(
					article.getWeb(), node.getCalledFlowName());
			if (calledSection == null) continue;
			String link = KnowWEUtils.getURLLink(calledSection);
			result.append(KnowWEUtils.maskHTML("<script>var element = $('" + secID
					+ "').getElement('div[id=" + node.getID()
					+ "]'); "
					+ "element.innerHTML='<a href=\"" + link
					+ "\">'+element.innerHTML+'</a>';</script>"));
		}
	}

	private void addNodeHighlight(StringBuilder result, String sectionID, Node node, String color) {
		result.append(KnowWEUtils.maskHTML("<script>$('" + sectionID
				+ "').getElement('div[id=" + node.getID()
				+ "]').style.border='2px solid " + color + "';</script>"));
	}

	private void addEdgeHighlight(StringBuilder result, String sectionID, Edge edge, String color) {
		result.append(KnowWEUtils.maskHTML("<script>var child = $('"
				+ sectionID
				+ "').getElement('div[id="
				+ edge.getID()
				+ "]').firstChild; while (child) {"
				+ "if (child.className.match(/[hv]_line/)) {"
				+ "child.style.border='1px solid " + color + "';} "
				+ "child = child.nextSibling;}</script>"));
	}

	private String createPreview(KnowWEArticle article, Section<FlowchartType> sec, UserContext user, String web, String topic, StringBuilder builder) {

		String preview = FlowchartUtils.createRenderablePreview(sec, user);

		if (preview == null) {
			return "No preview";
		}
		else {
			return KnowWEUtils.maskHTML(preview);
		}

	}

}
