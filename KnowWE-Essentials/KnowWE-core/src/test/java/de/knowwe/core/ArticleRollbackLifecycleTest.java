/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.Ignore;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;

import static de.knowwe.core.ArticleLifecycleFixture.line;
import static org.junit.Assert.*;

/**
 * Separates rollback coverage from construction isolation. Some of these contracts already fail before the planned
 * refactoring; keeping them explicit makes it possible to distinguish inherited rollback defects from new regressions.
 */
public class ArticleRollbackLifecycleTest {

	@Rule
	public final ArticleLifecycleFixture wiki = new ArticleLifecycleFixture();

	@Test
	public void rollbackNewArticleRemovesItsRegistrationsWithoutCompiling() {
		String id;
		Article created;
		wiki.manager.open();
		try {
			created = wiki.manager.registerArticle("New", "new\n");
			id = line(created, 0).getID();
			Messages.storeMessage(line(created, 0), getClass(), Messages.error("Discarded message"));
			wiki.manager.rollback();
		}
		finally {
			wiki.manager.commit(); // must be harmless after rollback closed this frame
		}
		assertNull(wiki.manager.getArticle("New"));
		assertNull(Sections.get(id));
		assertFalse(Messages.getSectionsWithMessages().contains(line(created, 0)));
		assertTrue(wiki.manager.getQueuedArticles().isEmpty());
		assertEquals(0, wiki.compiler.compilationCount());
	}

	@Test
	@Ignore("Rollback hardening is separate from the agreed intermediate lifecycle change: current rollback does not restore the original ID mapping")
	public void rollbackReplacementRestoresOriginalSectionIds() throws Exception {
		Article original = wiki.register("Page", "same\n");
		String id = line(original, 0).getID();
		wiki.manager.open();
		try {
			wiki.manager.registerArticle("Page", original.getText());
			wiki.manager.rollback();
		}
		finally {
			wiki.manager.commit();
		}
		assertSame(original, wiki.manager.getArticle("Page"));
		assertSame("ID lookup must agree with the restored article map", line(original, 0), Sections.get(id));
		assertEquals(1, wiki.compiler.compilationCount());
	}

	@Test
	@Ignore("Rollback hardening is separate from the agreed intermediate lifecycle change: current rollback does not restore original message tracking")
	public void rollbackReplacementRestoresOriginalMessageTracking() throws Exception {
		Article original = wiki.register("Page", "old\n");
		Messages.storeMessage(line(original, 0), getClass(), Messages.error("Original diagnostic"));
		Article replacement;
		wiki.manager.open();
		try {
			replacement = wiki.manager.registerArticle("Page", "new\n");
			Messages.storeMessage(line(replacement, 0), getClass(), Messages.error("Replacement diagnostic"));
			wiki.manager.rollback();
		}
		finally {
			wiki.manager.commit();
		}
		assertSame(original, wiki.manager.getArticle("Page"));
		assertTrue(Messages.getSectionsWithMessages().contains(line(original, 0)));
		assertFalse(Messages.getSectionsWithMessages().contains(line(replacement, 0)));
	}

	@Test
	@Ignore("Rollback hardening is separate from the agreed intermediate lifecycle change: current rollback can retain a newly created intermediate article")
	public void rollbackOfNewArticleWithMultipleVersionsDoesNotKeepAnIntermediateVersion() {
		wiki.manager.open();
		try {
			wiki.manager.registerArticle("New", "first\n");
			wiki.manager.registerArticle("New", "second\n");
			wiki.manager.rollback();
		}
		finally {
			wiki.manager.commit();
		}
		assertNull("The article did not exist before this frame", wiki.manager.getArticle("New"));
		assertEquals(0, wiki.compiler.compilationCount());
	}

	@Test
	public void outerRollbackAfterInnerCommitRestoresOriginalAndReleasesFrame() throws Exception {
		Article original = wiki.register("Page", "original\n");
		wiki.manager.open();
		try {
			wiki.manager.open();
			try {
				wiki.manager.registerArticle("Page", "intermediate\n");
				wiki.manager.registerArticle("Page", "final\n");
			}
			finally {
				wiki.manager.commit();
			}
			assertEquals(1, wiki.compiler.compilationCount());
			wiki.manager.rollback();
		}
		finally {
			wiki.manager.commit();
		}
		assertSame(original, wiki.manager.getArticle("Page"));
		assertTrue(wiki.manager.getQueuedArticles().isEmpty());
		assertEquals(1, wiki.compiler.compilationCount());

		ExecutorService nextWriter = Executors.newSingleThreadExecutor();
		try {
			Article next = nextWriter.submit(() -> wiki.manager.registerArticle("Next", "next\n")).get(5, TimeUnit.SECONDS);
			assertSame(next, wiki.manager.getArticle("Next"));
		}
		finally {
			nextWriter.shutdownNow();
			assertTrue(nextWriter.awaitTermination(5, TimeUnit.SECONDS));
		}
	}

	@Test
	public void rollbackDeletionRestoresArticleAndCancelsPendingDeletion() throws Exception {
		Article original = wiki.register("Page", "original\n");
		wiki.manager.open();
		try {
			wiki.manager.deleteArticle("Page");
			wiki.manager.rollback();
		}
		finally {
			wiki.manager.commit();
		}
		assertSame(original, wiki.manager.getArticle("Page"));
		assertTrue(Sections.isLive(original.getRootSection()));
		assertEquals(1, wiki.compiler.compilationCount());
		wiki.register("Other", "other\n");
		assertSame("A later commit must not apply the rolled-back deletion", original, wiki.manager.getArticle("Page"));
	}
}
