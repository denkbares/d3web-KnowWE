/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

public class DialogProvider implements ToolProvider {

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(D3webUtils.getCompiler(userContext, section));
		return !D3webUtils.isEmpty(kb);
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		// check is a URL is available
		String runURL = getRunURL(section, userContext);
		if (runURL == null) return new Tool[0];

		// and create the tool
		return new Tool[] { new DefaultTool(Icon.RUN, "Run in Dialog",
				"Starts a new dialog with that knowledge base.",
				"window.open('" + runURL + "');", Tool.CATEGORY_EXECUTE) };
	}

	/**
	 * Creates a URL to start the dialog for the knowledge base compiling the specified section. The method returns null
	 * if there is no such knowledge base, or if the knowledge base is empty.
	 */
	@Nullable
	public static String getRunURL(Section<?> section, UserContext userContext) {
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(userContext, section);
		if (D3webUtils.isEmpty(kb)) return null;

		Locale locale = Locale.getDefault();
		if (userContext != null && userContext.getRequest() != null) {
			// get the "most preferred" language out of HTTP-Request
			locale = AcceptLanguage.getInitLocale(userContext.getRequest(), kb);
		}
		return "action/InitWiki" +
				"?user=" + (userContext != null ? userContext.getUserName() : "") +
				"&amp;" + Attributes.SECTION_ID + "=" + section.getID() +
				"&amp;lang=" + locale;
	}
}
