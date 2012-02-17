package de.knowwe.tools;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;

public interface ToolProvider {

	/**
	 * Returns the tools that this provider can offer for the specified article,
	 * section and user context.
	 * 
	 * @created 23.09.2010
	 * @param section the section the tools are requested for
	 * @param userContext the user's context the tools are requested for
	 * @return the tools that can be provided by this provider
	 */
	Tool[] getTools(Section<?> section, UserContext userContext);
}
