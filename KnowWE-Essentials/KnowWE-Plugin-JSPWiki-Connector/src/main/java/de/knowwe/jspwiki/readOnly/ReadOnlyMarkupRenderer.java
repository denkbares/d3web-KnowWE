/*
 * Copyright (C) 2023 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.jspwiki.readOnly;

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
	public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult string) {
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
