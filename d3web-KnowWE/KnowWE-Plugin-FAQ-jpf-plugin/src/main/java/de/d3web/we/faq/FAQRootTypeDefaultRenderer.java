/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

package de.d3web.we.faq;


import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 *
 * @author meggy
 * @created 30.06.2010
 */
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
		answer = answer.concat(a);
		
		string.append(KnowWEUtils.maskHTML("<div id=\"" + sec.getID() +
				"\" >\n"));

		// build the HTML
		string.append(KnowWEUtils.maskHTML("<div class='faq_question'> Q: "));
		string.append(KnowWEUtils.maskHTML(question));
		string.append(KnowWEUtils.maskHTML("</div>"));
		string.append(KnowWEUtils.maskHTML("<div class='faq_answer'> "));
		string.append(KnowWEUtils.maskHTML(answer));
		string.append(KnowWEUtils.maskHTML("<div class='faq_tags'> "));
		string.append(KnowWEUtils.maskHTML(status));
		string.append(KnowWEUtils.maskHTML(" "));
		string.append(KnowWEUtils.maskHTML(major));
		string.append(KnowWEUtils.maskHTML("</div>"));
		string.append(KnowWEUtils.maskHTML("</div>"));

	}
}

/*
 * boolean renderDialog = Boolean.parseBoolean(content);
 * string.append(KnowWEUtils.maskHTML("<div id=\"" + sec.getId() + "\" >\n"));
 * string.append(KnowWEUtils.maskHTML("<a name='" + KnowWEUtils.getAnchor(sec) +
 * "'></a>\n")); string.append("{{{\n");
 *
 * if (content) {
 *
 *
 * string.append(KnowWEUtils.maskHTML("<div id=\"\" style=\"float:right;\">" ));
 *
 * string.append(KnowWEUtils.maskHTML(
 * "<span class=\"dt-ajax pointer\" rel=\"{dt : '', KdomNodeId : '" +
 * sec.getId() +
 * "'}\"><img src='KnowWEExtension/images/dt_icon_explanation2.png' alt='Default decision tree view' title='Default decision tree view'/></span> "
 * + "<span class=\"dt-ajax pointer\" rel=\"{dt : 'question', KdomNodeId : '" +
 * sec.getId() +
 * "'}\"><img src='KnowWEExtension/images/icon_question_small.gif' alt='Show decision tree in question mode' title='Show decision tree in question mode'/></span> "
 * + "<span class=\"dt-ajax pointer\" rel=\"{dt : 'answer', KdomNodeId : '" +
 * sec.getId() +
 * "'}\"><img src='KnowWEExtension/images/icon_diagnosis.gif' alt='Show decision tree in anwser mode' title='Show decision tree in anwser mode'/></span>"
 * )); string.append(KnowWEUtils.maskHTML("</div>\n")); }
 *
 * // render messages and content renderMessages(sec, string);
 * DelegateRenderer.getInstance().render(article, sec, user, string);
 */