/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.hermes.action;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.hermes.kdom.TimeEventDescriptionType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

public class InsertRelationAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		KnowWEArticleManager articleManager = KnowWEEnvironment.getInstance().getArticleManager(
				KnowWEEnvironment.DEFAULT_WEB);
		String topic = parameterMap.getTopic();
		KnowWEArticle art = articleManager.getArticle(
				topic);
		Section event = art.getSection().findChild(parameterMap.get("kdomid"));

		if (event != null) {
			Section description = event.findAncestorOfType(TimeEventDescriptionType.class);
			if (description != null) {
				String property = parameterMap.get("property");
				String object = parameterMap.get("object");
				if (property != null && object != null) {

					StringBuffer insertion = new StringBuffer();
					insertion.append("[");
					insertion.append(property);
					insertion.append("::");
					insertion.append(object);
					insertion.append("]");

					if (!description.getOriginalText().contains(insertion.toString())) {
						Map<String, String> nodesMap = new HashMap<String, String>();
						nodesMap.put(description.getID(), description.getOriginalText() + " - "
								+ insertion.toString());
						articleManager.replaceKDOMNodes(parameterMap, topic, nodesMap);
					}
					return "done";
				}
			}
		}

		return "false";
	}

}
