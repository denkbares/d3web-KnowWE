/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.compile.terminology;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Computes the markup changes a term rename implies, without performing or persisting them. Callers decide how the
 * resulting texts reach the wiki, which is what lets the servlet actions and other drivers share one implementation of
 * the rename semantics.
 * <p>
 * The semantics themselves live in {@link RenamableTerm}, which each markup type implements to say whether one of its
 * sections may be renamed and what its text becomes afterwards.
 */
public final class TermRenamings {

	private TermRenamings() {
	}

	/**
	 * Collects every renamable section that defines or references the term, grouped by the article holding it.
	 *
	 * @param compilers      the compilers whose terminologies are searched
	 * @param termIdentifier the term to collect the definitions and references of
	 */
	@NotNull
	public static Map<Article, Set<Section<? extends RenamableTerm>>> getRegistrationsByArticle(
			@NotNull Collection<? extends TermCompiler> compilers, @NotNull Identifier termIdentifier) {

		Map<Article, Set<Section<? extends RenamableTerm>>> registrationsByArticle = new HashMap<>();
		Consumer<Section<?>> addIfRenamable = section -> {
			if (section.get() instanceof RenamableTerm) {
				registrationsByArticle.computeIfAbsent(section.getArticle(), k -> new HashSet<>())
						.add(Sections.cast(section, RenamableTerm.class));
			}
		};

		for (TermCompiler compiler : compilers) {
			TerminologyManager manager = compiler.getTerminologyManager();
			manager.getTermDefiningSections(termIdentifier).forEach(addIfRenamable);
			manager.getTermReferenceSections(termIdentifier).forEach(addIfRenamable);
		}
		return registrationsByArticle;
	}

	/**
	 * The replacements a rename implies, as section id to new section text, grouped by article. Sections whose article
	 * the caller may not edit, sections refusing the rename, and sections whose text does not change are left out, so
	 * an empty result means the rename is a no-op.
	 *
	 * @param registrationsByArticle the sections to rename, as collected by
	 *                               {@link #getRegistrationsByArticle(Collection, Identifier)}
	 * @param oldIdentifier          the identifier the sections currently refer to
	 * @param newIdentifier          the identifier the sections should refer to afterwards
	 * @param canEditArticle         whether the caller may edit the article of the given title
	 */
	@NotNull
	public static Map<Article, Map<String, String>> collectReplacements(
			@NotNull Map<Article, Set<Section<? extends RenamableTerm>>> registrationsByArticle,
			@NotNull Identifier oldIdentifier,
			@NotNull Identifier newIdentifier,
			@NotNull Predicate<String> canEditArticle) {

		Map<Article, Map<String, String>> replacements = new HashMap<>();
		appendReplacements(registrationsByArticle, oldIdentifier, newIdentifier, canEditArticle, replacements);
		return replacements;
	}

	/**
	 * Adds the replacements of one rename to an existing map, so several renames can be collected before any article is
	 * touched. Behaves like {@link #collectReplacements(Map, Identifier, Identifier, Predicate)} otherwise.
	 *
	 * @param target the map to add to, keyed by article and then by section id
	 */
	public static void appendReplacements(
			@NotNull Map<Article, Set<Section<? extends RenamableTerm>>> registrationsByArticle,
			@NotNull Identifier oldIdentifier,
			@NotNull Identifier newIdentifier,
			@NotNull Predicate<String> canEditArticle,
			@NotNull Map<Article, Map<String, String>> target) {

		for (Map.Entry<Article, Set<Section<? extends RenamableTerm>>> entry : registrationsByArticle.entrySet()) {
			if (!canEditArticle.test(entry.getKey().getTitle())) continue;
			for (Section<? extends RenamableTerm> termSection : entry.getValue()) {
				if (!termSection.get().allowRename(termSection)) continue;
				String textAfterRename = termSection.get()
						.getSectionTextAfterRename(termSection, oldIdentifier, newIdentifier);
				if (textAfterRename.equals(termSection.getText())) continue;
				target.computeIfAbsent(entry.getKey(), k -> new HashMap<>())
						.put(termSection.getID(), textAfterRename);
			}
		}
	}

	/**
	 * The sections that hold the term but refuse to be renamed, grouped by article. A rename leaves these pointing at
	 * the old name, so a caller that cannot fix them up should at least report them.
	 *
	 * @param registrationsByArticle the sections to check, as collected by
	 *                               {@link #getRegistrationsByArticle(Collection, Identifier)}
	 */
	@NotNull
	public static Map<Article, Set<Section<? extends RenamableTerm>>> getUnrenamableSections(
			@NotNull Map<Article, Set<Section<? extends RenamableTerm>>> registrationsByArticle) {

		Map<Article, Set<Section<? extends RenamableTerm>>> unrenamable = new HashMap<>();
		for (Map.Entry<Article, Set<Section<? extends RenamableTerm>>> entry : registrationsByArticle.entrySet()) {
			for (Section<? extends RenamableTerm> termSection : entry.getValue()) {
				if (termSection.get().allowRename(termSection)) continue;
				unrenamable.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).add(termSection);
			}
		}
		return unrenamable;
	}
}
