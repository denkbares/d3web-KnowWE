/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.knowwe.core.VariantAttachmentWiki.Page;
import de.knowwe.core.VariantAttachmentWiki.State;
import de.knowwe.core.VariantAttachmentWiki.TestVariantCompiler;

import static de.knowwe.core.VariantAttachmentWiki.*;
import static org.junit.Assert.assertEquals;

/**
 * Checks the start of a wiki in which an import compiles a different attachment for each variant, selected by a
 * variable that is defined on other pages. The pages of a wiki are registered in parallel and in no particular order,
 * so the import may be registered before or after the definitions it depends on. Whatever the order is, the result has
 * to be the same, and it has to be there with the one compilation of the start.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
public class VariableAttachmentStartOrderTest {

	private static final String FROM_WIKI_1 = "Import/Codes@wiki-1.txt";
	private static final String FROM_WIKI_2 = "Import/Codes@wiki-2.txt";

	private static final Page IMPORT = variableImport("Import", "fgWiki", "Codes", "shared");
	private static final Page DEFINITION_A = variable("DefinitionA", "fgWiki", "wiki-1", "vehicle-a");
	private static final Page DEFINITION_B = variable("DefinitionB", "fgWiki", "wiki-2", "vehicle-b");
	private static final Page VARIANT_A = variant("VariantA", "shared", "vehicle-a");
	private static final Page VARIANT_B = variant("VariantB", "shared", "vehicle-b");
	private static final Page LOCAL = content("Local", "local", "shared");

	private static final List<Page> PAGES = List.of(IMPORT, DEFINITION_A, DEFINITION_B, VARIANT_A, VARIANT_B, LOCAL);

	private static final State EXPECTED = expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
			"VariantA: local, from-wiki-1",
			"VariantB: local, from-wiki-2");

	private static VariantAttachmentWiki wikiWithBothAttachments() {
		VariantAttachmentWiki wiki = new VariantAttachmentWiki();
		wiki.addAttachment(FROM_WIKI_1, contentMarkup("from-wiki-1", null));
		wiki.addAttachment(FROM_WIKI_2, contentMarkup("from-wiki-2", null));
		return wiki;
	}

	private static void assertStartedWithOneCompilation(String message, State expected, VariantAttachmentWiki wiki) {
		assertEquals(message, expected, wiki.state());
		assertEquals(message + ": compilations of the wiki", 1, wiki.compilations());
		for (TestVariantCompiler variant : wiki.variants()) {
			assertEquals(message + ": compilations of " + variant.getName(), 1, variant.compilations());
		}
	}

	@Test
	public void everyOrderOfRegistrationLeadsToTheSameResult() throws Exception {
		List<List<Page>> orders = permutations(PAGES);
		assertEquals(720, orders.size());
		for (List<Page> order : orders) {
			try (VariantAttachmentWiki wiki = wikiWithBothAttachments()) {
				wiki.start(order);
				assertStartedWithOneCompilation("Order " + order, EXPECTED, wiki);
			}
		}
	}

	@Test
	public void parallelRegistrationLeadsToTheSameResult() throws Exception {
		for (int round = 0; round < 150; round++) {
			List<Page> pages = new ArrayList<>(PAGES);
			Collections.rotate(pages, round);
			try (VariantAttachmentWiki wiki = wikiWithBothAttachments()) {
				wiki.startInParallel(pages);
				assertStartedWithOneCompilation("Round " + round, EXPECTED, wiki);
			}
		}
	}

	@Test
	public void definitionsAndImportOnTheSamePage() throws Exception {
		String definitions = variableMarkup("fgWiki", "wiki-1", "vehicle-a") + variableMarkup("fgWiki", "wiki-2", "vehicle-b");
		String importMarkup = variableImportMarkup("fgWiki", "Codes", "shared");
		for (String text : List.of(definitions + importMarkup, importMarkup + definitions)) {
			for (List<Page> order : permutations(List.of(new Page("Import", text), VARIANT_A, VARIANT_B, LOCAL))) {
				try (VariantAttachmentWiki wiki = wikiWithBothAttachments()) {
					wiki.start(order);
					assertStartedWithOneCompilation("Order " + order, EXPECTED, wiki);
				}
			}
		}
	}

	@Test
	public void definitionsWithoutPackageUseThePackageOfTheirPage() throws Exception {
		// without @package, a definition is in the default package like any other markup, so it is valid for all
		// variants compiling the default package: here both attachments are compiled by the one variant doing so
		List<Page> pages = List.of(IMPORT,
				variable("DefinitionA", "fgWiki", "wiki-1", null),
				variable("DefinitionB", "fgWiki", "wiki-2", null),
				variant("VariantA", "shared", "default"), VARIANT_B, LOCAL);
		State expected = expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local, from-wiki-1, from-wiki-2",
				"VariantB: local");
		for (List<Page> order : permutations(pages)) {
			try (VariantAttachmentWiki wiki = wikiWithBothAttachments()) {
				wiki.start(order);
				assertStartedWithOneCompilation("Order " + order, expected, wiki);
			}
		}
	}

	@Test
	public void missingAttachmentIsCompiledAsSoonAsItIsStored() throws Exception {
		try (VariantAttachmentWiki wiki = new VariantAttachmentWiki()) {
			wiki.addAttachment(FROM_WIKI_1, contentMarkup("from-wiki-1", null));
			wiki.start(PAGES);
			assertStartedWithOneCompilation("Start", expect(List.of(FROM_WIKI_1),
					"VariantA: local, from-wiki-1",
					"VariantB: local"), wiki);

			// what the first poll of the import does after the start
			wiki.storeAttachment(FROM_WIKI_2, contentMarkup("from-wiki-2", null));
			wiki.await(EXPECTED, wiki::state);
			assertEquals("the other variant must not compile again", 1, wiki.variant("VariantA").compilations());
			assertEquals(2, wiki.variant("VariantB").compilations());
		}
	}

	@Test
	public void attachmentWithoutDefinitionIsNotCompiled() throws Exception {
		try (VariantAttachmentWiki wiki = wikiWithBothAttachments()) {
			// left over from a value the variable no longer has
			wiki.addAttachment("Import/Codes@wiki-3.txt", contentMarkup("from-wiki-3", null));
			wiki.start(PAGES);
			assertStartedWithOneCompilation("Start", EXPECTED, wiki);

			// neither if it is stored again
			wiki.storeAttachment("Import/Codes@wiki-3.txt", contentMarkup("from-wiki-3-changed", null));
			wiki.storeAttachment(FROM_WIKI_2, contentMarkup("from-wiki-2-changed", null));
			wiki.await(expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
					"VariantA: local, from-wiki-1",
					"VariantB: local, from-wiki-2-changed"), wiki::state);
		}
	}

	@Test
	public void importWithoutVariableIsCompiledLikeItsMarkup() throws Exception {
		List<Page> pages = List.of(literalImport("Import", "wiki-1", "Codes", "vehicle-a"),
				DEFINITION_A, DEFINITION_B, VARIANT_A, VARIANT_B, LOCAL);
		for (List<Page> order : List.of(pages, reversed(pages))) {
			try (VariantAttachmentWiki wiki = new VariantAttachmentWiki()) {
				wiki.addAttachment("Import/Codes.txt", contentMarkup("from-wiki-1", null));
				wiki.start(order);
				assertStartedWithOneCompilation("Order " + order, expect(List.of("Import/Codes.txt"),
						"VariantA: local, from-wiki-1",
						"VariantB: local"), wiki);
			}
		}
	}

	private static <T> List<T> reversed(List<T> list) {
		List<T> reversed = new ArrayList<>(list);
		Collections.reverse(reversed);
		return reversed;
	}

	static <T> List<List<T>> permutations(List<T> items) {
		if (items.isEmpty()) return List.of(List.of());
		List<List<T>> result = new ArrayList<>();
		for (int i = 0; i < items.size(); i++) {
			List<T> rest = new ArrayList<>(items);
			T first = rest.remove(i);
			for (List<T> permutation : permutations(rest)) {
				List<T> order = new ArrayList<>(permutation.size() + 1);
				order.add(first);
				order.addAll(permutation);
				result.add(order);
			}
		}
		return result;
	}
}
