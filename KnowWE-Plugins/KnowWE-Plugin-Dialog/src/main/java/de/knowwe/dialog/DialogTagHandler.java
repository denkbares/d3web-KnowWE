/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package de.knowwe.dialog;

import java.util.Locale;
import java.util.Map;

import de.knowwe.dialog.action.InitWiki;

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
