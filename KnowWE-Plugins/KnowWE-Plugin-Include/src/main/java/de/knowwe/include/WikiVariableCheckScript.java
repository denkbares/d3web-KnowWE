/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.include;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DestroyScript;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.variables.InterWikiVariableMarkup;
import de.knowwe.core.compile.variables.VariableIndex;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Checks for the variant of a compiler whether the wiki variables used by imports are properly defined. A variant
 * needs exactly one wiki for each import, so the variable of an <code>%%InterWikiImport</code> must have exactly one
 * value among the definitions the compiler of the variant compiles:
 * <ul>
 * <li>At an import whose variable has no value for the variant, there is a warning that nothing is imported.</li>
 * <li>At an import whose variable has several different values for the variant, there is an error: the variant
 * compiles the content of all these wikis.</li>
 * <li>At each <code>%%InterWikiVariable</code> that has such a conflicting definition for the variant, there is a
 * warning pointing to the other ones, whether the variable is used by an import of the variant or not.</li>
 * </ul>
 * The messages are stored for the compiler, so each variant has its own.
 * <p>
 * The script has to be added to both types, {@link InterWikiImportMarkup} and {@link InterWikiVariableMarkup}, for a
 * compiler standing for a variant; that is why it is abstract: a subclass names the compiler. The messages at one
 * section depend on other sections: the ones at an import on the definitions, the ones at a definition on the other
 * definitions of the variable. Therefore, whenever a definition is compiled or destroyed, the script checks the
 * imports and definitions of the same variable again. A script compiler runs a script only once per section and
 * compilation, so this does not loop.
 *
 * @param <C> the compiler standing for a variant
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
public abstract class WikiVariableCheckScript<C extends PackageCompiler & IncrementalCompiler>
		implements CompileScript<C, DefaultMarkupType>, DestroyScript<C, DefaultMarkupType> {

	/** The source of the messages, the same for all subclasses */
	private static final Class<?> MESSAGE_SOURCE = WikiVariableCheckScript.class;

	@Override
	public void compile(C compiler, Section<DefaultMarkupType> section) {
		if (section.get() instanceof InterWikiImportMarkup) {
			checkImport(compiler, Sections.cast(section, InterWikiImportMarkup.class));
		}
		else if (section.get() instanceof InterWikiVariableMarkup) {
			Section<InterWikiVariableMarkup> definition = Sections.cast(section, InterWikiVariableMarkup.class);
			checkDefinition(compiler, definition);
			checkDependingSections(compiler, definition);
		}
	}

	@Override
	public void destroy(C compiler, Section<DefaultMarkupType> section) {
		Messages.clearMessages(compiler, section, MESSAGE_SOURCE);
		if (section.get() instanceof InterWikiVariableMarkup) {
			// the definition is gone for this compiler, which may solve or cause problems elsewhere
			checkDependingSections(compiler, Sections.cast(section, InterWikiVariableMarkup.class));
		}
	}

	private void checkImport(C compiler, Section<InterWikiImportMarkup> importMarkup) {
		Messages.clearMessages(compiler, importMarkup, MESSAGE_SOURCE);
		String variable = importMarkup.get().getWikiVariable(importMarkup);
		VariableIndex index = VariableIndex.get(importMarkup);
		if (variable == null || index == null) return;

		Map<String, List<Section<InterWikiVariableMarkup>>> definitionsByWiki =
				getDefinitionsByWiki(index.getDefinitions(variable, compiler));
		if (definitionsByWiki.isEmpty()) {
			Messages.storeMessage(compiler, importMarkup, MESSAGE_SOURCE, Messages.warning(
					"Nothing is imported in this variant: the wiki variable '" + variable + "' has no value for it."));
		}
		else if (definitionsByWiki.size() > 1) {
			Messages.storeMessage(compiler, importMarkup, MESSAGE_SOURCE, Messages.error(
					"The wiki variable '" + variable + "' has several values in this variant: "
							+ verbalize(definitionsByWiki, null) + ". The content of all these wikis is compiled, "
							+ "but a variant must import from exactly one."));
		}
	}

	private void checkDefinition(C compiler, Section<InterWikiVariableMarkup> definition) {
		Messages.clearMessages(compiler, definition, MESSAGE_SOURCE);
		String variable = InterWikiVariableMarkup.getVariableName(definition);
		VariableIndex index = VariableIndex.get(definition);
		if (variable == null || index == null) return;

		Map<String, List<Section<InterWikiVariableMarkup>>> definitionsByWiki =
				getDefinitionsByWiki(index.getDefinitions(variable, compiler));
		String wiki = InterWikiImportMarkup.normalizeWiki(InterWikiVariableMarkup.getVariableValue(definition));
		if (definitionsByWiki.size() > 1 && definitionsByWiki.containsKey(wiki)) {
			Messages.storeMessage(compiler, definition, MESSAGE_SOURCE, Messages.warning(
					"The wiki variable '" + variable + "' has other values in this variant too: "
							+ verbalize(definitionsByWiki, wiki) + ". A variant must have exactly one."));
		}
	}

	/**
	 * Checks the sections again whose messages depend on the given definition: the other definitions of the variable
	 * and the imports using it, as far as the compiler compiles them.
	 */
	private void checkDependingSections(C compiler, Section<InterWikiVariableMarkup> definition) {
		// in a complete build all sections are compiled anyway
		if (!compiler.isIncrementalBuild()) return;
		String variable = InterWikiVariableMarkup.getVariableName(definition);
		VariableIndex index = VariableIndex.get(definition);
		ArticleManager articleManager = definition.getArticleManager();
		if (variable == null || index == null || articleManager == null) return;

		for (Section<InterWikiVariableMarkup> other : index.getDefinitions(variable, compiler)) {
			if (other != definition) Compilers.recompileSection(compiler, other, getClass());
		}
		for (Section<InterWikiImportMarkup> importMarkup : Sections.successors(articleManager, InterWikiImportMarkup.class)) {
			if (!variable.equals(importMarkup.get().getWikiVariable(importMarkup))) continue;
			if (!Sections.isLive(importMarkup) || !compiler.isCompiling(importMarkup)) continue;
			Compilers.recompileSection(compiler, importMarkup, getClass());
		}
	}

	/**
	 * Groups the given definitions by the (normalized) wiki they give the variable as its value.
	 */
	private static Map<String, List<Section<InterWikiVariableMarkup>>> getDefinitionsByWiki(List<Section<InterWikiVariableMarkup>> definitions) {
		Map<String, List<Section<InterWikiVariableMarkup>>> definitionsByWiki = new LinkedHashMap<>();
		for (Section<InterWikiVariableMarkup> definition : definitions) {
			String wiki = InterWikiImportMarkup.normalizeWiki(InterWikiVariableMarkup.getVariableValue(definition));
			if (wiki.isBlank()) continue;
			definitionsByWiki.computeIfAbsent(wiki, k -> new ArrayList<>()).add(definition);
		}
		return definitionsByWiki;
	}

	/**
	 * Lists the wikis with the pages defining them, optionally without one of the wikis.
	 */
	private static String verbalize(Map<String, List<Section<InterWikiVariableMarkup>>> definitionsByWiki, @Nullable String withoutWiki) {
		List<String> wikis = new ArrayList<>();
		definitionsByWiki.forEach((wiki, definitions) -> {
			if (Objects.equals(wiki, withoutWiki)) return;
			String pages = definitions.stream().map(Section::getTitle).distinct().collect(Collectors.joining(", "));
			wikis.add(wiki + " (defined on " + pages + ")");
		});
		return String.join(", ", wikis);
	}

	/**
	 * Returns the messages of this check at the given section for the given compiler. For tests and for displaying
	 * them separately.
	 */
	public static List<Message> getMessages(PackageCompiler compiler, Section<?> section) {
		return List.copyOf(Messages.getMessages(compiler, section, MESSAGE_SOURCE));
	}
}
