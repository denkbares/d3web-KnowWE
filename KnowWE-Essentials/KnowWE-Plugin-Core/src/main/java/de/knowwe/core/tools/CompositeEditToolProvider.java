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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;
import com.denkbares.utils.Pair;
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
			@NotNull Collection<Pair<Identifier, String>> identifiers = getIdentifiers(section, termSection);

			for (Pair<Identifier, String> pair : identifiers) {
				if (identifiers.size() > 1) {
					tools.add(getCompositeEditTool(section, pair.getB(), pair.getA()));
				}
				else {
					tools.add(getCompositeEditTool(section, pair.getA()));
				}
			}
			return tools.toArray(new Tool[0]);
		}
		return ToolUtils.emptyToolArray();
	}

	@NotNull
	protected String getToolText(Identifier identifier) {
		return CompositeEditToolProvider.SHOW_INFO + " (" + identifier.toPrettyPrint() + ")";
	}

	@NotNull
	protected Collection<Pair<Identifier, String>> getIdentifiers(Section<?> section, Section<Term> termSection) {
		return Compilers.getCompilersWithCompileScript(section, TermCompiler.class)
				.stream()
				.flatMap(c -> getTermIdentifiers(c, termSection)
						.filter(i -> showToolForIdentifier(c, i))
						.map(i -> new Pair<>(i, getToolTextFromTermDefinition(c, i))))
				.distinct()
				.sorted(Comparator.comparing(Pair::getB))
				.collect(Collectors.toList());
	}

	private static boolean showToolForIdentifier(TermCompiler c, Identifier i) {
		return c.getTerminologyManager()
				.getTermDefiningSections(i)
				.stream()
				.filter(s -> s.get() instanceof CompositeEditToolVerbalizer)
				.map(s -> Sections.cast(s, CompositeEditToolVerbalizer.class))
				.allMatch(s -> s.get().showToolForIdentifier(c, i));
	}

	private @NotNull String getToolTextFromTermDefinition(TermCompiler c, Identifier i) {
		return c.getTerminologyManager()
				.getTermDefiningSections(i)
				.stream()
				.filter(s -> s.get() instanceof CompositeEditToolVerbalizer)
				.map(s -> Sections.cast(s, CompositeEditToolVerbalizer.class))
				.map(s -> s.get().getCompositeEditToolText(c, i))
				.findFirst().orElse(getToolText(i));
	}

	protected Stream<Identifier> getTermIdentifiers(TermCompiler compiler, Section<Term> termSection) {
		try {
			Collection<Identifier> identifiers = compiler.getTerminologyManager()
					.getRegisteredIdentifiers(compiler, termSection);
			Identifier termIdentifier = termSection.get().getTermIdentifier(compiler, termSection);
			return Stream.concat(identifiers.stream(), Stream.of(termIdentifier)).distinct();
		}
		catch (ClassCastException e) { // in case the identifier can only be generated for certain term compilers
			return Stream.empty();
		}
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return section.get() instanceof Term;
	}

	@NotNull
	protected Tool getCompositeEditTool(@NotNull Section<?> section, Identifier identifier) {
		return getCompositeEditTool(section, CompositeEditToolProvider.SHOW_INFO, identifier);
	}

	@NotNull
	protected Tool getCompositeEditTool(@NotNull Section<?> section, String text, Identifier identifier) {
		return CompositeEditToolProvider.createCompositeEditTool(text, identifier);
	}

	@NotNull
	public static Tool createCompositeEditTool(String text, Identifier identifier) {
		return new CompositeEditTool(text, identifier);
	}

	public static String createCompositeEditModeAction(Identifier termIdentifier) {
		String externalTermIdentifierForm = termIdentifier.toExternalForm();
		return "KNOWWE.plugin.compositeEditTool.openCompositeEditDialog('"
			   + TermInfoToolProvider.maskTermForHTML(externalTermIdentifierForm) + "')";
	}

	public static class CompositeEditTool extends DefaultTool {

		private final Identifier identifier;

		public CompositeEditTool(String title, Identifier identifier) {
			super(Icon.INFO, title, "Shows information about this object", CompositeEditToolProvider.createCompositeEditModeAction(identifier), Tool.CATEGORY_INFO);
			this.identifier = identifier;
		}

		public Identifier getIdentifier() {
			return identifier;
		}
	}
}

