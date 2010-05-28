/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.kdom.questionTreeNew.dialog;

import java.util.List;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.CustomRenderer;
import de.d3web.we.kdom.rendering.RenderingMode;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * QuestionLineRenderer.
 * Renders the {@link QuestionLine } in the collapsible question tree view.
 * 
 * @author smark
 * @since 2010/05/28
 */
public class QuestionLineRenderer extends CustomRenderer{

	@Override
	public boolean doesApply(String user, String topic, RenderingMode type) {
		return true;
	}

	@Override
	public void render(KnowWEArticle article, Section sec,
			KnowWEUserContext user, StringBuilder string) {
		
		string.append(KnowWEUtils.maskHTML("<span")); 
		string.append(" style='").append(FontColorRenderer.COLOR3).append("'");
		string.append(KnowWEUtils.maskHTML(">"));
			
		List<Section<? extends KnowWEObjectType>> children = sec.getChildren();
		
		for (Section<? extends KnowWEObjectType> section : children) {
			String sectionName = section.getObjectType().getName();
			if( sectionName.equals("QuestionDefQTree") ) {
				string.append( section.getOriginalText() );
			}
		}
		string.append(KnowWEUtils.maskHTML("</span>"));
	}
}
