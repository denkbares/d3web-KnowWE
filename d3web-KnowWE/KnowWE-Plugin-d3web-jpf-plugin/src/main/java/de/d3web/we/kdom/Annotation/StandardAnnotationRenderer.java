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

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.ConditionalRenderer;
import de.d3web.we.kdom.semanticAnnotation.AnnotatedString;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotationContent;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class StandardAnnotationRenderer extends ConditionalRenderer {

	@Override
	public void renderDefault(Section sec, KnowWEUserContext user, StringBuilder string) {
		try {
			String text = "''" + sec.findSuccessor(AnnotatedString.class).getOriginalText() + "''";
			Section content = sec.findSuccessor(SemanticAnnotationContent.class);
			if(content != null) {
				String title = content.getOriginalText();
				text = KnowWEUtils.maskHTML("<a href=\"#"+sec.getID()+"\"></a>"+"<span title='"+title+"'>"+text+"</span>");
			}
		
			string.append(text);
		} catch (NullPointerException ne){
			// TODO Use Logger here.
//			return "ERROR: No AnnotatedString child found secid: "+sec.getId();
		}
		
	}

}
