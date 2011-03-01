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
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;

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

		if (nodeID == null) {
			saveNewFlowchart(map, web, topic, newText);
		}
		else {
			replaceExistingFlowchart(map, web, nodeID, topic, newText);
		}

	}

	/**
	 * Saves a flowchart when the surrounding %%DiaFlux markup exists.
	 * 
	 * @created 23.02.2011
	 * @param map
	 * @param web
	 * @param nodeID
	 * @param topic
	 * @param newText
	 */
	@SuppressWarnings("unchecked")
	private void replaceExistingFlowchart(KnowWEParameterMap map, String web, String nodeID, String topic, String newText) {
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		Section<DiaFluxType> diaFluxSection = (Section<DiaFluxType>) mgr.getArticle(topic).findSection(
				nodeID);

		Section<FlowchartType> flowchartSection = Sections.findSuccessor(diaFluxSection,
				FlowchartType.class);

		// if flowchart is existing, replace flowchart
		if (flowchartSection != null) {
			save(map, topic, flowchartSection.getID(), newText);
		}
		else { // no flowchart, insert flowchart
			StringBuilder builder = new StringBuilder("%%DiaFlux");
			builder.append("\r\n");
			builder.append(newText);

			// one line version, breaks because of missing linebreak, see ticket
			// #172
			if (diaFluxSection.getOriginalText().matches("%%DiaFlux */?% *")) {
				builder.append("\r\n");
				builder.append("%");
			}
			else { // TODO this adds all content, just extract annotations
				builder.append(diaFluxSection.getOriginalText().substring(9));
			}

			save(map, topic, nodeID, builder.toString());
		}
	}

	/**
	 * Saves a flowchart for which no section exists in the article yet.
	 * 
	 * @created 23.02.2011
	 * @param map
	 * @param web
	 * @param topic
	 * @param newText
	 */
	private void saveNewFlowchart(KnowWEParameterMap map, String web, String topic, String newText) {
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(map.getWeb());
		KnowWEArticle article = mgr.getArticle(topic);
		Section<KnowWEArticle> rootSection = article.getSection();

		// append flowchart to root section and replace it
		String newArticle = rootSection.getOriginalText() + "\r\n%%DiaFlux\r\n" + newText
				+ "\r\n%\r\n";
		String nodeID = rootSection.getID();
			
		save(map, topic, nodeID, newArticle);
	}

	private void save(KnowWEParameterMap map, String topic, String nodeID, String newText) {
		Map<String, String> nodesMap = new HashMap<String, String>();
		nodesMap.put(nodeID, newText);
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(map.getWeb());
		mgr.replaceKDOMNodesSaveAndBuild(map, topic, nodesMap);
	}

}
