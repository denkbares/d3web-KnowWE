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

import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * 
 * @author Reinhard Hatko
 */
public class FlowchartSectionRenderer extends KnowWEDomRenderer<FlowchartType> {

	@Override
	public void render(KnowWEArticle article, Section<FlowchartType> sec, KnowWEUserContext user, StringBuilder string) {

		String topic = sec.getArticle().getTitle();
		String web = sec.getArticle().getWeb();

		string.append(createPreview(article, sec, user, web, topic, string));
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
