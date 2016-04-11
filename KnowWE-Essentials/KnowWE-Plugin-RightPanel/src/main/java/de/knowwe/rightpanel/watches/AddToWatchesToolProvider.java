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

package de.knowwe.rightpanel.watches;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
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
		return (section.get() instanceof Term
				&& ((Term) section.get()).getTermIdentifier(Sections.cast(section, Term.class)) != null);
	}

	protected Tool getAddToWatchesTool(Section<? extends Term> section) {
		return new DefaultTool(Icon.ADD,
				"Add to watches",
				"Add this term to the watches for debugging.",
				createAddToWatchesAction(section),
				Tool.CATEGORY_INFO);
	}

	protected String createAddToWatchesAction(Section<? extends Term> section) {
		Identifier termIdentifier = ((Term) section.get()).getTermIdentifier(Sections.cast(section, Term.class));
		return "KNOWWE.core.plugin.rightPanel.watches.addToWatches('" + Strings.encodeHtml(termIdentifier.toExternalForm()) + "')";
	}
}
