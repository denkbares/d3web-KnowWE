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

package de.d3web.we.kdom.semanticAnnotation;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.ConditionalRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class StandardAnnotationRenderer extends ConditionalRenderer {

    @Override
    public void renderDefault(Section sec, KnowWEUserContext user, StringBuilder string) {

    	Section astring = sec.findSuccessor(AnnotatedString.class);
		String text = "";
		if (astring != null)
			text = "''" + astring.getOriginalText() + "''";
		else
			text = "(?)";
		Section content = sec.findSuccessor(AnnotationContent.class);
		if (content != null) {
			String title = content.getOriginalText();
			text = KnowWEEnvironment.maskHTML("<span title='" + title + "'>"
					+ text + "</span>");
		}
		if (!sec.getObjectType().getOwl(sec).getValidPropFlag()) {
			text = KnowWEEnvironment
					.maskHTML("<p class=\"box error\">invalid annotation attribute:"
							+ sec.getObjectType().getOwl(sec).getBadAttribute()
							+ "</p>");
		}

		string.append(text);
    }

}
