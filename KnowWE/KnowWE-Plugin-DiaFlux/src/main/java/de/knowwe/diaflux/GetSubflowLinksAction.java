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
import java.util.Set;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.diaFlux.flow.ComposedNode;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.packaging.KnowWEPackageManager;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.diaflux.type.DiaFluxType;
import de.knowwe.diaflux.type.FlowchartType;

/**
 * 
 * @author Reinhard Hatko
 * @created 23.08.2011
 */
public class GetSubflowLinksAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String kdomid = context.getParameter("kdomid");
		Section<?> diaFluxSec = Sections.getSection(kdomid);

		Section<FlowchartType> flowchart =
				Sections.findSuccessor(diaFluxSec, FlowchartType.class);
		if (flowchart == null) {
			context.getWriter().write(GetTraceHighlightAction.EMPTY_HIGHLIGHT);
			return;
		}

		KnowWEArticle article = KnowWEUtils.getCompilingArticles(flowchart).iterator().next();

		String string = addSubFlowLinks(article, flowchart);

		context.setContentType("text/xml");
		context.getWriter().write(string);

	}

	// copied from FlowchartRenderers
	private String addSubFlowLinks(KnowWEArticle article, Section<FlowchartType> section) {
		// make sub-flowcharts links to be able to go to their definition
		String thisFlowchartName = FlowchartType.getFlowchartName(section);
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(article.getWeb(), article.getTitle());
		if (kb == null) return GetTraceHighlightAction.EMPTY_HIGHLIGHT;
		FlowSet flowSet = DiaFluxUtils.getFlowSet(kb);
		if (flowSet == null) return GetTraceHighlightAction.EMPTY_HIGHLIGHT;
		Flow flow = flowSet.get(thisFlowchartName);
		if (flow == null) return GetTraceHighlightAction.EMPTY_HIGHLIGHT;

		String flowName = FlowchartType.getFlowchartName(section);

		StringBuilder builder = new StringBuilder();
		builder.append("<flow id='" + FlowchartUtils.escapeHtmlId(flowName) + "'>");
		for (ComposedNode node : flow.getNodesOfClass(ComposedNode.class)) {
			// link to flowchart definition
			Section<FlowchartType> calledSection = findFlowchartSection(
					section, node.getCalledFlowName());
			if (calledSection == null) continue;

			String link = KnowWEUtils.getURLLink(calledSection);
			builder.append("<node id='" + node.getID() + "'>");
			builder.append(link);
			builder.append("</node>");
		}
		builder.append("</flow>");
		return builder.toString();
	}

	public static Section<FlowchartType> findFlowchartSection(
			Section<FlowchartType> section, String calledFlowName) {
		// get all articles compiling this flowchart that will be containing the
		// link
		KnowWEPackageManager pkgManager =
				KnowWEEnvironment.getInstance().getPackageManager(section.getWeb());
		Set<String> compilingArticles = pkgManager.getCompilingArticles(section);
		// get all packages that are compiled by these articles
		Collection<String> allPossiblePackageNames = new ArrayList<String>();
		for (String compilingArticle : compilingArticles) {
			allPossiblePackageNames.addAll(pkgManager.getCompiledPackages(compilingArticle));
		}
		// get all sections compiled by these articles
		Collection<Section<?>> allPossibleSections = new ArrayList<Section<?>>();
		for (String packageName : allPossiblePackageNames) {
			allPossibleSections.addAll(pkgManager.getSectionsOfPackage(packageName));
		}
		// look for flowcharts with the given name in these compiled sections
		Collection<Section<FlowchartType>> matches = new ArrayList<Section<FlowchartType>>();
		for (Section<?> possibleSection : allPossibleSections) {
			if (!(possibleSection.get() instanceof DiaFluxType)) continue;
			Section<FlowchartType> flowchart = Sections.findSuccessor(
					possibleSection, FlowchartType.class);
			if (flowchart == null) continue;
			String flowName = FlowchartType.getFlowchartName(flowchart);
			if (calledFlowName.equalsIgnoreCase(flowName)) {
				matches.add(flowchart);
			}
		}
		// only if there is exactly one match, we know it is the correct one
		if (matches.size() == 1) return matches.iterator().next();
		return null;
	}
}
