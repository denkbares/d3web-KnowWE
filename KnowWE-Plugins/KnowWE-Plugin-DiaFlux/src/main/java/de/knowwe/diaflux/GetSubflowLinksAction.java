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
import de.d3web.diaFlux.flow.ComposedNode;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
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

		Section<FlowchartType> flowchart = Sections.get(kdomid, FlowchartType.class);
		if (flowchart == null) {
			Highlight.writeEmpty(context);
			return;
		}

		KnowledgeBase kb = FlowchartUtils.getKB(Sections.ancestor(flowchart,
				DiaFluxType.class));

		if (kb == null) {
			Highlight.writeEmpty(context);
			return;
		}

		String string = getSubFlowLinks(kb, flowchart);

		Highlight.write(context, string);
	}

	// copied from FlowchartRenderers
	private static String getSubFlowLinks(KnowledgeBase kb, Section<FlowchartType> section) {
		// make sub-flowcharts links to be able to go to their definition
		String flowName = FlowchartType.getFlowchartName(section);
		if (kb == null) return Highlight.EMPTY_HIGHLIGHT;
		FlowSet flowSet = DiaFluxUtils.getFlowSet(kb);
		if (flowSet == null) return Highlight.EMPTY_HIGHLIGHT;
		Flow flow = flowSet.get(flowName);
		if (flow == null) return Highlight.EMPTY_HIGHLIGHT;

		StringBuilder builder = new StringBuilder();
		builder.append("<flow id='").append(FlowchartUtils.getParentID(section)).append("'>");
		for (ComposedNode node : flow.getNodesOfClass(ComposedNode.class)) {
			// link to flowchart definition
			Section<FlowchartType> calledSection = FlowchartUtils.findFlowchartSection(
					section, node.getCalledFlowName());
			if (calledSection == null) continue;

			String link = KnowWEUtils.getURLLink(calledSection);
			builder.append("<node id='").append(node.getID()).append("'>");
			builder.append(link);
			builder.append("</node>");
		}
		builder.append("</flow>");
		return builder.toString();
	}


}
