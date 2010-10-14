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

package de.d3web.we.flow.kbinfo;

import java.util.LinkedList;
import java.util.List;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;

public class ManagerUtils {

	public static KnowWEArticle getArticle(String web, D3webKnowledgeService service) {
		KnowWEEnvironment knowWEEnv = KnowWEEnvironment.getInstance();
		KnowWEArticleManager articleManager = knowWEEnv.getArticleManager(web);
		if (articleManager == null) return null;

		String id = service.getId();
		int pos = id.indexOf("..");
		String topic = id.substring(0, pos);
		KnowWEArticle article = articleManager.getArticle(topic);
		return article;
	}

	public static List<Section<FlowchartType>> getFlowcharts(String web, D3webKnowledgeService service) {
		KnowWEArticle article = ManagerUtils.getArticle(web, service);
		List<Section<FlowchartType>> result = new LinkedList<Section<FlowchartType>>();
		if (article != null) {
			article.getSection().findSuccessorsOfType(FlowchartType.class, result);
		}
		return result;
	}
}
