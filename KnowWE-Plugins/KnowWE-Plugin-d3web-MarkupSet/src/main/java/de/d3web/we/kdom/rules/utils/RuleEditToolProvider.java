/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.d3web.we.kdom.rules.utils;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.instantedit.tools.InstantEditTool;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * Toolprovider for %%Rule markup
 *
 * @author Adrian Müller (denkbares GmbH)
 * @created 26.09.16
 */
public class RuleEditToolProvider implements ToolProvider {
	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return KnowWEUtils.canWrite(section.getArticle(), userContext);
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		return new Tool[] { getRuleEditTool(section, userContext), getDebuggingTool(section, userContext) };
	}

	private Tool getDebuggingTool(Section<?> section, UserContext userContext) {
		boolean showDebugView = isRuleDebuggingActive(userContext);

		if (showDebugView) {
			return new DefaultTool(Icon.DEBUG, "Stop rule debugging",
					"Switch back to more concise rendering of rules.",
					"jq$.cookie('RuleDebugView', 'false'); window.location.reload()",
					Tool.ActionType.ONCLICK,
					Tool.CATEGORY_UTIL);
		}
		else {
			return new DefaultTool(
					Icon.DEBUG,
					"Rule debugging",
					"Changes rendering of rules to better show the status of the different conditions and actions.",
					"jq$.cookie('RuleDebugView', 'true'); window.location.reload()",
					Tool.ActionType.ONCLICK,
					Tool.CATEGORY_UTIL);
		}
	}

	protected Tool getRuleEditTool(Section<?> section, UserContext userContext) {
		return new InstantEditTool(
				Icon.EDIT,
				"Edit Rule",
				"Edit this rule",
				section, "KNOWWE.plugin.rule.editTool");
	}

	public static boolean isRuleDebuggingActive(UserContext user) {
		return "true".equals(KnowWEUtils.getCookie("RuleDebugView", "false", user));
	}
}
