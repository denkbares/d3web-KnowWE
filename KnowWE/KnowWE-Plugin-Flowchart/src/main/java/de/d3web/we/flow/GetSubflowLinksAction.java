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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.diaFlux.flow.ComposedNode;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.UserActionContext;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.packaging.PackageRenderUtils;
import de.d3web.we.flow.type.DiaFluxType;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.utils.KnowWEUtils;

/**
 * 
 * @author Reinhard Hatko
 * @created 23.08.2011
 */
public class GetSubflowLinksAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String kdomid = context.getParameter("kdomid");
		String web = context.getWeb();

		KnowWEArticleManager articleManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		Section<DiaFluxType> diaFluxSec = (Section<DiaFluxType>) Sections.getSection(
				kdomid);

		Section<FlowchartType> flowchart = Sections.findSuccessor(diaFluxSec, FlowchartType.class);
		if (flowchart == null) {
			context.getWriter().write("<flow></flow>");
			return;
		}

		KnowWEArticle article = PackageRenderUtils.checkArticlesCompiling(diaFluxSec.getArticle(),
				diaFluxSec, new StringBuilder());

		String string = addSubFlowLinks(article, flowchart);

		context.setContentType("text/xml");
		context.getWriter().write(string);

	}

	// copied from FlowchartRenderers
	private String addSubFlowLinks(KnowWEArticle article, Section<FlowchartType> section) {
		// make sub-flowcharts links to be able to go to their definition
		String thisFlowchartName = FlowchartType.getFlowchartName(section);
		KnowledgeBase kb = D3webModule.getKnowledgeRepresentationHandler(
				article.getWeb()).getKB(article.getTitle());
		if (kb == null) return "<flow></flow>";
		FlowSet flowSet = DiaFluxUtils.getFlowSet(kb);
		if (flowSet == null) return "<flow></flow>";
		Flow flow = flowSet.get(thisFlowchartName);
		if (flow == null) return "<flow></flow>";

		String flowName = FlowchartType.getFlowchartName(section);

		StringBuilder builder = new StringBuilder();
		builder.append("<flow id='" + flowName + "'>");
		for (ComposedNode node : flow.getNodesOfClass(ComposedNode.class)) {
			// link to flowchart definition
			Section<FlowchartType> calledSection = findFlowchartSection(
					article.getWeb(), node.getCalledFlowName());
			if (calledSection == null) continue;

			String link = KnowWEUtils.getURLLink(calledSection);
			builder.append("<node id='" + node.getID() + "'>");
			builder.append(link);
			builder.append("</node>");
		}
		builder.append("</flow>");
		return builder.toString();
	}

	public static Section<FlowchartType> findFlowchartSection(String web, String calledFlowName) {
		KnowWEArticleManager manager = KnowWEEnvironment.getInstance().getArticleManager(web);

		for (Iterator<KnowWEArticle> iterator = manager.getArticleIterator(); iterator.hasNext();) {
			KnowWEArticle article = iterator.next();
			List<Section<FlowchartType>> matches = new LinkedList<Section<FlowchartType>>();
			Sections.findSuccessorsOfType(article.getSection(), FlowchartType.class, matches);
			for (Section<FlowchartType> match : matches) {
				String flowName = FlowchartType.getFlowchartName(match);
				if (calledFlowName.equalsIgnoreCase(flowName)) {
					// simply return the first matching flowchart in we found in
					// any article
					return match;
				}
			}
		}
		// not match in no article
		return null;
	}

}
