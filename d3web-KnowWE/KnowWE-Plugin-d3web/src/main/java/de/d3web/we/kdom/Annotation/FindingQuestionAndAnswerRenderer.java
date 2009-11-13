/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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
package de.d3web.we.kdom.Annotation;

import java.util.List;

import de.d3web.report.Message;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.renderer.FontColorBackgroundRenderer;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.renderer.ObjectInfoLinkRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.taghandler.KDOMRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Renders red background for Findings and questions when there was 
 * an error when creating the knowledge. Error is contained in tooltip.
 * 
 * @author Reinhard Hatko
 * Created on: 12.11.2009
 */
public class FindingQuestionAndAnswerRenderer extends KnowWEDomRenderer {

	private final KnowWEDomRenderer delegate ;
	
	public FindingQuestionAndAnswerRenderer(String foregroundColor) {
		
		delegate = new ObjectInfoLinkRenderer(
				FontColorRenderer.getRenderer(foregroundColor));
	
	}
	
	
	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {

		List<Message> messages = ((AbstractKnowWEObjectType) sec.getObjectType()).getMessages(sec);
		
		if (messages.isEmpty()) {
			delegate.render(sec, user, string);
		} else {
			
			//TODO: atm just the first is used, rest ignored
			Message message = messages.get(0);
			
			string.append("<span title=\"" + message +"\">");
			
			FontColorBackgroundRenderer.getRenderer(FontColorRenderer.COLOR6, "red").render(sec, user, string);

			string.append("</span>");
			
			
			
		} 
		
		
		

	}

}
