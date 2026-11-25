/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package de.knowwe.jspwiki.administration;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * Offers the dialog to delete multiple pages at once in the %%Admin markup, see {@link DeletePagesAction}.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 30.09.2026
 */
public class DeletePagesToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		if (!userContext.userIsAdmin()) return new Tool[0];
		return new Tool[] { new DefaultTool(Icon.DELETE,
				"Delete multiple pages",
				"Clear or delete a list of pages at once, one page name per line, with a preview of what would happen",
				"KNOWWE.plugin.jspwikiConnector.deletePages()",
				Tool.ActionType.ONCLICK,
				Tool.CATEGORY_EXECUTE) };
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return userContext.userIsAdmin();
	}
}
