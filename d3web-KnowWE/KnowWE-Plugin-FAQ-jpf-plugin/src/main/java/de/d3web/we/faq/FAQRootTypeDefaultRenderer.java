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

package de.d3web.we.faq;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Default renderer for the custom FAQ type; renders the faq entries in the same
 * way as they are rendered when using the FAQ tag handler
 * 
 * @author M. Freiberg
 * @created 30.06.2010
 */
public class FAQRootTypeDefaultRenderer extends DefaultMarkupRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec,
			KnowWEUserContext user, StringBuilder string) {

		String question =
				DefaultMarkupType.getAnnotation(sec, "question");
		String status =
				DefaultMarkupType.getAnnotation(sec, "status");
		String major =
				DefaultMarkupType.getAnnotation(sec, "major");

		String answer = "A: ";
		String a = DefaultMarkupType.getContent(sec);
		a = a.trim();
		a = FAQUtils.resolveLinks(a);
		answer = answer.concat(a);

		string.append(KnowWEUtils.maskHTML(FAQUtils.renderFAQPluginInner(question, answer, status,
				major)));
	}
}