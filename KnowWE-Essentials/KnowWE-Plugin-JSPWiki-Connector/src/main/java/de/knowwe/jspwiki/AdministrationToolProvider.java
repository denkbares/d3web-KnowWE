package de.knowwe.jspwiki;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * @author Tim Abler
 * @created 09.10.2018
 */
public class AdministrationToolProvider implements ToolProvider{

	@Override
	public Tool[] getTools(Section<?> section, UserContext user) {
		if(user.userIsAdmin()) {
			boolean readonly = ReadOnlyManager.isReadOnly();

			String js = "javascript:KNOWWE.plugin.jspwikiConnector.setReadOnly(" + readonly + ")";

			Tool help;
			if (readonly) {
				help = new DefaultTool(
						Icon.TOGGLE_ON,
						"Deactivate ReadOnly Mode",
						"Disclaimer: This is a purely administrative feature and should not be used for security purposes, because it is not secure.",
						js,
						Tool.CATEGORY_LAST);
			} else {
				help = new DefaultTool(
						Icon.TOGGLE_OFF,
						"Activate ReadOnly Mode",
						"Disclaimer: This is a purely administrative feature and should not be used for security purposes, because it is not secure.",
						js,
						Tool.CATEGORY_LAST);
			}

			return new Tool[] { help };
		} else {
			return null;
		}
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}
}
