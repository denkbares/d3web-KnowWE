/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package de.knowwe.dialog.action;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Set;

import de.knowwe.dialog.SessionConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(GetLanguages.class);

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

		if (kb == null) {
			failUnexpected(context, "No knowledge base found");
		}

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
				LOGGER.warn(l
								+ " is not a valid language. Thus it will be ignored as available language.");
			}
		}
		writer.write("</languages>\n");
	}

}
