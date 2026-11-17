/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.knowwe.core.VariantAttachmentWiki.State;

import static de.knowwe.core.VariantAttachmentWiki.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Checks changes in a running wiki in which an import compiles a different attachment for each variant, selected by a
 * variable: changes of the definitions, of the import and of the attachments (which is how an update from the source
 * wiki arrives). After each scenario the wiki has to be in the very state it would have after a restart with the same
 * pages and attachments.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
public class VariableAttachmentChangeTest {

	private static final String FROM_WIKI_1 = "Import/Codes@wiki-1.txt";
	private static final String FROM_WIKI_2 = "Import/Codes@wiki-2.txt";
	private static final String FROM_WIKI_3 = "Import/Codes@wiki-3.txt";

	private static final State STARTED = expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
			"VariantA: local, from-wiki-1",
			"VariantB: local, from-wiki-2");

	private VariantAttachmentWiki wiki;

	@Before
	public void startWiki() throws Exception {
		wiki = new VariantAttachmentWiki();
		wiki.addAttachment(FROM_WIKI_1, contentMarkup("from-wiki-1", null));
		wiki.addAttachment(FROM_WIKI_2, contentMarkup("from-wiki-2", null));
		wiki.start(List.of(
				variableImport("Import", "fgWiki", "Codes", "shared"),
				variable("DefinitionA", "fgWiki", "wiki-1", "vehicle-a"),
				variable("DefinitionB", "fgWiki", "wiki-2", "vehicle-b"),
				variant("VariantA", "shared", "vehicle-a"),
				variant("VariantB", "shared", "vehicle-b"),
				content("Local", "local", "shared")));
		assertEquals(STARTED, wiki.state());
	}

	@After
	public void closeWiki() {
		wiki.close();
	}

	/**
	 * Asserts the state of the running wiki, and that a restart with the same pages and attachments leads to the same
	 * state. Closes the wiki, so this is the last check of a test.
	 */
	private void assertStateAlsoAfterRestart(State expected) throws InterruptedException {
		wiki.await(expected, wiki::state);
		assertEquals("State after restart", expected, wiki.stateAfterRestart());
	}

	@Test
	public void importProvidesTheArticlesOfAllItsAttachments() {
		var importSection = de.knowwe.core.kdom.parsing.Sections.successor(
				wiki.manager.getArticle("Import"), VariantAttachmentWiki.TestImportMarkup.class);
		assertEquals(List.of(FROM_WIKI_1, FROM_WIKI_2), importSection.get().getCompiledAttachmentArticles(importSection)
				.stream().map(de.knowwe.core.kdom.Article::getTitle).sorted().toList());
		// there is no single article for an import with several attachments
		assertNull(importSection.get().getCompiledAttachmentArticle(importSection));
	}

	/* --------------------------------------------- definitions --------------------------------------------- */

	@Test
	public void valueOfDefinitionChangesToWikiNotImportedYet() throws Exception {
		wiki.save(variable("DefinitionA", "fgWiki", "wiki-3", "vehicle-a"));
		assertEquals(expect(List.of(FROM_WIKI_2),
				"VariantA: local",
				"VariantB: local, from-wiki-2"), wiki.state());
		assertNull("the article of the previous source must be gone", wiki.manager.getArticle(FROM_WIKI_1));

		// the poll fetches the content from the new source
		wiki.storeAttachment(FROM_WIKI_3, contentMarkup("from-wiki-3", null));
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_2, FROM_WIKI_3),
				"VariantA: local, from-wiki-3",
				"VariantB: local, from-wiki-2"));
	}

	@Test
	public void valueOfDefinitionChangesToWikiAlreadyImported() throws Exception {
		wiki.save(variable("DefinitionA", "fgWiki", "wiki-2", "vehicle-a"));
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_2),
				"VariantA: local, from-wiki-2",
				"VariantB: local, from-wiki-2"));
	}

	@Test
	public void packageOfDefinitionChanges() throws Exception {
		// only the package changes, the attachments stay the same, but are compiled by other variants
		wiki.save(variable("DefinitionA", "fgWiki", "wiki-1", "vehicle-b"));
		assertEquals(expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local",
				"VariantB: local, from-wiki-1, from-wiki-2"), wiki.state());

		wiki.save(variable("DefinitionA", "fgWiki", "wiki-1", "vehicle-a"));
		assertStateAlsoAfterRestart(STARTED);
	}

	@Test
	public void definitionIsRemoved() throws Exception {
		wiki.delete("DefinitionA");
		assertNull(wiki.manager.getArticle(FROM_WIKI_1));
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_2),
				"VariantA: local",
				"VariantB: local, from-wiki-2"));
	}

	@Test
	public void definitionIsRemovedAndAddedAgain() throws Exception {
		wiki.save("DefinitionA", "no definition any more");
		assertEquals(expect(List.of(FROM_WIKI_2),
				"VariantA: local",
				"VariantB: local, from-wiki-2"), wiki.state());

		wiki.save(variable("DefinitionA", "fgWiki", "wiki-1", "vehicle-a"));
		assertStateAlsoAfterRestart(STARTED);
	}

	@Test
	public void definitionForFurtherVariantIsAdded() throws Exception {
		wiki.save(variant("VariantC", "shared", "vehicle-c"));
		assertEquals(expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local, from-wiki-1",
				"VariantB: local, from-wiki-2",
				"VariantC: local"), wiki.state());

		// the new variant imports from the same wiki as variant A and shares its attachment
		wiki.save(variable("DefinitionC", "fgWiki", "wiki-1", "vehicle-c"));
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local, from-wiki-1",
				"VariantB: local, from-wiki-2",
				"VariantC: local, from-wiki-1"));
	}

	@Test
	public void conflictingDefinitionIsAddedAndRemoved() throws Exception {
		// a second value for variant A: until this is fixed, the variant compiles both attachments
		wiki.save(variable("DefinitionA2", "fgWiki", "wiki-2", "vehicle-a"));
		assertEquals(expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local, from-wiki-1, from-wiki-2",
				"VariantB: local, from-wiki-2"), wiki.state());
		assertEquals(List.of("wiki-1", "wiki-2"), List.copyOf(wiki.variables().getValues("fgWiki", wiki.variant("VariantA"))));

		wiki.delete("DefinitionA2");
		assertStateAlsoAfterRestart(STARTED);
	}

	/* ----------------------------------------------- imports ----------------------------------------------- */

	@Test
	public void importIsAddedAndRemoved() throws Exception {
		wiki.addAttachment("Parts/List@wiki-1.txt", contentMarkup("parts-from-wiki-1", null));
		wiki.save(variableImport("Parts", "fgWiki", "List", "shared"));
		assertEquals(expect(List.of(FROM_WIKI_1, FROM_WIKI_2, "Parts/List@wiki-1.txt"),
				"VariantA: local, from-wiki-1, parts-from-wiki-1",
				"VariantB: local, from-wiki-2"), wiki.state());

		wiki.delete("Import");
		assertNull(wiki.manager.getArticle(FROM_WIKI_1));
		assertNull(wiki.manager.getArticle(FROM_WIKI_2));
		assertStateAlsoAfterRestart(expect(List.of("Parts/List@wiki-1.txt"),
				"VariantA: local, parts-from-wiki-1",
				"VariantB: local"));
	}

	@Test
	public void importSwitchesBetweenLiteralWikiAndVariable() throws Exception {
		wiki.addAttachment("Import/Codes.txt", contentMarkup("from-literal-wiki", null));
		wiki.save(literalImport("Import", "wiki-1", "Codes", "shared"));
		assertEquals(expect(List.of("Import/Codes.txt"),
				"VariantA: local, from-literal-wiki",
				"VariantB: local, from-literal-wiki"), wiki.state());

		wiki.save(variableImport("Import", "fgWiki", "Codes", "shared"));
		assertNull(wiki.manager.getArticle("Import/Codes.txt"));
		assertStateAlsoAfterRestart(STARTED);
	}

	@Test
	public void importUsesAnotherVariable() throws Exception {
		wiki.addAttachment(FROM_WIKI_3, contentMarkup("from-wiki-3", null));
		wiki.save(variable("DefinitionOther", "otherWiki", "wiki-3", "vehicle-b"));
		assertEquals(STARTED, wiki.state());

		wiki.save(variableImport("Import", "otherWiki", "Codes", "shared"));
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_3),
				"VariantA: local",
				"VariantB: local, from-wiki-3"));
	}

	@Test
	public void importUsesUndefinedVariable() throws Exception {
		wiki.save(variableImport("Import", "unknownWiki", "Codes", "shared"));
		assertStateAlsoAfterRestart(expect(List.of(),
				"VariantA: local",
				"VariantB: local"));
	}

	@Test
	public void packageOfImportChanges() throws Exception {
		// the import itself is only compiled by variant A now, so variant B no longer gets its attachment; the
		// attachments are compiled again together with the page of the import, no full parse is needed for that
		wiki.save(variableImport("Import", "fgWiki", "Codes", "vehicle-a"));
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local, from-wiki-1",
				"VariantB: local"));
	}

	@Test
	public void pageOfImportChangesWithoutEffectOnAttachments() throws Exception {
		// e.g. a text next to the import: the attachments are neither parsed nor compiled again
		int compilationsOfA = wiki.variant("VariantA").compilations();
		int compilationsOfB = wiki.variant("VariantB").compilations();
		var articleOfWiki1 = wiki.manager.getArticle(FROM_WIKI_1);

		wiki.save("Import", variableImportMarkup("fgWiki", "Codes", "shared") + "some text\n");
		assertEquals(STARTED, wiki.state());
		assertSame(articleOfWiki1, wiki.manager.getArticle(FROM_WIKI_1));
		assertEquals(compilationsOfA, wiki.variant("VariantA").compilations());
		assertEquals(compilationsOfB, wiki.variant("VariantB").compilations());
	}

	@Test
	public void packageOfLiteralImportChanges() throws Exception {
		wiki.addAttachment("Parts/List.txt", contentMarkup("parts", null));
		wiki.save(literalImport("Parts", "wiki-1", "List", "vehicle-a"));
		assertEquals(expect(List.of(FROM_WIKI_1, FROM_WIKI_2, "Parts/List.txt"),
				"VariantA: local, from-wiki-1, parts",
				"VariantB: local, from-wiki-2"), wiki.state());

		wiki.save(literalImport("Parts", "wiki-1", "List", "vehicle-b"));
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_1, FROM_WIKI_2, "Parts/List.txt"),
				"VariantA: local, from-wiki-1",
				"VariantB: local, from-wiki-2, parts"));
	}

	@Test
	public void updateOfSourceAndPageOfImportAreCompiledTogether() throws Exception {
		// an update stores the attachment and writes the page of the import (its @latestChange), both within one
		// registration frame; that must be one compilation with the new content, and none for the stored attachment
		int compilations = wiki.compilations();
		int compilationsOfA = wiki.variant("VariantA").compilations();
		wiki.manager.open();
		try {
			wiki.storeAttachment(FROM_WIKI_2, contentMarkup("from-wiki-2-changed", null));
			wiki.save("Import", variableImportMarkup("fgWiki", "Codes", "shared") + "updated\n");
		}
		finally {
			wiki.manager.commit();
		}
		State expected = expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local, from-wiki-1",
				"VariantB: local, from-wiki-2-changed");
		wiki.await(expected, wiki::state);
		assertEquals(compilations + 1, wiki.compilations());
		assertEquals("the variant importing from the other wiki must not compile again",
				compilationsOfA, wiki.variant("VariantA").compilations());
		assertStateAlsoAfterRestart(expected);
	}

	/* ------------------------------------------ updates of the source ------------------------------------------ */

	@Test
	public void sourceOfOneVariantChanges() throws Exception {
		int compilationsOfA = wiki.variant("VariantA").compilations();
		int compilationsOfB = wiki.variant("VariantB").compilations();

		wiki.storeAttachment(FROM_WIKI_2, contentMarkup("from-wiki-2-changed", null));
		State expected = expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local, from-wiki-1",
				"VariantB: local, from-wiki-2-changed");
		wiki.await(expected, wiki::state);
		assertEquals("the variant importing from the other wiki must not compile again",
				compilationsOfA, wiki.variant("VariantA").compilations());
		assertEquals(compilationsOfB + 1, wiki.variant("VariantB").compilations());
		assertStateAlsoAfterRestart(expected);
	}

	@Test
	public void sourcesOfBothVariantsChange() throws Exception {
		wiki.storeAttachment(FROM_WIKI_1, contentMarkup("from-wiki-1-changed", null));
		wiki.storeAttachment(FROM_WIKI_2, contentMarkup("from-wiki-2-changed", null));
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local, from-wiki-1-changed",
				"VariantB: local, from-wiki-2-changed"));
	}

	@Test
	public void attachmentIsDeleted() throws Exception {
		wiki.deleteAttachment(FROM_WIKI_1);
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_2),
				"VariantA: local",
				"VariantB: local, from-wiki-2"));
	}

	@Test
	public void definitionChangesWhileUpdateArrives() throws Exception {
		wiki.addAttachment(FROM_WIKI_3, contentMarkup("from-wiki-3", null));
		for (int round = 0; round < 20; round++) {
			String value = round % 2 == 0 ? "wiki-3" : "wiki-1";
			wiki.storeAttachment(FROM_WIKI_1, contentMarkup("from-wiki-1-update-" + round, null));
			wiki.storeAttachment(FROM_WIKI_2, contentMarkup("from-wiki-2-update-" + round, null));
			wiki.save(variable("DefinitionA", "fgWiki", value, "vehicle-a"));
		}
		// the last round has set wiki-1 again
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local, from-wiki-1-update-19",
				"VariantB: local, from-wiki-2-update-19"));
	}
}
