/*
 * Copyright (C) 2021 denkbares GmbH, Germany
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
import java.util.ArrayList;
import java.util.List;

import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Provide an update tool for InterWikiInclude markup
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 09.12.21
 */
public class InterWikiImportMarkupToolProvider implements ToolProvider {
	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		List<Tool> tools = new ArrayList<>();
		tools.add(new DefaultTool(Icon.REFRESH,
				"Force update now", "Updates the referenced wiki resource, even if it has not changed",
				InterWikiImportMarkup.buildRefreshScript(section.getID(), true),
				Tool.ActionType.ONCLICK,
				Tool.CATEGORY_EXECUTE));

		Section<InterWikiImportMarkup> markup = $(section).closest(InterWikiImportMarkup.class).getFirst();
		if (markup != null
				&& markup.get().isTrackingMode(markup)
				&& KnowWEUtils.canWrite(markup, userContext)
				&& hasActiveTrackingWarning(markup)) {
			String action = "(function(){"
					+ "jq$.ajax({"
					+ "url: KNOWWE.core.util.getURL({action:'AcceptInterWikiTrackingDiffAction',"
					+ Attributes.SECTION_ID + ":'" + markup.getID() + "'}),"
					+ "type:'post',"
					+ "cache:false"
					+ "}).done(function(){window.location.reload();})"
					+ ".fail(function(xhr){"
					+ "KNOWWE.notification.error(null,"
					+ "xhr.responseText || 'Unable to acknowledge tracking differences.',"
					+ "'tracking-accept',10000);"
					+ "});"
					+ "})();";
			tools.add(new DefaultTool(Icon.CHECK2,
					"Acknowledge differences",
					"Stores @trackingAcceptedAt for the currently displayed diff.",
					action,
					Tool.ActionType.ONCLICK,
					Tool.CATEGORY_EDIT));
		}

		return tools.toArray(new Tool[0]);
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return true;
	}

	private static boolean hasActiveTrackingWarning(Section<InterWikiImportMarkup> markup) {
		try {
			return InterWikiTrackingService.getTrackingStatus(markup).warningActive();
		}
		catch (IOException e) {
			return false;
		}
	}
}
