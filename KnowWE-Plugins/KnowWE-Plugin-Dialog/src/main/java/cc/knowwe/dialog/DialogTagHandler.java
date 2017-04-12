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
package cc.knowwe.dialog;

import java.util.Locale;
import java.util.Map;

import cc.knowwe.dialog.action.InitWiki;

import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.knowledgebase.KnowledgeBaseType;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.taghandler.AbstractTagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * The TagHandler which renders a link to start the denkbares dialog
 * [{KnowWEPlugin dialog}]
 * 
 * @author Sebastian Furth
 */
public class DialogTagHandler extends AbstractTagHandler {

	public DialogTagHandler() {
		super("dialog");
	}

	@Override
	public void render(Section<?> section, UserContext user, Map<String, String> values, RenderResult result) {

		if (values.containsKey("dialog")) {
			String value = values.get("dialog");
			Strings.trimQuotes(value);
			if (!Strings.isBlank(value)) {
				Article article = KnowWEUtils.getArticle(section.getWeb(), value);
				if (article != null) {
					Section<KnowledgeBaseType> kbSection = Sections.successor(
							article.getRootSection(), KnowledgeBaseType.class);
					if (kbSection != null) {
						section = kbSection;
					}
				}
			}
		}

		Locale locale = Locale.getDefault();
		if (user != null && user.getRequest() != null) {
			// get the "most preferred" language out of HTTP-Request
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(section);
			locale = AcceptLanguage.getInitLocale(user.getRequest(), kb);
		}

		result.appendHtml("<a target='_blank' href='action/InitWiki?");
		result.append(InitWiki.PARAM_USER);
		result.append("=");
		result.append(Strings.encodeURL(user.getUserName()));
		result.append("&amp;");
		result.append(Attributes.SECTION_ID);
		result.append("=");
		result.append(section.getID());
		result.append("&amp;");
		result.append(InitWiki.PARAM_LANGUAGE);
		result.append("=");
		result.append(locale.toString());
		result.appendHtml("'><img src=\"KnowWEExtension/images/run.gif\" style='margin-bottom: -3px' /></a>");

	}

	@Override
	public String getExampleString() {
		return "[{KnowWEPlugin " + getTagName()
				+ " [ = \u00ABknowledge base article\u00BB]" + "}]";
	}

}
