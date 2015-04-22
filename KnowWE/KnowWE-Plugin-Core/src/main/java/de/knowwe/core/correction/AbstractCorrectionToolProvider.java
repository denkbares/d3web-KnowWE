/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.core.correction;

import java.util.List;

import de.knowwe.core.action.Action;
import de.knowwe.core.action.KDOMReplaceTermNameAction;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;
import de.knowwe.util.Icon;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 21.04.15
 */
public abstract class AbstractCorrectionToolProvider implements ToolProvider {

	private final Class<? extends Action> actionClass;

	public AbstractCorrectionToolProvider() {
		this.actionClass = KDOMReplaceTermNameAction.class;
	}

	public AbstractCorrectionToolProvider(Class<? extends Action> actionClass) {
		this.actionClass = actionClass;
	}

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		List<Suggestion> suggestions = getSuggestions(section);
		if (suggestions.isEmpty()) {
			return ToolUtils.emptyToolArray();
		}

		Tool[] tools = new Tool[suggestions.size() + 1];

		tools[0] = new DefaultTool(
				Icon.LIGHTBULB,
				Messages.getMessageBundle().getString("KnowWE.Correction.do"),
				"",
				null,
				Tool.CATEGORY_CORRECT
		);

		for (int i = 0; i < suggestions.size(); i++) {
			Suggestion suggestion = suggestions.get(i);
			tools[i + 1] = new DefaultTool(
					Icon.SHARE,
					suggestions.get(i).getSuggestionLabel(),
					"",
					"KNOWWE.plugin.correction.doCorrection('" + section.getID() +
							"', " + (!suggestion.isScript() ? "'" : "")
							+ suggestions.get(i).getSuggestionText()
							+ (!suggestion.isScript() ? "'" : "") + ", '"
							+ actionClass.getSimpleName() + "');",
					Tool.CATEGORY_CORRECT + "/item"
			);
		}

		return tools;
	}

	protected abstract List<Suggestion> getSuggestions(Section<?> section);

}
