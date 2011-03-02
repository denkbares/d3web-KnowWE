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

import java.util.Collection;

import de.d3web.core.inference.condition.NoAnswerException;
import de.d3web.core.inference.condition.UnknownAnswerException;
import de.d3web.core.session.Session;
import de.d3web.diaFlux.flow.DiaFluxCaseObject;
import de.d3web.diaFlux.flow.FlowRun;
import de.d3web.diaFlux.flow.IEdge;
import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * 
 * @author Reinhard Hatko
 */
public class FlowchartRenderer extends KnowWEDomRenderer<FlowchartType> {

	@Override
	public void render(KnowWEArticle article, Section<FlowchartType> sec, KnowWEUserContext user, StringBuilder string) {

		// render preview
		String topic = sec.getArticle().getTitle();
		String web = sec.getArticle().getWeb();
		string.append(createPreview(article, sec, user, web, topic, string));

		// render debug highlighting into the existing flowchart
		// we do this by applying some additional styling
		// to the preview nodes using javascript/dhtml
		String highlight = user.getUrlParameterMap().get("highlight");
		if (highlight != null && highlight.equals("true")) {
			// prepare some basic information
			Session session = D3webUtils.getSession(article.getTitle(), user, article.getWeb());
			DiaFluxCaseObject diaFluxCaseObject = DiaFluxUtils.getDiaFluxCaseObject(session);
			String secID = sec.getFather().getFather().getID();
			String thisFlowchartName = FlowchartType.getFlowchartName(sec);

			// first highlight traced nodes/edges to yellow
			for (INode node : diaFluxCaseObject.getTracedNodes()) {
				if (node.getFlow().getName().equals(thisFlowchartName)) {
					addNodeHighlight(string, secID, node, "#BB0");
				}
			}
			for (IEdge edge : diaFluxCaseObject.getTracedEdges()) {
				if (edge.getStartNode().getFlow().getName().equals(thisFlowchartName)) {
					addEdgeHighlight(string, secID, edge, "#BB0");
				}
			}
			// then highlight all currently active nodes/edges to green
			for (FlowRun run : diaFluxCaseObject.getRuns()) {
				for (INode node : run.getActiveNodes()) {
					if (node.getFlow().getName().equals(thisFlowchartName)) {
						addNodeHighlight(string, secID, node, "green");
						for (IEdge edge : node.getOutgoingEdges()) {
							if (hasFired(session, diaFluxCaseObject.getRuns(), edge)) {
								addEdgeHighlight(string, secID, edge, "green");
							}
						}
					}
				}
			}
		}
	}

	private void addNodeHighlight(StringBuilder result, String sectionID, INode node, String color) {
		result.append(KnowWEUtils.maskHTML("<script>$('" + sectionID
				+ "').getElement('div[id=" + node.getID()
				+ "]').style.border='2px solid " + color + "';</script>"));
	}

	private void addEdgeHighlight(StringBuilder result, String sectionID, IEdge edge, String color) {
		result.append(KnowWEUtils.maskHTML("<script>var child = $('"
				+ sectionID
				+ "').getElement('div[id="
				+ edge.getID()
				+ "]').firstChild; while (child) {"
				+ "if (child.className.match(/[hv]_line/)) {"
				+ "child.style.border='1px solid " + color + "';} "
				+ "child = child.nextSibling;}</script>\n"));
	}

	private boolean hasFired(Session session, Collection<FlowRun> runs, IEdge edge) {
		if (edge.getCondition() == null) return false;
		try {
			if (!edge.getCondition().eval(session)) return false;
		}
		catch (NoAnswerException e) {
			return false;
		}
		catch (UnknownAnswerException e) {
			return false;
		}
		for (FlowRun run : runs) {
			if (edge.getStartNode().canFireEdges(session, run)) return true;
		}
		return false;
	}

	private String createPreview(KnowWEArticle article, Section<FlowchartType> sec, KnowWEUserContext user, String web, String topic, StringBuilder builder) {

		String preview = FlowchartUtils.createRenderablePreview(sec);

		if (preview == null) {
			return "No preview";
		}
		else {
			return KnowWEUtils.maskHTML(preview);
		}

	}

}
