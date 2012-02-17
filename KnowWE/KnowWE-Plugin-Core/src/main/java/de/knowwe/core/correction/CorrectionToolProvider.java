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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.plugin.Extension;
import de.d3web.plugin.PluginManager;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.ScopeUtils;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;

/**
 * This ToolProvider provides quick fixes for correcting small mistakes (typos)
 * in term references.
 * 
 * @see CorrectionProvider
 * 
 * @author Alex Legler
 * @created 19.12.2010
 */
public class CorrectionToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		List<CorrectionProvider.Suggestion> suggestions = new LinkedList<CorrectionProvider.Suggestion>();
		ResourceBundle wikiConfig = ResourceBundle.getBundle("KnowWE_config");

		int threshold = Integer.valueOf(wikiConfig.getString("knowweplugin.correction.threshold"));
		KnowWEArticle article = KnowWEUtils.getCompilingArticles(section).iterator().next();
		if (!hasError(article, section)) {
			return new Tool[0];
		}

		for (CorrectionProvider c : getProviders(section)) {
			List<CorrectionProvider.Suggestion> s = c.getSuggestions(article, section, threshold);

			if (s != null) {
				suggestions.addAll(s);
			}
		}

		// Ensure there are no duplicates
		suggestions = new LinkedList<CorrectionProvider.Suggestion>(
				new HashSet<CorrectionProvider.Suggestion>(suggestions));

		// Sort by ascending distance
		Collections.sort(suggestions);

		if (suggestions.size() == 0) {
			return new Tool[0];
		}

		Tool[] tools = new Tool[suggestions.size() + 1];

		tools[0] = new DefaultTool(
				"KnowWEExtension/images/quickfix.gif",
				KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.Correction.do"),
				KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.Correction.do"),
				null,
				"correct"
				);

		for (int i = 0; i < suggestions.size(); i++) {
			tools[i + 1] = new DefaultTool(
					"KnowWEExtension/images/correction_change.gif",
					suggestions.get(i).getSuggestion(),
					"",
					"KNOWWE.plugin.correction.doCorrection('" + section.getID() + "', '"
							+ suggestions.get(i).getSuggestion() + "');",
					"correct/item"
					);
		}

		return tools;
	}

	protected boolean hasError(KnowWEArticle article, Section<?> section) {
		return section.hasErrorInSubtree(article);
	}

	private static CorrectionProvider[] getProviders(Section<?> section) {
		Extension[] extensions = PluginManager.getInstance().getExtensions("KnowWEExtensionPoints",
				"CorrectionProvider");
		extensions = ScopeUtils.getMatchingExtensions(extensions, section);
		CorrectionProvider[] providers = new CorrectionProvider[extensions.length];

		for (int i = 0; i < extensions.length; i++) {
			Extension extension = extensions[i];
			providers[i] = (CorrectionProvider) extension.getSingleton();
		}

		return providers;
	}
}
