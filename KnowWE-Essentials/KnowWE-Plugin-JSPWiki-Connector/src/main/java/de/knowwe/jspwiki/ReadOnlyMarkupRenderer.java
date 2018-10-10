package de.knowwe.jspwiki;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;

/**
 * @author Tim Abler
 * @created 09.10.2018
 */
class ReadOnlyMarkupRenderer extends DefaultMarkupRenderer {

	@Override
	protected void renderContents(Section<?> section, UserContext user, RenderResult string) {
		if (!user.userIsAdmin()) {
			string.append("This feature is only visible and usable for administrators.");
			return;
		}
		string.append("__ReadOnly Mode__");
		String checked = ReadOnlyManager.isReadOnly() ? " checked" : "";
		string.appendHtml("<div class='onoffswitch'>"
				+ "<input type='checkbox' name='onoffswitch' class='onoffswitch-checkbox' id='myonoffswitch'"
				+ checked
				+ " onchange='javascript:KNOWWE.plugin.jspwikiConnector.setReadOnlyCheckbox(this)'>"
				+ "<label class='onoffswitch-label' for='myonoffswitch'>"
				+ "<div class='onoffswitch-inner'></div>"
				+ "<div class='onoffswitch-switch'></div>"
				+ "</label>"
				+ "</div>");
		string.append("Disclaimer: This is a purely administrative feature and should not be used for security purposes, because it is not secure.");
	}
}
