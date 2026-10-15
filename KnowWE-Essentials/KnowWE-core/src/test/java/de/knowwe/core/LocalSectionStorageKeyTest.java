/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import java.util.HashSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.kdom.Article;

import static de.knowwe.core.ArticleLifecycleFixture.line;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * The key of a section's local storage has to survive the changes of the section ids, which happen with every edit of
 * the article and every restart of the wiki, otherwise the user's settings for the section get lost.
 */
public class LocalSectionStorageKeyTest {

	@Rule
	public final ArticleLifecycleFixture wiki = new ArticleLifecycleFixture();

	@Test
	public void keyIsKeptWhenTheArticleIsEditedElsewhere() throws Exception {
		Article original = wiki.register("Page", "first\nsecond\nthird\n");
		String id = line(original, 2).getID();
		String key = AbstractAction.getLocalSectionStorageKey(line(original, 2));

		Article edited = queue("Page", "first, but edited\nsecond\nthird\n");
		assertNotEquals("Precondition: the edit changes the section ids", id, line(edited, 2).getID());
		assertEquals(key, AbstractAction.getLocalSectionStorageKey(line(edited, 2)));

		// the content is the same again, but the ids are not
		Article reverted = queue("Page", "first\nsecond\nthird\n");
		assertNotEquals(id, line(reverted, 2).getID());
		assertEquals(key, AbstractAction.getLocalSectionStorageKey(line(reverted, 2)));
	}

	@Test
	public void keysAreDistinctPerSectionAndArticle() throws Exception {
		Article page = wiki.register("Page", "same\nsame\nsame\n");
		Article other = wiki.register("Other", "same\nsame\nsame\n");
		Set<String> keys = new HashSet<>();
		for (Article article : new Article[] { page, other }) {
			for (int i = 0; i < 3; i++) {
				keys.add(AbstractAction.getLocalSectionStorageKey(line(article, i)));
			}
		}
		keys.add(AbstractAction.getLocalSectionStorageKey(page.getRootSection()));
		assertEquals(7, keys.size());
	}

	@Test
	public void keyOfSectionIdIsLookedUp() throws Exception {
		Article page = wiki.register("Page", "first\nsecond\n");
		assertEquals(AbstractAction.getLocalSectionStorageKey(line(page, 1)),
				AbstractAction.getLocalSectionStorageKey(line(page, 1).getID()));
		assertEquals("unknownId", AbstractAction.getLocalSectionStorageKey("unknownId"));
	}

	private Article queue(String title, String text) throws InterruptedException {
		wiki.manager.open();
		try {
			wiki.manager.queueArticle(title, text);
		}
		finally {
			wiki.manager.commit();
		}
		wiki.awaitCompilation();
		return wiki.manager.getArticle(title);
	}
}
