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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;
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
 * @author Stefan Plehn
 * @created 10.03.2014
 */
public class CompositeEditToolProvider implements ToolProvider {

	public static final String SHOW_INFO = "Show Info";

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		if (hasTools(section, userContext)) {
			Section<Term> termSection = Sections.cast(section, Term.class);

			List<Tool> tools = new ArrayList<>();
			List<Identifier> identifiers = Compilers.getCompilersWithCompileScript(section, TermCompiler.class)
					.stream()
					.map(c -> termSection.get().getTermIdentifier(c, termSection))
					.map(CompositeEditToolProvider::matchCompatibilityForm)
					.distinct()
					.sorted()
					.collect(Collectors.toList());

			for (Identifier identifier : identifiers) {
				if (identifiers.size() > 1) {
					tools.add(getCompositeEditTool(SHOW_INFO + " (" + identifier.toExternalForm() + ")", identifier));
				}
				else {
					tools.add(getCompositeEditTool(identifier));
				}
			}
			return tools.toArray(new Tool[0]);
		}
		return ToolUtils.emptyToolArray();
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return section.get() instanceof Term;
	}

	@NotNull
	protected Tool getCompositeEditTool(Identifier identifier) {
		return getCompositeEditTool(SHOW_INFO, identifier);
	}

	@NotNull
	protected Tool getCompositeEditTool(String text, Identifier identifier) {
		return createCompositeEditTool(text, identifier);
	}

	@NotNull
	public static Tool createCompositeEditTool(String text, Identifier identifier) {
		return new DefaultTool(
				Icon.INFO,
				text,
				"Shows information about this object",
				createCompositeEditModeAction(identifier),
				Tool.CATEGORY_INFO);
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

