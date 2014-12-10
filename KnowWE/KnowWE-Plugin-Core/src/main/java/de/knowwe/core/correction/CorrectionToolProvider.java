/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.plugin.Extension;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.correction.CorrectionProvider.Suggestion;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.ScopeExtensions;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;
import de.knowwe.util.Icon;

/**
 * This ToolProvider provides quick fixes for correcting small mistakes (typos) in term references.
 *
 * @author Alex Legler
 * @created 19.12.2010
 * @see CorrectionProvider
 */
public class CorrectionToolProvider implements ToolProvider {

	private static final ScopeExtensions extensions =
			new ScopeExtensions("KnowWEExtensionPoints", "CorrectionProvider");

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
			tools[i + 1] = new DefaultTool(
					Icon.SHARE,
					suggestions.get(i).getSuggestion(),
					"",
					"KNOWWE.plugin.correction.doCorrection('" + section.getID() + "', '"
							+ suggestions.get(i).getSuggestion() + "');",
					Tool.CATEGORY_CORRECT + "/item"
			);
		}

		return tools;
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		Set<Suggestion> suggestions = getSuggestions(section, 1);
		return !suggestions.isEmpty();
	}

	public static List<Suggestion> getSuggestions(Section<?> section) {
		Set<Suggestion> suggestions = getSuggestions(section, 1000);
		// Sort to list of ascending distance
		List<Suggestion> result = new LinkedList<Suggestion>(suggestions);
		Collections.sort(result);
		return result;
	}

	public static Set<Suggestion> getSuggestions(Section<?> section, int maxCount) {
		List<Extension> matches = extensions.getMatches(section);
		if (matches.isEmpty()) return Collections.emptySet();

		// Ensure there are no duplicates
		Set<Suggestion> suggestions = new HashSet<Suggestion>();
		ResourceBundle wikiConfig = KnowWEUtils.getConfigBundle();

		int threshold = Integer.valueOf(wikiConfig.getString("knowweplugin.correction.threshold"));
		Collection<TermCompiler> compilers = Compilers.getCompilers(section, TermCompiler.class);
		for (TermCompiler termCompiler : compilers) {

			for (Extension extension : matches) {
				CorrectionProvider c = (CorrectionProvider) extension.getSingleton();
				List<Suggestion> s = c.getSuggestions(termCompiler, section, threshold);
				if (s != null) {
					suggestions.addAll(s);
					if (suggestions.size() >= maxCount) break;
				}
			}
		}
		return suggestions;
	}
}
