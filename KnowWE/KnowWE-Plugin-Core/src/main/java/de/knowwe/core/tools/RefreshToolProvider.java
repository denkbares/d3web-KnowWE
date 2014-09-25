package de.knowwe.core.tools;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * Created by Albrecht Striffler (denkbares GmbH) on 14.03.14.
 */
public class RefreshToolProvider implements ToolProvider {
	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		return new Tool[] { getRefreshTool(section, userContext) };
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return false;
	}

	protected Tool getRefreshTool(Section<?> section, UserContext userContext) {
		// tool to execute a full-parse onto the knowledge base
		String jsAction = "var url = window.location.href;" +
				"url = url.replace(/&amp;parse=full/g, '');" +
				"if (url.indexOf('?') == -1) {url += '?';}" +
				"url += '&amp;parse=full';" +
				"window.location = url;";
		return new DefaultTool(
				Icon.REFRESH,
				"Refresh",
				"Performs a fresh rebuild of the knowledge base from the wiki content.",
				jsAction);
	}
}
