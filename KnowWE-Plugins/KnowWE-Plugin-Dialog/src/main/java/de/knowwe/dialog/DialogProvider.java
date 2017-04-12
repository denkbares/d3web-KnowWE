/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog;

import java.util.Locale;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;
import de.knowwe.util.Icon;

public class DialogProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(D3webUtils.getCompiler(section));
		return !D3webUtils.isEmpty(kb);
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {

		// hide tool if knowledge base is empty...
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(section);
		if (D3webUtils.isEmpty(kb)) {
			return ToolUtils.emptyToolArray();
		}

		Locale locale = Locale.getDefault();
		if (userContext != null && userContext.getRequest() != null) {
			// get the "most preferred" language out of HTTP-Request
			locale = AcceptLanguage.getInitLocale(userContext.getRequest(), kb);
		}
		String runURL = "action/InitWiki" +
				"?user=" + (userContext != null ? userContext.getUserName() : "") +
				"&amp;" + Attributes.SECTION_ID + "=" + section.getID() +
				"&amp;lang=" + locale;
		Tool run = new DefaultTool(
				Icon.RUN,
				"Run in dialog",
				"Starts a new dialog with that knowledge base.",
				"window.open('" + runURL + "');", Tool.CATEGORY_EXECUTE);
		return new Tool[] { run };
	}

}
