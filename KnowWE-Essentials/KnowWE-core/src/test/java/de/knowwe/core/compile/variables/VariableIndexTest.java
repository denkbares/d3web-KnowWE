/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.compile.variables;

import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.VariantAttachmentWiki;

import static de.knowwe.core.VariantAttachmentWiki.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Checks the {@link VariableIndex}: which definitions and values it provides, for the wiki and for a variant, and
 * that it already does so while the pages are registered.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
public class VariableIndexTest {

	private VariantAttachmentWiki wiki;
	private VariableIndex index;

	@Before
	public void createWiki() {
		wiki = new VariantAttachmentWiki();
		index = wiki.variables();
	}

	@After
	public void closeWiki() {
		wiki.close();
	}

	private void startWithTwoVariants() throws InterruptedException {
		wiki.start(List.of(
				variable("DefinitionA", "fgWiki", "wiki-1", "vehicle-a"),
				variable("DefinitionB", "fgWiki", "wiki-2", "vehicle-b"),
				variable("Other", "turmWiki", "wiki-9", "vehicle-a"),
				variant("VariantA", "shared", "vehicle-a"),
				variant("VariantB", "shared", "vehicle-b")));
	}

	@Test
	public void valuesAreAvailableAsSoonAsThePageIsRegistered() throws Exception {
		assertEquals("nothing registered yet", Set.of(), index.getValues("fgWiki"));

		DefaultArticleManager manager = (DefaultArticleManager) wiki.getEnvironment().getArticleManager();
		manager.open();
		try {
			manager.queueArticle("DefinitionA", variableMarkup("fgWiki", "wiki-1", "vehicle-a"));
			// not compiled yet, not even committed
			assertEquals(Set.of("wiki-1"), index.getValues("fgWiki"));
			manager.queueArticle("DefinitionB", variableMarkup("fgWiki", "wiki-2", "vehicle-b"));
			assertEquals(List.of("wiki-1", "wiki-2"), List.copyOf(index.getValues("fgWiki")));
		}
		finally {
			manager.commit();
		}
		wiki.awaitCompilation();
		assertEquals(List.of("wiki-1", "wiki-2"), List.copyOf(index.getValues("fgWiki")));
	}

	@Test
	public void valuesOfTheWikiAndOfAVariant() throws Exception {
		startWithTwoVariants();
		assertEquals(List.of("wiki-1", "wiki-2"), List.copyOf(index.getValues("fgWiki")));
		assertEquals(Set.of("wiki-1"), index.getValues("fgWiki", wiki.variant("VariantA")));
		assertEquals(Set.of("wiki-2"), index.getValues("fgWiki", wiki.variant("VariantB")));
		assertEquals(Set.of("wiki-9"), index.getValues("turmWiki", wiki.variant("VariantA")));
		assertEquals(Set.of(), index.getValues("turmWiki", wiki.variant("VariantB")));
		assertEquals(Set.of(), index.getValues("unknown"));

		assertEquals(1, index.getDefinitions("fgWiki", "wiki-1").size());
		assertEquals("DefinitionA", index.getDefinitions("fgWiki", "wiki-1").get(0).getTitle());
		assertEquals(List.of(), index.getDefinitions("fgWiki", "wiki-9"));
	}

	@Test
	public void sameValueDefinedTwiceIsOneValue() throws Exception {
		startWithTwoVariants();
		wiki.save(variable("DefinitionC", "fgWiki", "wiki-1", "vehicle-c"));
		assertEquals(List.of("wiki-1", "wiki-2"), List.copyOf(index.getValues("fgWiki")));
		assertEquals(2, index.getDefinitions("fgWiki", "wiki-1").size());
	}

	@Test
	public void newVersionOfPageReplacesItsDefinitions() throws Exception {
		startWithTwoVariants();
		wiki.save(variable("DefinitionA", "fgWiki", "wiki-3", "vehicle-a"));
		assertEquals(List.of("wiki-3", "wiki-2"), List.copyOf(index.getValues("fgWiki")));
		assertEquals(Set.of("wiki-3"), index.getValues("fgWiki", wiki.variant("VariantA")));

		wiki.save(variable("DefinitionA", "renamed", "wiki-3", "vehicle-a"));
		assertEquals(Set.of("wiki-2"), index.getValues("fgWiki"));
		assertEquals(Set.of("wiki-3"), index.getValues("renamed"));

		wiki.save("DefinitionA", "no definition any more");
		assertEquals(Set.of(), index.getValues("renamed"));

		wiki.delete("DefinitionB");
		assertEquals(Set.of(), index.getValues("fgWiki"));
	}

	@Test
	public void severalDefinitionsOnOnePage() throws Exception {
		wiki.start(List.of(new Page("Definitions", variableMarkup("fgWiki", "wiki-1", "vehicle-a")
				+ "some text\n" + variableMarkup("fgWiki", "wiki-2", "vehicle-b")
				+ variableMarkup("turmWiki", "wiki-9", null))));
		assertEquals(List.of("wiki-1", "wiki-2"), List.copyOf(index.getValues("fgWiki")));
		assertEquals(Set.of("wiki-9"), index.getValues("turmWiki"));
	}

	@Test
	public void incompleteDefinitionsAreIgnored() throws Exception {
		wiki.start(List.of(
				new Page("NoValue", "%%InterWikiVariable\n@name: fgWiki\n%\n"),
				new Page("NoName", "%%InterWikiVariable\n@value: wiki-1\n%\n")));
		assertEquals(Set.of(), index.getValues("fgWiki"));
	}

	@Test
	public void definitionsInCompiledAttachmentsAreIgnored() throws Exception {
		String attachment = "Import/Codes@wiki-1.txt";
		wiki.addAttachment(attachment, variableMarkup("fgWiki", "wiki-7", "vehicle-a") + contentMarkup("from-wiki-1", null));
		wiki.start(List.of(
				variableImport("Import", "fgWiki", "Codes", "shared"),
				variable("DefinitionA", "fgWiki", "wiki-1", "vehicle-a"),
				variant("VariantA", "shared", "vehicle-a")));

		assertEquals(Set.of("wiki-1"), index.getValues("fgWiki"));
		assertEquals(expect(List.of(attachment), "VariantA: from-wiki-1"), wiki.state());
		List<String> messages = wiki.messages(attachment);
		assertTrue("Expected a warning at the ignored definition, but got " + messages,
				messages.stream().anyMatch(message -> message.contains("are ignored")));
	}
}
