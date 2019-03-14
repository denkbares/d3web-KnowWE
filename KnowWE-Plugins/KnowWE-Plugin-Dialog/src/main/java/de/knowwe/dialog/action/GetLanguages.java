/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package de.knowwe.dialog.action;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Set;

import de.knowwe.dialog.SessionConstants;

import com.denkbares.utils.Log;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * 
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 23.11.2010
 */
public class GetLanguages extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		// HttpSession session = context.getSession();
		// KnowledgeBaseProvider provider = (KnowledgeBaseProvider)
		// session.getAttribute(
		// SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE_PROVIDER);
		// KnowledgeBase kb = provider.getKnowledgeBase();

		// get knowledge base
		KnowledgeBase kb = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);

		// Get available languages
		Set<Locale> locales = KnowledgeBaseUtils.getAvailableLocales(kb);

		context.setContentType(XML);
		Writer writer = context.getWriter();
		writer.write("<languages>\n");
		for (Locale l : locales) {
			if (!l.getLanguage().isEmpty()) {
				writer.write("\t<lang code='" + l + "'>" +
						l.getDisplayName(l) + "</lang>\n");
			}
			else {
				Log.warning(l
								+ " is not a valid language. Thus it will be ignored as available language.");
			}
		}
		writer.write("</languages>\n");
	}

}
