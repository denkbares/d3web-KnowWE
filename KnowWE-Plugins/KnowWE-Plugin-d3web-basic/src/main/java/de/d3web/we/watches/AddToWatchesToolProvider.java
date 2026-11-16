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

package de.d3web.we.watches;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.tools.ToolUtils;
import de.knowwe.util.Icon;

/**
 * Created by Stefan Plehn (denkbares GmbH) on 04.12.14.
 */
public class AddToWatchesToolProvider implements ToolProvider {

	private static final String TITLE = "Add to watches";

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		if (hasTools(section, userContext)) {
			return new Tool[] {
					getAddToWatchesTool(Sections.cast(section, Term.class)) };
		}
		return ToolUtils.emptyToolArray();
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		// the watches are shown in their tab of the right panel, which is not available on every page
		if (!WatchesTabProvider.isWatchesAvailable(userContext)) return false;
		if (section.get() instanceof Term) {
			final Section<Term> termSection = Sections.cast(section, Term.class);
			return termSection.get().getTermIdentifier(getTermCompiler(section), termSection) != null;
		}
		return false;
	}

	private TermCompiler getTermCompiler(Section<?> section) {
		return Compilers.getCompilersWithCompileScript(section, TermCompiler.class).stream().findAny().orElse(null);
	}

	private Tool getAddToWatchesTool(Section<? extends Term> section) {
		Identifier termIdentifier = section.get().getTermIdentifier(getTermCompiler(section), section);
		// the tools of several terms in a tool menu (e.g. of a term and the terms it contains) are merged into one
		return new DefaultTool(Icon.ADD,
				TITLE,
				"Add this term to the watches for debugging.",
				createAddToWatchesAction(termIdentifier),
				Tool.CATEGORY_UTIL)
				.setTarget(TITLE, Strings.encodeHtml(termIdentifier.toPrettyPrint()), 0);
	}

	private String createAddToWatchesAction(Identifier termIdentifier) {
		return "KNOWWE.plugin.d3webbasic.watches.addToWatches('" + Strings.encodeHtml(termIdentifier.toExternalForm()) + "')";
	}
}
