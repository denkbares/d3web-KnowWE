package de.d3web.we.tools;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.user.UserContext;

public interface ToolProvider {

	/**
	 * Returns the tools that this provider can offer for the specified article,
	 * section and user context.
	 *
	 * @created 23.09.2010
	 * @param article the article the tools are requested for
	 * @param section the section the tools are requested for
	 * @param userContext the user's context the tools are requested for
	 * @return the tools that can be provided by this provider
	 */
	Tool[] getTools(KnowWEArticle article, Section<?> section, UserContext userContext);
}
