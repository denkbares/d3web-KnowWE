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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.flow.type.DiaFluxType;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.Section;

/**
 * Receives a xml-encoded flowchart from the editor and replaces the old kdom
 * node with the new content
 * 
 * @author Reinhard Hatko
 * @created 24.11.2010
 */
public class SaveFlowchartAction extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {
		KnowWEParameterMap map = context.getKnowWEParameterMap();

		String web = map.getWeb();
		String nodeID = map.get(KnowWEAttributes.TARGET);
		String topic = map.getTopic();

		String newText = map.get(KnowWEAttributes.TEXT);

		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		Section<DiaFluxType> diaFluxSection = (Section<DiaFluxType>) mgr.getArticle(topic).findSection(
				nodeID);

		Section<FlowchartType> flowchartSection = diaFluxSection.findSuccessor(FlowchartType.class);

		// if flowchart is existing, replace flowchart
		if (flowchartSection != null) {
			save(map, topic, flowchartSection.getID(), newText);
		}
		else { // no flowchart, insert flowchart //TODO more convenient way??

			StringBuilder builder = new StringBuilder("%%DiaFlux");
			builder.append("\r\n");
			builder.append(newText);

			builder.append(diaFluxSection.getOriginalText().substring(9));

			save(map, topic, nodeID, builder.toString());

		}

	}

	private void save(KnowWEParameterMap map, String topic, String nodeID, String newText) {
		Map<String, String> nodesMap = new HashMap<String, String>();
		nodesMap.put(nodeID, newText);
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(map.getWeb());
		mgr.replaceKDOMNodesSaveAndBuild(map, topic, nodesMap);

	}


}
