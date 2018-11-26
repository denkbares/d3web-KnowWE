/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.core.tools;

import com.denkbares.strings.Identifier;
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
 * @author Stefan Plehn
 * @created 10.03.2014
 */
public class CompositeEditToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		if (hasTools(section, userContext)) {
			return new Tool[] {
					getCompositeEditTool(Sections.cast(section, Term.class)) };
		}
		return ToolUtils.emptyToolArray();
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return section.get() instanceof Term;
	}

	protected Tool getCompositeEditTool(Section<? extends Term> section) {
		return new DefaultTool(
				Icon.INFO,
				"Show Info",
				"Shows information about this object",
				createCompositeEditModeAction(section),
				Tool.CATEGORY_INFO);
	}

	public static String createCompositeEditModeAction(Section<? extends Term> section) {
		Identifier termIdentifier = matchCompatibilityForm(section.get().getTermIdentifier(section));
		return createCompositeEditModeAction(termIdentifier);
	}

	/**
	 * This method is still missplaced, these issues should be handled in OntologyMarkupSet
	 */
	public static Identifier matchCompatibilityForm(Identifier termIdentifier) {
		return (termIdentifier.countPathElements() == 2 && termIdentifier.getPathElementAt(0).isEmpty())
				? new Identifier(termIdentifier.isCaseSensitive(), "lns", termIdentifier.getLastPathElement())
				: termIdentifier;
	}

	public static String createCompositeEditModeAction(Identifier termIdentifier) {
		String externalTermIdentifierForm = termIdentifier.toExternalForm();
		return "KNOWWE.plugin.compositeEditTool.openCompositeEditDialog('"
				+ TermInfoToolProvider.maskTermForHTML(externalTermIdentifierForm) + "')";
	}

}

