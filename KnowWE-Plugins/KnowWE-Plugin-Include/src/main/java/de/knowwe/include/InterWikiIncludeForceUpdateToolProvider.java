/*
 * Copyright (C) 2024 denkbares GmbH, Germany
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

package de.knowwe.include;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.RecompileAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.attachment.AttachmentUpdateMarkup;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 04.07.2024
 */
public class InterWikiIncludeForceUpdateToolProvider extends AbstractAction implements ToolProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(InterWikiIncludeForceUpdateToolProvider.class);

	@Override
	public void execute(UserActionContext context) throws IOException {
		if (!context.userIsAdmin()) return;

		ArticleManager articleManager = context.getArticleManager();

		articleManager.open();
		try {
			// also update all markups
			$(articleManager).successor(AttachmentUpdateMarkup.class).stream().forEach(markup -> {
				LOGGER.info("Checking " + markup.get().getUrl(markup) + " for updates...");
				markup.get().performUpdate(markup, true, false);
			});
			RecompileAction.recompileVariant(context, "Update imports tool clicked");
		}
		finally {
			LOGGER.info("Updated all imports, recompiling....");
			articleManager.commit();
		}
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		DefaultTool updateImports = new DefaultTool(Icon.REFRESH,
				"Update imports",
				"Update all import markups",
				"jq$.ajax({url : 'action/InterWikiIncludeForceUpdateToolProvider', cache : false });" +
				"KNOWWE.notification.success(null, 'Triggered all imports to update - this may take a while. Full recompile will be performed at the end.', 'import-all', 20000);",
				Tool.CATEGORY_EXECUTE);
		return new Tool[] { updateImports };
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return userContext.userIsAdmin();
	}
}
