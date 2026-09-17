/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Rule;
import org.junit.Test;
import org.junit.Ignore;

import com.denkbares.events.EventManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;
import de.knowwe.event.ArticleRegisteredEvent;
import de.knowwe.event.FullParseEvent;

import static de.knowwe.core.ArticleLifecycleFixture.line;
import static org.junit.Assert.*;

/**
 * Exercises registration frames through the real CompilerManager, including reentrant events and real attachment
 * registration. Article visibility is deliberately checked before commit: construction isolation must not introduce
 * transaction-local article views. The contribution test is a small public lifecycle fixture, not a replacement for
 * the private CBX integration test or for d3web/ontology compiler integration tests.
 */
public class ArticleCompilationLifecycleTest {

	@Rule
	public final ArticleLifecycleFixture wiki = new ArticleLifecycleFixture();

	@Test
	public void multipleReplacementsCompileOnlyOriginalRemovalAndFinalAddition() throws Exception {
		Article original = wiki.register("Page", "version A\n");
		String originalId = line(original, 0).getID();
		int runsBefore = wiki.compiler.compilationCount();
		Article intermediate;
		Article replacement;
		String intermediateId;
		wiki.manager.open();
		try {
			intermediate = wiki.manager.registerArticle("Page", "version B\n");
			intermediateId = line(intermediate, 0).getID();
			Messages.storeMessage(line(intermediate, 0), getClass(), Messages.error("Intermediate diagnostic"));
			replacement = wiki.manager.registerArticle("Page", "version C\n");
			assertSame(replacement, wiki.manager.getArticle("Page"));
			assertEquals(1, wiki.manager.getQueuedArticles().size());
			assertSame(replacement, wiki.manager.getQueuedArticles().iterator().next());
			assertEquals("No compilation before the outer commit", runsBefore, wiki.compiler.compilationCount());
		}
		finally {
			wiki.manager.commit();
		}
		wiki.awaitCompilation();
		assertEquals(runsBefore + 1, wiki.compiler.compilationCount());
		assertEquals(List.of(original.getRootSection()), wiki.compiler.lastCompilation().removed());
		assertEquals(List.of(replacement.getRootSection()), wiki.compiler.lastCompilation().added());
		assertNull(Sections.get(originalId));
		assertNull(Sections.get(intermediateId));
		assertFalse(Messages.getSectionsWithMessages().contains(line(intermediate, 0)));
		assertEquals(Map.of("version C", 1L), wiki.compiler.contributions());
	}

	@Test
	@Ignore("Pending lifecycle change: the current queue records an intermediate newly created version as a compiler removal")
	public void replacingANewArticleInTheSameFrameHasNoPreviouslyCompiledVersionToRemove() throws Exception {
		Article replacement;
		wiki.manager.open();
		try {
			wiki.manager.registerArticle("New", "intermediate\n");
			replacement = wiki.manager.registerArticle("New", "final\n");
		}
		finally {
			wiki.manager.commit();
		}
		wiki.awaitCompilation();
		assertEquals(List.of(replacement.getRootSection()), wiki.compiler.lastCompilation().added());
		assertTrue("An uncompiled intermediate version is not an original removal", wiki.compiler.lastCompilation().removed().isEmpty());
	}

	@Test
	public void queuedReplacementIsVisibleToOtherThreadsBeforeCommit() throws Exception {
		wiki.register("Page", "old\n");
		ExecutorService reader = Executors.newSingleThreadExecutor();
		wiki.manager.open();
		try {
			wiki.manager.queueArticle("Page", "new\n");
			Article replacement = wiki.manager.getArticle("Page");
			String id = line(replacement, 0).getID();
			assertSame(replacement, reader.submit(() -> wiki.manager.getArticle("Page")).get(5, TimeUnit.SECONDS));
			assertSame(line(replacement, 0), reader.submit(() -> Sections.get(id)).get(5, TimeUnit.SECONDS));
		}
		finally {
			wiki.manager.commit();
			reader.shutdownNow();
			assertTrue(reader.awaitTermination(5, TimeUnit.SECONDS));
		}
	}

	@Test
	public void deletingArticleRemovesItsIdsMessagesAndContributions() throws Exception {
		Article original = wiki.register("Page", "definition: pump\n");
		String id = line(original, 0).getID();
		Messages.storeMessage(line(original, 0), getClass(), Messages.error("Old diagnostic"));
		wiki.manager.deleteArticle("Page");
		wiki.awaitCompilation();

		assertNull(wiki.manager.getArticle("Page"));
		assertNull(Sections.get(id));
		assertFalse(Messages.getSectionsWithMessages().contains(line(original, 0)));
		assertEquals(List.of(original.getRootSection()), wiki.compiler.lastCompilation().removed());
		assertTrue(wiki.compiler.contributions().isEmpty());
	}

	@Test
	public void deleteThenRecreateInOneFrameKeepsTheReplacement() throws Exception {
		Article original = wiki.register("Page", "old\n");
		Article replacement;
		wiki.manager.open();
		try {
			wiki.manager.deleteArticle("Page");
			replacement = wiki.manager.registerArticle("Page", "restored\n");
			assertTrue(Sections.isLive(replacement.getRootSection()));
		}
		finally {
			wiki.manager.commit();
		}
		wiki.awaitCompilation();
		assertSame(replacement, wiki.manager.getArticle("Page"));
		assertEquals(List.of(original.getRootSection()), wiki.compiler.lastCompilation().removed());
		assertEquals(List.of(replacement.getRootSection()), wiki.compiler.lastCompilation().added());
		assertEquals(Map.of("restored", 1L), wiki.compiler.contributions());
	}

	@Test
	public void createThenDeleteInOneFrameLeavesNoActiveArticle() throws Exception {
		String id;
		wiki.manager.open();
		try {
			Article created = wiki.manager.registerArticle("New", "transient\n");
			id = line(created, 0).getID();
			wiki.manager.deleteArticle("New");
		}
		finally {
			wiki.manager.commit();
		}
		wiki.awaitCompilation();
		assertNull(wiki.manager.getArticle("New"));
		assertNull(Sections.get(id));
		assertTrue(wiki.compiler.contributions().isEmpty());
	}

	/** Assertions are made after event dispatch so event exception handling cannot hide a failed observation. */
	@Test
	public void registrationListenerSeesPublishedArticleAndResolvableIdsBeforeCompilation() throws Exception {
		wiki.register("Page", "old\n");
		int runsBefore = wiki.compiler.compilationCount();
		List<RegistrationObservation> observations = new ArrayList<>();
		wiki.on(ArticleRegisteredEvent.class, event -> {
			Article article = event.getArticle();
			if (article.getArticleManager() != wiki.manager) return;
			String id = line(article, 0).getID();
			observations.add(new RegistrationObservation(article, wiki.manager.getArticle(article.getTitle()),
					Sections.get(id), wiki.compiler.compilationCount()));
		});

		Article replacement = wiki.register("Page", "new\n");
		assertEquals(1, observations.size());
		RegistrationObservation observation = observations.get(0);
		assertSame(replacement, observation.eventArticle());
		assertSame(replacement, observation.liveArticle());
		assertSame(line(replacement, 0), observation.resolvedSection());
		assertEquals(runsBefore, observation.compilationCount());
	}

	@Test
	public void reentrantRegistrationJoinsTheSameCompilation() throws Exception {
		AtomicReference<Article> generated = new AtomicReference<>();
		wiki.on(ArticleRegisteredEvent.class, event -> {
			if (event.getArticle().getTitle().equals("Source")) {
				generated.set(wiki.manager.registerArticle("Generated", "derived\n"));
			}
		});
		Article source = wiki.register("Source", "source\n");

		assertNotNull("The listener must actually run", generated.get());
		assertSame(generated.get(), wiki.manager.getArticle("Generated"));
		assertEquals(1, wiki.compiler.compilationCount());
		assertEquals(2, wiki.compiler.lastCompilation().added().size());
		assertTrue(wiki.compiler.lastCompilation().added().containsAll(List.of(source.getRootSection(), generated.get().getRootSection())));
		assertTrue(wiki.compiler.lastCompilation().removed().isEmpty());
	}

	/** Exercises AttachmentManager itself, including FullParse's deduplication of already queued attachments. */
	@Test
	public void fullParseRecreatesAttachmentOnceAndRemovingItsReferenceCleansItUp() throws Exception {
		String path = "Parent/data.txt";
		wiki.attachments.put(path, "definition: attachment-old\n");
		Article parent = wiki.register("Parent", "attachment: " + path + "\n");
		Article oldAttachment = wiki.manager.getArticle(path);
		assertNotNull("AttachmentManager must register the referenced content", oldAttachment);
		String oldId = line(oldAttachment, 0).getID();
		assertEquals(1, wiki.compiler.compilationCount());
		assertEquals(2, wiki.compiler.lastCompilation().added().size());
		List<RegistrationObservation> fullParseObservations = new ArrayList<>();
		wiki.on(FullParseEvent.class, event -> {
			for (Article article : event.getArticles()) {
				String id = line(article, 0).getID();
				fullParseObservations.add(new RegistrationObservation(article, wiki.manager.getArticle(article.getTitle()),
						Sections.get(id), wiki.compiler.compilationCount()));
			}
		});

		wiki.attachments.put(path, "definition: attachment-new\n");
		Article refreshedParent;
		Article refreshedAttachment;
		wiki.manager.open();
		try {
			wiki.manager.recreateAndQueueArticles(List.of(parent));
			refreshedParent = wiki.manager.getArticle("Parent");
			EventManager.getInstance().fireEvent(new FullParseEvent(refreshedParent, "test"));
			refreshedAttachment = wiki.manager.getArticle(path);
			assertNotSame(oldAttachment, refreshedAttachment);
			EventManager.getInstance().fireEvent(new FullParseEvent(refreshedParent, "test"));
			assertSame("A second full parse must reuse the attachment already queued in this frame",
					refreshedAttachment, wiki.manager.getArticle(path));
		}
		finally {
			wiki.manager.commit();
		}
		wiki.awaitCompilation();
		assertEquals(2, wiki.compiler.compilationCount());
		assertEquals(2, fullParseObservations.size());
		for (RegistrationObservation observation : fullParseObservations) {
			assertSame(refreshedParent, observation.eventArticle());
			assertSame(refreshedParent, observation.liveArticle());
			assertSame(line(refreshedParent, 0), observation.resolvedSection());
			assertEquals("FullParse listeners must run before this frame is compiled", 1, observation.compilationCount());
		}
		assertEquals(2, wiki.compiler.lastCompilation().added().size());
		assertTrue(wiki.compiler.lastCompilation().removed().containsAll(List.of(parent.getRootSection(), oldAttachment.getRootSection())));
		assertEquals(2, wiki.compiler.lastCompilation().removed().size());
		assertEquals("definition: attachment-new\n", refreshedAttachment.getText());
		assertNull(Sections.get(oldId));
		assertEquals(1, wiki.manager.getAttachmentManager().getCompilingAttachmentSections(refreshedAttachment).size());
		assertSame(line(refreshedParent, 0), wiki.manager.getAttachmentManager()
				.getCompilingAttachmentSections(refreshedAttachment).iterator().next());

		String refreshedId = line(refreshedAttachment, 0).getID();
		wiki.register("Parent", "no attachment\n");
		assertNull(wiki.manager.getArticle(path));
		assertNull(Sections.get(refreshedId));
		assertFalse(wiki.manager.getAttachmentManager().isAttachmentArticle(path));
		assertTrue(wiki.manager.getAttachmentManager().getCompilingAttachmentSections(refreshedAttachment).isEmpty());
		assertFalse(wiki.compiler.contributions().containsKey("definition: attachment-new"));
	}

	/** A shared attachment stays compiled until the final parent reference disappears. */
	@Test
	public void sharedCompiledAttachmentSurvivesUntilItsLastParentReferenceIsRemoved() throws Exception {
		String path = "Shared/data.txt";
		wiki.attachments.put(path, "definition: shared-attachment\n");
		Article firstParent = wiki.register("First", "attachment: " + path + "\n");
		Article attachment = wiki.manager.getArticle(path);
		assertNotNull("The first reference must register the attachment", attachment);
		String attachmentId = line(attachment, 0).getID();
		Article secondParent = wiki.register("Second", "attachment: " + path + "\n");
		assertSame("The second reference must reuse the compiled attachment", attachment, wiki.manager.getArticle(path));
		assertEquals(2, wiki.manager.getAttachmentManager().getCompilingAttachmentSections(attachment).size());
		assertTrue(wiki.manager.getAttachmentManager().getCompilingAttachmentSections(attachment)
				.containsAll(List.of(line(firstParent, 0), line(secondParent, 0))));

		int runsBeforeRemovingFirstReference = wiki.compiler.compilationCount();
		wiki.register("First", "no attachment\n");
		assertSame("Removing one reference must retain the shared attachment", attachment, wiki.manager.getArticle(path));
		assertSame(line(attachment, 0), Sections.get(attachmentId));
		assertEquals(1, wiki.manager.getAttachmentManager().getCompilingAttachmentSections(attachment).size());
		assertSame(line(secondParent, 0), wiki.manager.getAttachmentManager()
				.getCompilingAttachmentSections(attachment).iterator().next());
		assertEquals(Long.valueOf(1), wiki.compiler.contributions().get("definition: shared-attachment"));
		assertEquals(runsBeforeRemovingFirstReference + 1, wiki.compiler.compilationCount());
		assertFalse("Retaining one reference must not recompile the attachment", wiki.compiler.lastCompilation().added().contains(attachment.getRootSection()));
		assertFalse("Retaining one reference must not remove the attachment", wiki.compiler.lastCompilation().removed().contains(attachment.getRootSection()));

		wiki.register("Second", "no attachment\n");
		assertNull("Removing the last reference must remove the attachment article", wiki.manager.getArticle(path));
		assertNull(Sections.get(attachmentId));
		assertFalse(wiki.manager.getAttachmentManager().isAttachmentArticle(path));
		assertTrue(wiki.manager.getAttachmentManager().getCompilingAttachmentSections(attachment).isEmpty());
		assertFalse(wiki.compiler.contributions().containsKey("definition: shared-attachment"));
	}

	/**
	 * Public miniature of the clear/rebuild/restore sequence: an identity-based contribution ledger detects stale
	 * definitions and duplicate relations. This uses a synthetic compiler, not confidential CBX data or d3web semantics.
	 */
	@Test
	public void clearAndRestoreContributionsWhileRecreatingCompilerArticles() throws Exception {
		Article knowledgeBase = wiki.register("Knowledge Base", "compiler: knowledge-base\n");
		Article ontology = wiki.register("System Structure", "compiler: ontology\n");
		String content = "definition: pump\nrelation: failure->pump\n";
		wiki.register("Definition", content);
		Map<String, Long> complete = Map.of("compiler: knowledge-base", 1L, "compiler: ontology", 1L,
				"definition: pump", 1L, "relation: failure->pump", 1L);
		Map<String, Long> cleared = Map.of("compiler: knowledge-base", 1L, "compiler: ontology", 1L);
		assertEquals(complete, wiki.compiler.contributions());

		for (int cycle = 0; cycle < 2; cycle++) {
			wiki.manager.open();
			try {
				wiki.manager.queueArticle("Definition", "");
				wiki.manager.recreateAndQueueArticles(List.of(knowledgeBase, ontology));
				assertEquals("", wiki.manager.getArticle("Definition").getText());
			}
			finally {
				wiki.manager.commit();
			}
			wiki.awaitCompilation();
			assertEquals(3, wiki.compiler.lastCompilation().added().size());
			assertEquals(3, wiki.compiler.lastCompilation().removed().size());
			assertEquals("Clearing must remove every contribution of the old version", cleared, wiki.compiler.contributions());
			wiki.register("Definition", content);
			assertEquals("Restoration must add each definition/relation exactly once", complete, wiki.compiler.contributions());
			knowledgeBase = wiki.manager.getArticle("Knowledge Base");
			ontology = wiki.manager.getArticle("System Structure");
		}
	}

	private record RegistrationObservation(Article eventArticle, Article liveArticle, Section<?> resolvedSection,
										   int compilationCount) { }
}
