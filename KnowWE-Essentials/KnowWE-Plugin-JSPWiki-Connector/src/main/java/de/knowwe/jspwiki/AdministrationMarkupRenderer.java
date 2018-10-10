package de.knowwe.jspwiki;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;

/**
 * @author Tim Abler
 * @created 10.10.2018
 */
public class AdministrationMarkupRenderer extends DefaultMarkupRenderer {

	@Override
	protected void renderContents(Section<?> section, UserContext user, RenderResult string) {
		if (!user.userIsAdmin()) {
			string.append("This feature is only visible and usable for administrators.");
			return;
		}
		string.append("The administrator tools are displayed in the menu to the right.");
	}
}
