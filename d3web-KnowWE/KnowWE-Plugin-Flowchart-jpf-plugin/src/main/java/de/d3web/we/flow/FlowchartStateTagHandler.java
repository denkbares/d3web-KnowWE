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

package de.d3web.we.flow;

import java.util.Map;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.renderer.ReRenderSectionMarkerRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * [{KnowWEPlugin Flowchart}]
 *
 * @author Florian Ziegler
 */
public class FlowchartStateTagHandler extends AbstractTagHandler {

	private final KnowWEDomRenderer<KnowWEObjectType> renderer;

	public FlowchartStateTagHandler() {
		super("flowchart");
		renderer = new ReRenderSectionMarkerRenderer<KnowWEObjectType>(new FlowchartStateRender());
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		KnowWEArticleManager artManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = artManager.getArticle(topic);

		StringBuilder string = new StringBuilder();

		Section<KnowWEObjectType> section = (Section<KnowWEObjectType>) article.findSection(values.get("kdomid"));

		renderer.render(article, section,
				user,
				string);

		return string.toString();

	}

}
