/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.include;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.include.InterWikiImportTestWiki.Page;
import de.knowwe.include.InterWikiImportTestWiki.SourceWikis;
import de.knowwe.include.InterWikiImportTestWiki.State;
import utils.TestUserContext;

import static de.knowwe.include.InterWikiImportTestWiki.*;
import static org.junit.Assert.*;

/**
 * Checks <code>%%InterWikiImport</code> with <code>@wikiVariable</code>: the import gets one attachment per source
 * wiki, each compiled by the variants whose definition of the variable names that wiki. No test imports from another
 * wiki: {@link SourceWikis} answers the requests of the update service, everything else is the usual mechanism.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
public class InterWikiVariableImportTest {

	private static final String WIKI_1 = "https://fg-wiki-1.example.com";
	private static final String WIKI_2 = "https://fg-wiki-2.example.com";
	private static final String WIKI_3 = "https://fg-wiki-3.example.com";

	private static final String FROM_WIKI_1 = "Import/WikiImport-Codes@fg-wiki-1.example.com.txt";
	private static final String FROM_WIKI_2 = "Import/WikiImport-Codes@fg-wiki-2.example.com.txt";
	private static final String FROM_WIKI_3 = "Import/WikiImport-Codes@fg-wiki-3.example.com.txt";

	private static final Instant T1 = Instant.parse("2026-11-01T10:00:00Z");
	private static final Instant T2 = Instant.parse("2026-11-02T10:00:00Z");

	private static final Page IMPORT = variableImport("Import", "fgWiki", "Codes", "shared");
	private static final Page DEFINITION_A = variable("DefinitionA", "fgWiki", WIKI_1, "vehicle-a");
	private static final Page DEFINITION_B = variable("DefinitionB", "fgWiki", WIKI_2, "vehicle-b");
	private static final Page VARIANT_A = variant("VariantA", "shared", "vehicle-a");
	private static final Page VARIANT_B = variant("VariantB", "shared", "vehicle-b");
	private static final Page LOCAL = content("Local", "local", "shared");

	private static final List<Page> PAGES = List.of(IMPORT, DEFINITION_A, DEFINITION_B, VARIANT_A, VARIANT_B, LOCAL);

	private static final State NOTHING_IMPORTED = expect(List.of(),
			"VariantA: local",
			"VariantB: local");
	private static final State IMPORTED = expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
			"VariantA: local, codes-of-wiki-1",
			"VariantB: local, codes-of-wiki-2");

	private InterWikiImportTestWiki wiki;
	private SourceWikis sources;

	@Before
	public void createWiki() {
		wiki = new InterWikiImportTestWiki();
		sources = wiki.sources;
		sources.setPage(WIKI_1, "Codes", contentMarkup("codes-of-wiki-1", null), T1);
		sources.setPage(WIKI_2, "Codes", contentMarkup("codes-of-wiki-2", null), T1);
		sources.setPage(WIKI_3, "Codes", contentMarkup("codes-of-wiki-3", null), T1);
	}

	@After
	public void closeWiki() {
		wiki.close();
	}

	private void startAndImport() throws InterruptedException {
		wiki.start(PAGES);
		wiki.poll();
		assertEquals(IMPORTED, wiki.state());
		sources.takeRequests();
	}

	private List<String> latestChanges() {
		List<String> lines = new ArrayList<>(wiki.lines("Import", "@latestChange"));
		Collections.sort(lines);
		return lines;
	}

	/**
	 * The messages at the import about failed checks of its source wikis.
	 */
	private List<String> syncMessages() {
		return wiki.messages("Import").stream().filter(message -> message.startsWith("Last attempt")).toList();
	}

	private void assertStateAlsoAfterRestart(State expected) throws Exception {
		assertEquals(expected, wiki.state());
		assertEquals("State after restart", expected, wiki.stateAfterRestart());
	}

	/* ------------------------------------------------ import ------------------------------------------------ */

	@Test
	public void firstPollImportsEachSourceIntoItsOwnAttachment() throws Exception {
		wiki.start(PAGES);
		assertEquals(NOTHING_IMPORTED, wiki.state());

		wiki.poll();
		assertEquals(List.of(WIKI_1 + "/ Codes@null", WIKI_2 + "/ Codes@null"), sources.takeRequests());
		// the import writes its package into the imported markups, as it does without a variable
		assertEquals(contentMarkup("codes-of-wiki-1\n", "shared"), wiki.attachmentText(FROM_WIKI_1));
		assertEquals(contentMarkup("codes-of-wiki-2\n", "shared"), wiki.attachmentText(FROM_WIKI_2));
		assertEquals(List.of(
				"@latestChange: " + T1 + " " + WIKI_1 + "/",
				"@latestChange: " + T1 + " " + WIKI_2 + "/"), latestChanges());
		assertEquals(List.of(), syncMessages());
		assertStateAlsoAfterRestart(IMPORTED);
	}

	@Test
	public void onlyTheChangedSourceIsImportedAgain() throws Exception {
		startAndImport();
		Article articleOfWiki1 = wiki.manager.getArticle(FROM_WIKI_1);
		int compilationsOfA = wiki.variant("VariantA").contentCompilations();
		int compilationsOfB = wiki.variant("VariantB").contentCompilations();

		sources.setPage(WIKI_2, "Codes", contentMarkup("codes-of-wiki-2-changed", null), T2);
		wiki.poll();

		// each source is asked with the latest change imported from it
		assertEquals(List.of(WIKI_1 + "/ Codes@" + T1, WIKI_2 + "/ Codes@" + T1), sources.takeRequests());
		assertEquals(List.of(
				"@latestChange: " + T1 + " " + WIKI_1 + "/",
				"@latestChange: " + T2 + " " + WIKI_2 + "/"), latestChanges());
		assertSame("the content of the unchanged source must not be parsed again",
				articleOfWiki1, wiki.manager.getArticle(FROM_WIKI_1));
		assertEquals("the variant of the unchanged source must not compile its content again",
				compilationsOfA, wiki.variant("VariantA").contentCompilations());
		assertEquals("the variant of the changed source compiles the new content, once",
				compilationsOfB + 1, wiki.variant("VariantB").contentCompilations());
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local, codes-of-wiki-1",
				"VariantB: local, codes-of-wiki-2-changed"));
	}

	@Test
	public void bothSourcesChangeWithOnePoll() throws Exception {
		startAndImport();
		sources.setPage(WIKI_1, "Codes", contentMarkup("codes-of-wiki-1-changed", null), T2);
		sources.setPage(WIKI_2, "Codes", contentMarkup("codes-of-wiki-2-changed", null), T2);
		wiki.poll();

		assertEquals(List.of(
				"@latestChange: " + T2 + " " + WIKI_1 + "/",
				"@latestChange: " + T2 + " " + WIKI_2 + "/"), latestChanges());
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local, codes-of-wiki-1-changed",
				"VariantB: local, codes-of-wiki-2-changed"));
	}

	@Test
	public void unchangedSourcesChangeNothing() throws Exception {
		startAndImport();
		String pageText = wiki.pageText("Import");
		Article page = wiki.manager.getArticle("Import");
		Article articleOfWiki1 = wiki.manager.getArticle(FROM_WIKI_1);
		Article articleOfWiki2 = wiki.manager.getArticle(FROM_WIKI_2);

		wiki.poll();
		assertEquals(List.of(WIKI_1 + "/ Codes@" + T1, WIKI_2 + "/ Codes@" + T1), sources.takeRequests());
		assertEquals(pageText, wiki.pageText("Import"));
		assertSame(page, wiki.manager.getArticle("Import"));
		assertSame(articleOfWiki1, wiki.manager.getArticle(FROM_WIKI_1));
		assertSame(articleOfWiki2, wiki.manager.getArticle(FROM_WIKI_2));
		assertEquals(IMPORTED, wiki.state());
	}

	/* ------------------------------------------- failing sources ------------------------------------------- */

	@Test
	public void unreachableSourceDoesNotBlockTheOther() throws Exception {
		sources.setUnreachable(WIKI_1, true);
		wiki.start(PAGES);
		wiki.poll();

		assertEquals(expect(List.of(FROM_WIKI_2),
				"VariantA: local",
				"VariantB: local, codes-of-wiki-2"), wiki.state());
		List<String> messages = syncMessages();
		assertEquals("Expected one message for the failing source, but got " + messages, 1, messages.size());
		assertTrue(messages.get(0), messages.get(0).contains(WIKI_1 + "/"));
		assertTrue(messages.get(0), messages.get(0).contains("Connection refused"));

		sources.setUnreachable(WIKI_1, false);
		wiki.poll();
		assertEquals(List.of(), syncMessages());
		assertStateAlsoAfterRestart(IMPORTED);
	}

	@Test
	public void notAuthorizedSourceDoesNotBlockTheOther() throws Exception {
		startAndImport();
		sources.setNotAuthorized(WIKI_2, true);
		sources.setPage(WIKI_1, "Codes", contentMarkup("codes-of-wiki-1-changed", null), T2);
		sources.setPage(WIKI_2, "Codes", contentMarkup("codes-of-wiki-2-changed", null), T2);
		wiki.poll();

		assertEquals(expect(List.of(FROM_WIKI_1, FROM_WIKI_2),
				"VariantA: local, codes-of-wiki-1-changed",
				"VariantB: local, codes-of-wiki-2"), wiki.state());
		List<String> messages = syncMessages();
		assertEquals("Expected one message for the failing source, but got " + messages, 1, messages.size());
		assertTrue(messages.get(0), messages.get(0).contains(WIKI_2 + "/"));
		assertTrue(messages.get(0), messages.get(0).contains("not authorized"));
	}

	/* --------------------------------------- changes of the definitions --------------------------------------- */

	@Test
	public void valueOfVariableChangesToAnotherWiki() throws Exception {
		startAndImport();
		wiki.save(variable("DefinitionA", "fgWiki", WIKI_3, "vehicle-a"));
		assertEquals(expect(List.of(FROM_WIKI_2),
				"VariantA: local",
				"VariantB: local, codes-of-wiki-2"), wiki.state());

		wiki.poll();
		// the new source is imported completely, the others only if they have changed
		assertEquals(List.of(WIKI_2 + "/ Codes@" + T1, WIKI_3 + "/ Codes@null"), sources.takeRequests());
		assertEquals(List.of(
				"@latestChange: " + T1 + " " + WIKI_2 + "/",
				"@latestChange: " + T1 + " " + WIKI_3 + "/"), latestChanges());
		assertFalse("the attachment of the previous source must be deleted", wiki.attachments().contains(FROM_WIKI_1));
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_2, FROM_WIKI_3),
				"VariantA: local, codes-of-wiki-3",
				"VariantB: local, codes-of-wiki-2"));
	}

	@Test
	public void sourceReturnsAfterItsAttachmentWasDeleted() throws Exception {
		startAndImport();
		// variant B imports from wiki 1 for a while: nothing new to fetch, the attachment of wiki 2 is deleted
		wiki.save(variable("DefinitionB", "fgWiki", WIKI_1, "vehicle-b"));
		wiki.poll();
		assertEquals(List.of(FROM_WIKI_1), List.copyOf(wiki.attachments()));
		assertEquals(expect(List.of(FROM_WIKI_1),
				"VariantA: local, codes-of-wiki-1",
				"VariantB: local, codes-of-wiki-1"), wiki.state());
		sources.takeRequests();

		// back to wiki 2: its content has not changed since the last import, but has to be fetched again
		wiki.save(DEFINITION_B);
		wiki.poll();
		assertEquals(List.of(WIKI_1 + "/ Codes@" + T1, WIKI_2 + "/ Codes@null"), sources.takeRequests());
		assertEquals(List.of(
				"@latestChange: " + T1 + " " + WIKI_1 + "/",
				"@latestChange: " + T1 + " " + WIKI_2 + "/"), latestChanges());
		assertStateAlsoAfterRestart(IMPORTED);
	}

	@Test
	public void definitionIsRemoved() throws Exception {
		startAndImport();
		wiki.delete("DefinitionA");
		wiki.poll();

		assertEquals(List.of(WIKI_2 + "/ Codes@" + T1), sources.takeRequests());
		assertEquals(List.of(FROM_WIKI_2), List.copyOf(wiki.attachments()));
		assertStateAlsoAfterRestart(expect(List.of(FROM_WIKI_2),
				"VariantA: local",
				"VariantB: local, codes-of-wiki-2"));
	}

	@Test
	public void definitionsWithoutPackageUseTheDefaultPackageOfTheirPage() throws Exception {
		List<Page> pages = List.of(IMPORT,
				new Page("DefinitionA", "%%Package vehicle-a\n\n" + variable("", "fgWiki", WIKI_1, null).text()),
				new Page("DefinitionB", variable("", "fgWiki", WIKI_2, null).text() + "\n%%Package vehicle-b\n"),
				VARIANT_A, VARIANT_B, LOCAL);
		List<Page> reversed = new ArrayList<>(pages);
		Collections.reverse(reversed);
		for (List<Page> order : List.of(pages, reversed)) {
			try (InterWikiImportTestWiki orderedWiki = new InterWikiImportTestWiki(sources)) {
				orderedWiki.start(order);
				orderedWiki.poll();
				assertEquals("Order " + order, IMPORTED, orderedWiki.state());
				assertEquals("Order " + order, IMPORTED, orderedWiki.stateAfterRestart());
			}
		}
	}

	/* ----------------------------------------- changes of the import ----------------------------------------- */

	@Test
	public void importSwitchesFromWikiToVariable() throws Exception {
		String legacyAttachment = "Import/WikiImport-Codes.txt";
		wiki.start(List.of(literalImport("Import", WIKI_1, "Codes", "shared"),
				DEFINITION_A, DEFINITION_B, VARIANT_A, VARIANT_B, LOCAL));
		wiki.poll();
		assertEquals(expect(List.of(legacyAttachment),
				"VariantA: local, codes-of-wiki-1",
				"VariantB: local, codes-of-wiki-1"), wiki.state());
		assertEquals(List.of("@latestChange: " + T1), latestChanges());

		// what the migration will do: replace the URL by the variable, everything else stays
		String migrated = wiki.pageText("Import").replace("@wiki: " + WIKI_1, "@wikiVariable: fgWiki");
		wiki.save(new Page("Import", migrated));
		assertNull(wiki.manager.getArticle(legacyAttachment));
		wiki.poll();

		assertEquals(List.of(
				"@latestChange: " + T1 + " " + WIKI_1 + "/",
				"@latestChange: " + T1 + " " + WIKI_2 + "/"), latestChanges());
		assertStateAlsoAfterRestart(IMPORTED);
	}

	@Test
	public void sourceMustBeGivenExactlyOnce() throws Exception {
		wiki.start(List.of(
				new Page("Neither", "%%InterWikiImport\n@page: Codes\n%\n"),
				new Page("Both", "%%InterWikiImport\n@wiki: " + WIKI_1 + "\n@wikiVariable: fgWiki\n@page: Codes\n%\n"),
				DEFINITION_A, VARIANT_A));
		assertTrue(wiki.messages("Neither").toString(),
				wiki.messages("Neither").stream().anyMatch(m -> m.contains("Either @wiki or @wikiVariable")));
		assertTrue(wiki.messages("Both").toString(),
				wiki.messages("Both").stream().anyMatch(m -> m.contains("must not be specified together")));
	}

	/* ------------------------------------------------ tracking ------------------------------------------------ */

	@Test
	public void trackingFollowsTheOneWikiOfTheVariable() throws Exception {
		Page tracking = new Page("Import", "%%InterWikiImport\n@wikiVariable: fgWiki\n@page: Codes\n@mode: tracking\n%\n");
		wiki.start(List.of(tracking, DEFINITION_A, VARIANT_A, VARIANT_B, LOCAL));
		wiki.poll();

		Section<InterWikiImportMarkup> markup = wiki.importMarkup("Import");
		assertEquals(contentMarkup("codes-of-wiki-1", null), markup.get().getTrackingReferenceText(markup));
		// the reference of a tracking import is not compiled
		assertEquals(NOTHING_IMPORTED, wiki.state());
	}

	@Test
	public void trackingIsNotPossibleWithSeveralWikis() throws Exception {
		Page tracking = new Page("Import", "%%InterWikiImport\n@wikiVariable: fgWiki\n@page: Codes\n@mode: tracking\n%\n");
		wiki.start(List.of(tracking, DEFINITION_A, DEFINITION_B, VARIANT_A, VARIANT_B, LOCAL));
		wiki.poll();

		assertEquals(List.of(), sources.takeRequests());
		assertEquals(List.of(), List.copyOf(wiki.attachments()));
		Section<InterWikiImportMarkup> markup = wiki.importMarkup("Import");
		assertNull(markup.get().getTrackingReferenceText(markup));
	}

	/* --------------------------------------------- selected variant --------------------------------------------- */

	@Test
	public void sourceOfTheVariantSelectedByTheUser() throws Exception {
		startAndImport();
		Section<InterWikiImportMarkup> markup = wiki.importMarkup("Import");
		TestUserContext user = new TestUserContext(wiki.environment, "Import");

		Compilers.markAsDefaultCompiler(user, wiki.variant("VariantA"));
		assertEquals(WIKI_1 + "/", markup.get().getWiki(markup, user));
		assertEquals(FROM_WIKI_1, markup.get().getWikiAttachmentPath(markup, user));
		assertNull(markup.get().getMissingSourceMessage(markup, user));

		assertEquals(List.of("DefinitionA"),
				markup.get().getVariableDefinitions(markup, user).stream().map(Section::getTitle).toList());

		Compilers.markAsDefaultCompiler(user, wiki.variant("VariantB"));
		assertEquals(WIKI_2 + "/", markup.get().getWiki(markup, user));
		assertEquals(FROM_WIKI_2, markup.get().getWikiAttachmentPath(markup, user));
		assertEquals(List.of("DefinitionB"),
				markup.get().getVariableDefinitions(markup, user).stream().map(Section::getTitle).toList());
		assertEquals(T1, markup.get().getLatestChange(markup, markup.get().getWiki(markup, user)));

		// the import as a whole has no single source
		assertNull(markup.get().getWiki(markup));
		assertNull(markup.get().getWikiAttachmentPath(markup));
		assertEquals(List.of(WIKI_1 + "/", WIKI_2 + "/"), markup.get().getWikis(markup));
	}

	@Test
	public void selectedVariantWithoutValueOrWithSeveralValues() throws Exception {
		// variant B has no value, variant C has two
		wiki.start(List.of(IMPORT, DEFINITION_A, VARIANT_A, VARIANT_B, LOCAL,
				variant("VariantC", "shared", "vehicle-c"),
				variable("DefinitionC1", "fgWiki", WIKI_2, "vehicle-c"),
				variable("DefinitionC2", "fgWiki", WIKI_3, "vehicle-c")));
		wiki.poll();
		Section<InterWikiImportMarkup> markup = wiki.importMarkup("Import");
		TestUserContext user = new TestUserContext(wiki.environment, "Import");

		Compilers.markAsDefaultCompiler(user, wiki.variant("VariantB"));
		assertNull(markup.get().getWiki(markup, user));
		assertNull(markup.get().getWikiAttachmentPath(markup, user));
		assertTrue(markup.get().getMissingSourceMessage(markup, user).contains("has no value for the currently selected variant"));

		Compilers.markAsDefaultCompiler(user, wiki.variant("VariantC"));
		assertNull(markup.get().getWiki(markup, user));
		assertTrue(markup.get().getMissingSourceMessage(markup, user).contains("has several values for the currently selected variant"));

		Compilers.markAsDefaultCompiler(user, wiki.variant("VariantA"));
		assertEquals(WIKI_1 + "/", markup.get().getWiki(markup, user));
	}

	@Test
	public void selectedVariantWithoutValueWhileAllOthersAgree() throws Exception {
		// the variable has one value in the whole wiki, but none for variant B
		wiki.start(List.of(IMPORT, DEFINITION_A, VARIANT_A, VARIANT_B, LOCAL));
		wiki.poll();
		Section<InterWikiImportMarkup> markup = wiki.importMarkup("Import");
		TestUserContext user = new TestUserContext(wiki.environment, "Import");

		Compilers.markAsDefaultCompiler(user, wiki.variant("VariantB"));
		assertNull("variant B must not be shown what it does not compile", markup.get().getWiki(markup, user));
		assertNull(markup.get().getWikiAttachmentPath(markup, user));
		assertTrue(markup.get().getMissingSourceMessage(markup, user).contains("has no value for the currently selected variant"));

		Compilers.markAsDefaultCompiler(user, wiki.variant("VariantA"));
		assertEquals(WIKI_1 + "/", markup.get().getWiki(markup, user));
		assertNull(markup.get().getMissingSourceMessage(markup, user));
	}

	@Test
	public void displayNameOfTheDefinitionOfTheSelectedVariant() throws Exception {
		// variant A names its wiki, variant B does not
		wiki.start(List.of(IMPORT, VARIANT_A, VARIANT_B, LOCAL, DEFINITION_B,
				new Page("DefinitionA", "%%InterWikiVariable\n@name: fgWiki\n@value: " + WIKI_1
						+ "\n@displayName: FG-Wiki A\n@package: vehicle-a\n%\n")));
		wiki.poll();
		assertEquals(IMPORTED, wiki.state());
		Section<InterWikiImportMarkup> markup = wiki.importMarkup("Import");
		TestUserContext user = new TestUserContext(wiki.environment, "Import");

		Compilers.markAsDefaultCompiler(user, wiki.variant("VariantA"));
		assertEquals("FG-Wiki A", markup.get().getSourceDisplayName(markup, user));
		Compilers.markAsDefaultCompiler(user, wiki.variant("VariantB"));
		assertEquals("without display name the variable itself is shown", "fgWiki", markup.get().getSourceDisplayName(markup, user));

		// the wikis themselves are named by the display name too, e.g. in the list of the sources
		assertEquals("FG-Wiki A", markup.get().getWikiDisplayName(markup, WIKI_1 + "/"));
		assertEquals("fg-wiki-2.example.com", markup.get().getWikiDisplayName(markup, WIKI_2 + "/"));

		// an import with @wiki has no such name
		wiki.save(literalImport("Literal", WIKI_1, "Codes", "shared"));
		Section<InterWikiImportMarkup> literal = wiki.importMarkup("Literal");
		assertNull(literal.get().getSourceDisplayName(literal, user));
	}

	@Test
	public void undefinedVariable() throws Exception {
		wiki.start(List.of(IMPORT, VARIANT_A, LOCAL));
		wiki.poll();
		assertEquals(List.of(), sources.takeRequests());
		Section<InterWikiImportMarkup> markup = wiki.importMarkup("Import");
		TestUserContext user = new TestUserContext(wiki.environment, "Import");
		assertTrue(markup.get().getMissingSourceMessage(markup, user).contains("is not defined"));
	}

	/* ------------------------------------------- check per variant ------------------------------------------- */

	@Test
	public void properlyDefinedVariableHasNoMessages() throws Exception {
		startAndImport();
		for (String variant : List.of("VariantA", "VariantB")) {
			for (String page : List.of("Import", "DefinitionA", "DefinitionB")) {
				assertEquals(variant + " at " + page, List.of(), wiki.variableMessages(variant, page));
			}
		}
	}

	@Test
	public void variantWithoutValueGetsWarningAtImport() throws Exception {
		wiki.start(List.of(IMPORT, DEFINITION_A, VARIANT_A, VARIANT_B, LOCAL));
		assertEquals(List.of(), wiki.variableMessages("VariantA", "Import"));
		List<String> messages = wiki.variableMessages("VariantB", "Import");
		assertEquals(messages.toString(), 1, messages.size());
		assertTrue(messages.get(0), messages.get(0).startsWith("WARNING: Nothing is imported in this variant"));

		// the message goes away as soon as the variant gets a definition, without the import being changed
		wiki.save(DEFINITION_B);
		assertEquals(List.of(), wiki.variableMessages("VariantB", "Import"));
		assertEquals(List.of(), wiki.variableMessages("VariantA", "Import"));

		// and comes back, when the definition is removed again
		wiki.delete("DefinitionB");
		assertEquals(1, wiki.variableMessages("VariantB", "Import").size());
	}

	@Test
	public void conflictingDefinitionsGetMessagesAtImportAndDefinitions() throws Exception {
		startAndImport();
		// a second, different value for variant A on another page
		wiki.save(variable("DefinitionA2", "fgWiki", WIKI_2, "vehicle-a"));

		List<String> atImport = wiki.variableMessages("VariantA", "Import");
		assertEquals(atImport.toString(), 1, atImport.size());
		assertTrue(atImport.get(0), atImport.get(0).startsWith("ERROR: The wiki variable 'fgWiki' has several values"));
		assertTrue(atImport.get(0), atImport.get(0).contains(WIKI_1 + "/ (defined on DefinitionA)"));
		assertTrue(atImport.get(0), atImport.get(0).contains(WIKI_2 + "/ (defined on DefinitionA2)"));

		// both definitions point to the other one, also the one that was not changed
		List<String> atFirst = wiki.variableMessages("VariantA", "DefinitionA");
		assertEquals(atFirst.toString(), 1, atFirst.size());
		assertTrue(atFirst.get(0), atFirst.get(0).startsWith("WARNING: The wiki variable 'fgWiki' has other values"));
		assertTrue(atFirst.get(0), atFirst.get(0).contains(WIKI_2 + "/ (defined on DefinitionA2)"));
		List<String> atSecond = wiki.variableMessages("VariantA", "DefinitionA2");
		assertEquals(atSecond.toString(), 1, atSecond.size());
		assertTrue(atSecond.get(0), atSecond.get(0).contains(WIKI_1 + "/ (defined on DefinitionA)"));

		// the other variant is not affected
		for (String page : List.of("Import", "DefinitionB")) {
			assertEquals(page, List.of(), wiki.variableMessages("VariantB", page));
		}

		// solving the conflict clears all messages, whichever definition is changed
		wiki.save(variable("DefinitionA", "fgWiki", WIKI_2, "vehicle-a"));
		for (String page : List.of("Import", "DefinitionA", "DefinitionA2")) {
			assertEquals(page, List.of(), wiki.variableMessages("VariantA", page));
		}
	}

	@Test
	public void sameValueDefinedTwiceIsNoConflict() throws Exception {
		startAndImport();
		wiki.save(variable("DefinitionA2", "fgWiki", WIKI_1 + "/", "vehicle-a"));
		for (String page : List.of("Import", "DefinitionA", "DefinitionA2")) {
			assertEquals(page, List.of(), wiki.variableMessages("VariantA", page));
		}
	}

	@Test
	public void conflictCausedByPackageOfDefinition() throws Exception {
		startAndImport();
		// the definition of wiki 2 now belongs to variant A too, e.g. by a wrong package
		wiki.save(variable("DefinitionB", "fgWiki", WIKI_2, "vehicle-a"));
		assertEquals(1, wiki.variableMessages("VariantA", "Import").size());
		assertEquals(1, wiki.variableMessages("VariantA", "DefinitionA").size());
		assertEquals(1, wiki.variableMessages("VariantA", "DefinitionB").size());
		// variant B has lost its only definition
		List<String> atImportOfB = wiki.variableMessages("VariantB", "Import");
		assertEquals(atImportOfB.toString(), 1, atImportOfB.size());
		assertTrue(atImportOfB.get(0), atImportOfB.get(0).startsWith("WARNING: Nothing is imported"));

		wiki.save(DEFINITION_B);
		for (String variant : List.of("VariantA", "VariantB")) {
			for (String page : List.of("Import", "DefinitionA", "DefinitionB")) {
				assertEquals(variant + " at " + page, List.of(), wiki.variableMessages(variant, page));
			}
		}
	}

	/* ------------------------------------------------ formats ------------------------------------------------ */

	@Test
	public void sourceKeyOfWiki() {
		assertEquals("fg-wiki-1.example.com", InterWikiImportAnnotationFormat.toSourceKey("https://FG-Wiki-1.example.com/"));
		assertEquals("fg-wiki-1.example.com", InterWikiImportAnnotationFormat.toSourceKey("fg-wiki-1.example.com"));
		assertEquals("wiki.example.com_KnowWE", InterWikiImportAnnotationFormat.toSourceKey("http://wiki.example.com/KnowWE/"));
		assertEquals("credentials must not get into file names",
				"wiki.example.com_8080_KnowWE", InterWikiImportAnnotationFormat.toSourceKey("https://user:secret@wiki.example.com:8080/KnowWE"));
	}

	@Test
	public void latestChangeWithAndWithoutWiki() {
		InterWikiImportMarkup.LatestChange plain = InterWikiImportMarkup.LatestChange.parse(" " + T1 + " ");
		assertEquals(T1, plain.instant());
		assertNull(plain.wiki());

		InterWikiImportMarkup.LatestChange withWiki = InterWikiImportMarkup.LatestChange.parse(T1 + "  FG-Wiki-1.example.com");
		assertEquals(T1, withWiki.instant());
		assertEquals(WIKI_1 + "/", withWiki.wiki());

		assertNull(InterWikiImportMarkup.LatestChange.parse("not a time " + WIKI_1));
		assertNull(InterWikiImportMarkup.LatestChange.parse(null));
	}
}
