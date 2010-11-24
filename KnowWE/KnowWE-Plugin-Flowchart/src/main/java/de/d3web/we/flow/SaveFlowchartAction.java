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

/**
 * Receives a xml-encoded flowchart from the editor and replaces the old kdom
 * node with the new content
 * 
 * @author Reinhard Hatko
 * @created 24.11.2010
 */
// This class was copied from ReplaceKDOMNodeAction, as it is still in Research
public class SaveFlowchartAction extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {
		KnowWEParameterMap map = context.getKnowWEParameterMap();

		String web = map.getWeb();
		String nodeID = map.get(KnowWEAttributes.TARGET);
		String name = map.getTopic();
		String newText = map.get(KnowWEAttributes.TEXT);
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);
		Map<String, String> nodesMap = new HashMap<String, String>();
		nodesMap.put(nodeID, newText);
		mgr.replaceKDOMNodesSaveAndBuild(map, name, nodesMap);

	}

}
