/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Rule;
import org.junit.Test;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.event.KDOMCreatedEvent;

import static de.knowwe.core.ArticleLifecycleFixture.line;
import static org.junit.Assert.*;

/**
 * Queue-time publication keeps drafts isolated. IDs survive unchanged-content recompiles, but every content change
 * invalidates the complete article's IDs. Equal ID addresses across recompiles do not imply equal Section instances.
 */
public class ArticleLifecycleTest {

	@Rule
	public final ArticleLifecycleFixture wiki = new ArticleLifecycleFixture();

	/** Creating and discarding a draft must not redirect the ID of the still-published article. */
	@Test
	public void constructingUnregisteredReplacementPreservesLiveSectionIds() throws Exception {
		Article original = wiki.register("Page", "unchanged\n");
		Section<?> originalLine = line(original, 0);
		String id = originalLine.getID();

		Article draft = wiki.draft("Page", original.getText());
		assertNotSame(originalLine, line(draft, 0));
		assertSame(original, wiki.manager.getArticle("Page"));
		assertSame("Construction must not publish the draft's sections", originalLine, Sections.get(id));
		assertTrue(Sections.isLive(originalLine));
		assertFalse(Sections.isLive(line(draft, 0)));
	}

	@Test
	public void constructingUnregisteredReplacementPreservesLiveMessageTracking() throws Exception {
		Article original = wiki.register("Page", "unchanged\n");
		Section<?> section = line(original, 0);
		Message message = Messages.error("Existing diagnostic");
		Messages.storeMessage(section, getClass(), message);

		wiki.draft("Page", "changed\n");
		assertEquals(List.of(message), List.copyOf(Messages.getMessages(section)));
		assertTrue("The live diagnostic must remain in the global overview",
				Messages.getSectionsWithMessages(Message.Type.ERROR).contains(section));
	}

	@Test
	public void draftMessagesAreLocallyReadableButNotGloballyPublished() {
		Article draft = wiki.draft("Unregistered", "draft\n");
		Section<?> section = line(draft, 0);
		Message message = Messages.error("Draft diagnostic");
		Messages.storeMessage(section, getClass(), message);

		assertEquals(List.of(message), List.copyOf(Messages.getMessages(section)));
		assertTrue("Local message queries must work on drafts", Messages.hasMessages(section, Message.Type.ERROR));
		assertFalse("Unregistered drafts must not appear in the global overview",
				Messages.getSectionsWithMessages(Message.Type.ERROR).contains(section));
	}

	@Test
	public void draftDiagnosticBecomesGloballyVisibleWhenItsArticleIsActuallyQueued() throws Exception {
		AtomicReference<Section<?>> constructedSection = new AtomicReference<>();
		Message message = Messages.error("Diagnostic written while still a draft");
		wiki.on(KDOMCreatedEvent.class, event -> {
			if (!event.getArticle().getTitle().equals("Page")) return;
			constructedSection.set(line(event.getArticle(), 0));
			Messages.storeMessage(constructedSection.get(), getClass(), message);
			assertFalse("The construction event runs before queue-time publication",
					Messages.getSectionsWithMessages(Message.Type.ERROR).contains(constructedSection.get()));
		});

		Article published = wiki.register("Page", "draft\n");
		assertSame(line(published, 0), constructedSection.get());
		assertEquals(List.of(message), List.copyOf(Messages.getMessages(constructedSection.get())));
		assertTrue("Queue-time publication must expose diagnostics already attached to the draft",
				Messages.getSectionsWithMessages(Message.Type.ERROR).contains(constructedSection.get()));
	}

	@Test
	public void lateMessageWriteToRetiredVersionStaysLocal() throws Exception {
		Article original = wiki.register("Page", "old\n");
		Section<?> retiredSection = line(original, 0);
		Article replacement = wiki.register("Page", "new\n");

		Message lateMessage = Messages.error("Late retired diagnostic");
		Messages.storeMessage(retiredSection, getClass(), lateMessage);
		assertEquals(List.of(lateMessage), List.copyOf(Messages.getMessages(retiredSection)));
		assertFalse("A retired version must not re-enter the global diagnostic overview",
				Messages.getSectionsWithMessages(Message.Type.ERROR).contains(retiredSection));
		assertFalse(Messages.getSectionsWithMessages(Message.Type.ERROR).contains(line(replacement, 0)));
	}

	@Test
	public void requestedDraftIdDoesNotMakeANewArticleGloballyReachable() {
		Article draft = wiki.draft("Unregistered", "draft\n");
		String id = line(draft, 0).getID();

		assertEquals(id, line(draft, 0).getID());
		assertNull(wiki.manager.getArticle("Unregistered"));
		assertNull("An ID request must not publish an unregistered section", Sections.get(id));
	}

	/** Uses the construction event to request the ID before the manager can queue that same instance. */
	@Test
	public void unchangedReplacementKeepsIdRequestedBeforeQueueing() throws Exception {
		Article original = wiki.register("Page", "unchanged\n");
		String originalId = line(original, 0).getID();
		AtomicReference<Section<?>> earlySection = new AtomicReference<>();
		AtomicReference<String> earlyId = new AtomicReference<>();
		wiki.on(KDOMCreatedEvent.class, event -> {
			if (!event.getArticle().getTitle().equals("Page")) return;
			earlySection.set(line(event.getArticle(), 0));
			earlyId.set(earlySection.get().getID());
		});

		Article replacement = wiki.register("Page", original.getText());
		assertSame(line(replacement, 0), earlySection.get());
		assertEquals(originalId, earlyId.get());
		assertEquals("Publishing must not change an already returned ID", earlyId.get(), line(replacement, 0).getID());
		assertSame(line(replacement, 0), Sections.get(originalId));
	}

	@Test
	public void partialContentChangeInvalidatesAllPreviousSectionIds() throws Exception {
		Article original = wiki.register("Page", "keep\nold\nremoved\n");
		String keepId = line(original, 0).getID();
		String changedId = line(original, 1).getID();
		String removedId = line(original, 2).getID();
		wiki.manager.open();
		try {
			wiki.manager.queueArticle("Page", "keep\nnew\n");
			Article replacement = wiki.manager.getArticle("Page");
			assertNotSame(original, replacement);
			assertNotEquals("Even an unchanged line gets a new ID when the article text changes", keepId, line(replacement, 0).getID());
			assertNull(Sections.get(keepId));
			assertSame("New sections must be reachable before commit", line(replacement, 0), Sections.get(line(replacement, 0).getID()));
			assertNull(Sections.get(changedId));
			assertNull(Sections.get(removedId));
			assertSame(line(replacement, 1), Sections.get(line(replacement, 1).getID()));
		}
		finally {
			wiki.manager.commit();
		}
	}

	/** Titles that collided under the old 32-bit hash scheme belong to independent namespaces. */
	@Test
	public void formerlyCollidingTitlesRemainDistinctAcrossReplacementAndDeletion() throws Exception {
		assertEquals("Aa".hashCode(), "BB".hashCode());
		Article first = wiki.register("Aa", "same\n");
		Article second = wiki.register("BB", "same\n");
		String firstId = first.getRootSection().getID();
		String secondId = second.getRootSection().getID();
		assertNotEquals(firstId, secondId);
		assertSame(first.getRootSection(), Sections.get(firstId));
		assertSame(second.getRootSection(), Sections.get(secondId));

		Article replacement = wiki.register("BB", second.getText());
		assertEquals(secondId, replacement.getRootSection().getID());
		assertSame(first.getRootSection(), Sections.get(firstId));
		assertSame(replacement.getRootSection(), Sections.get(secondId));
		wiki.manager.deleteArticle("BB");
		wiki.awaitCompilation();
		assertNull(Sections.get(secondId));
		assertSame("Removing the colliding section must not remove its neighbour", first.getRootSection(), Sections.get(firstId));
	}

	@Test
	public void collidingDraftDoesNotDisturbAnUnrelatedPublishedSection() throws Exception {
		Article first = wiki.register("Aa", "same\n");
		String firstId = first.getRootSection().getID();
		Article draft = wiki.draft("BB", first.getText());
		String draftId = draft.getRootSection().getID();
		assertNotEquals(firstId, draftId);
		assertSame(first.getRootSection(), Sections.get(firstId));
		draft.destroy(null);
		assertNull(Sections.get(draftId));
		assertSame(first.getRootSection(), Sections.get(firstId));
	}

	@Test
	public void discardingAnotherDraftCannotChangeAnIdBeforePublication() throws Exception {
		assertEquals("Aa".hashCode(), "BB".hashCode());
		Article discarded = wiki.draft("BB", "same\n");
		String discardedId = discarded.getRootSection().getID();
		CountDownLatch constructed = new CountDownLatch(1);
		CountDownLatch allowQueueing = new CountDownLatch(1);
		AtomicReference<Section<?>> earlyPublishedSection = new AtomicReference<>();
		AtomicReference<String> earlyPublishedId = new AtomicReference<>();
		wiki.on(KDOMCreatedEvent.class, event -> {
			if (!event.getArticle().getTitle().equals("Aa")) return;
			earlyPublishedSection.set(event.getArticle().getRootSection());
			earlyPublishedId.set(earlyPublishedSection.get().getID());
			constructed.countDown();
			await(allowQueueing);
		});
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Article> publishedFuture = executor.submit(() -> wiki.manager.registerArticle("Aa", "same\n"));
		try {
			assertTrue("Published article did not reach the construction boundary", constructed.await(5, TimeUnit.SECONDS));
			assertNotEquals("Different articles need independent ID namespaces", discardedId, earlyPublishedId.get());
			assertNull("The paused article must remain a draft", Sections.get(earlyPublishedId.get()));
			discarded.destroy(null);
			allowQueueing.countDown();
			Article published = publishedFuture.get(5, TimeUnit.SECONDS);
			assertSame(earlyPublishedSection.get(), published.getRootSection());
			assertEquals(earlyPublishedId.get(), published.getRootSection().getID());
			assertSame("Discarding a colliding draft must not remove the live owner", published.getRootSection(),
					Sections.get(earlyPublishedId.get()));
		}
		finally {
			allowQueueing.countDown();
			executor.shutdownNow();
			assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
		}
	}

	@Test
	public void failedParseLeavesNoResolvableIdAndDoesNotAffectAnotherDraft() {
		assertEquals("Aa".hashCode(), "BB".hashCode());
		AtomicReference<String> failedId = new AtomicReference<>();
		wiki.afterParse = parsed -> {
			failedId.set(parsed.getChildren().get(0).getID());
			throw new IllegalStateException("Intentional failed draft");
		};

		assertNull(Article.createArticle("same\n", "Aa", wiki.manager));
		assertNotNull("The failing parser must have requested the partial line ID", failedId.get());
		assertNull(Sections.get(failedId.get()));
		wiki.afterParse = parsed -> { };
		Article replacement = wiki.draft("BB", "same\n");
		assertNotEquals(failedId.get(), line(replacement, 0).getID());
		assertNull("The second draft must also remain private", Sections.get(line(replacement, 0).getID()));
	}

	@Test
	public void equalEmptySiblingSectionsKeepDistinctStableIdsAcrossReplacement() throws Exception {
		wiki.afterParse = parsed -> {
			Section<?> exemplar = parsed.getChildren().get(0);
			Section.createSection("", exemplar.get(), parsed);
			Section.createSection("", exemplar.get(), parsed);
		};
		Article original = wiki.register("Page", "line\n");
		Section<?> first = original.getRootSection().getChildren().get(1);
		Section<?> second = original.getRootSection().getChildren().get(2);
		String firstId = first.getID();
		String secondId = second.getID();
		assertNotEquals("Identical empty siblings need different IDs in one article", firstId, secondId);
		assertSame(first, Sections.get(firstId));
		assertSame(second, Sections.get(secondId));

		Article replacement = wiki.register("Page", original.getText());
		Section<?> replacementFirst = replacement.getRootSection().getChildren().get(1);
		Section<?> replacementSecond = replacement.getRootSection().getChildren().get(2);
		assertEquals(secondId, replacementSecond.getID());
		assertEquals(firstId, replacementFirst.getID());
		assertSame(replacementFirst, Sections.get(firstId));
		assertSame(replacementSecond, Sections.get(secondId));
	}

	@Test
	public void lateCleanupOfOldArticleCannotUnregisterReplacementIds() throws Exception {
		Article original = wiki.register("Page", "same\n");
		String id = line(original, 0).getID();
		Article replacement = wiki.register("Page", original.getText());
		assertSame(line(replacement, 0), Sections.get(id));

		original.destroy(null);
		original.destroy(null);
		assertSame("Cleanup must only remove IDs still owned by the old instance", line(replacement, 0), Sections.get(id));
	}

	/** A deleted article's namespace must not be reused for an unrelated article. */
	@Test
	public void lateCleanupOfDeletedArticleCannotAffectAnotherArticle() throws Exception {
		assertEquals("Aa".hashCode(), "BB".hashCode());
		Article deletedOwner = wiki.register("Aa", "same\n");
		String reusedId = deletedOwner.getRootSection().getID();
		wiki.manager.deleteArticle("Aa");
		wiki.awaitCompilation();
		assertNull(Sections.get(reusedId));

		Article newOwner = wiki.register("BB", "same\n");
		String newId = newOwner.getRootSection().getID();
		assertNotEquals(reusedId, newId);
		assertSame(newOwner.getRootSection(), Sections.get(newId));

		deletedOwner.destroy(null);
		assertNull(Sections.get(reusedId));
		assertSame("Late cleanup must not unregister another article", newOwner.getRootSection(), Sections.get(newId));
	}

	@Test
	public void firstIdRequestOnRetiredSectionCannotRegisterItAgain() throws Exception {
		Article original = wiki.register("Page", "same\n");
		Section<?> retired = line(original, 0); // deliberately never requested its ID while live
		Article replacement = wiki.register("Page", original.getText());
		String activeId = line(replacement, 0).getID();
		String retiredId = retired.getID();

		assertSame(line(replacement, 0), Sections.get(activeId));
		assertNotSame("A late request must not make a retired section globally reachable", retired, Sections.get(retiredId));
	}

	@Test
	public void parserFailureLeavesThePreviouslyPublishedArticleUsable() throws Exception {
		Article original = wiki.register("Page", "old\n");
		String id = line(original, 0).getID();
		Messages.storeMessage(line(original, 0), getClass(), Messages.error("Original message"));
		wiki.afterParse = parsed -> { throw new IllegalStateException("Intentional parser failure"); };

		assertNull(Article.createArticle("new\n", "Page", wiki.manager));
		assertSame(original, wiki.manager.getArticle("Page"));
		assertSame(line(original, 0), Sections.get(id));
		assertTrue(Messages.getSectionsWithMessages().contains(line(original, 0)));
	}

	@Test
	public void parserFailureDoesNotLeakIdsOrMessagesFromThePartialTree() {
		AtomicReference<Section<?>> partial = new AtomicReference<>();
		AtomicReference<String> partialId = new AtomicReference<>();
		wiki.afterParse = parsed -> {
			partial.set(parsed.getChildren().get(0));
			partialId.set(partial.get().getID());
			Messages.storeMessage(partial.get(), getClass(), Messages.error("Partial diagnostic"));
			throw new IllegalStateException("Intentional parser failure after creating a section");
		};

		assertNull(Article.createArticle("partial\n", "Failed", wiki.manager));
		assertNotNull("The parser must reach the fault injection", partial.get());
		assertNull(Sections.get(partialId.get()));
		assertFalse(Messages.getSectionsWithMessages().contains(partial.get()));
	}

	/** Pauses at the real boundary between construction and queueing, without relying on scheduling sleeps. */
	@Test
	public void readersKeepSeeingTheOldRegistryUntilReplacementIsQueued() throws Exception {
		Article original = wiki.register("Page", "same\n");
		String id = line(original, 0).getID();
		CountDownLatch constructed = new CountDownLatch(1);
		CountDownLatch allowQueueing = new CountDownLatch(1);
		wiki.on(KDOMCreatedEvent.class, event -> {
			if (!event.getArticle().getTitle().equals("Page")) return;
			constructed.countDown();
			await(allowQueueing);
		});
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Article> replacement = executor.submit(() -> wiki.manager.registerArticle("Page", original.getText()));
		try {
			assertTrue("Replacement did not finish construction", constructed.await(5, TimeUnit.SECONDS));
			assertSame(original, wiki.manager.getArticle("Page"));
			assertSame("A concurrent reader must not observe an unpublished replacement", line(original, 0), Sections.get(id));
		}
		finally {
			allowQueueing.countDown();
			try {
				Article published = replacement.get(5, TimeUnit.SECONDS);
				assertSame(line(published, 0), Sections.get(id));
			}
			finally {
				executor.shutdownNow();
				assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
			}
		}
	}

	/** Coordinates at the construction event, which is after parsing but before either draft can be queued. */
	@Test
	public void discardingOverlappingUnqueuedReplacementDraftsPreservesTheLiveRegistry() throws Exception {
		Article original = wiki.register("Page", "same\n");
		Section<?> originalLine = line(original, 0);
		String id = originalLine.getID();
		CountDownLatch draftsConstructed = new CountDownLatch(2);
		CountDownLatch releaseDrafts = new CountDownLatch(1);
		List<DraftId> earlyDraftIds = Collections.synchronizedList(new ArrayList<>());
		wiki.on(KDOMCreatedEvent.class, event -> {
			if (!event.getArticle().getTitle().equals("Page")) return;
			Section<?> draftLine = line(event.getArticle(), 0);
			earlyDraftIds.add(new DraftId(draftLine, draftLine.getID()));
			draftsConstructed.countDown();
			await(releaseDrafts);
		});
		ExecutorService executor = Executors.newFixedThreadPool(2);
		Future<Article> first = executor.submit(() -> wiki.draft("Page", original.getText()));
		Future<Article> second = executor.submit(() -> wiki.draft("Page", original.getText()));
		try {
			assertTrue("Both drafts did not reach the construction boundary", draftsConstructed.await(5, TimeUnit.SECONDS));
			releaseDrafts.countDown();
			Article firstDraft = first.get(5, TimeUnit.SECONDS);
			Article secondDraft = second.get(5, TimeUnit.SECONDS);
			assertEquals("Both drafts must request an ID at the construction boundary", 2, earlyDraftIds.size());
			List<String> brokenStages = new ArrayList<>();
			if (wiki.manager.getArticle("Page") != original) brokenStages.add("construction replaced the live article");
			if (Sections.get(id) != originalLine) brokenStages.add("completed drafts changed the live ID registry");
			for (int i = 0; i < earlyDraftIds.size(); i++) {
				DraftId draftId = earlyDraftIds.get(i);
				if (!draftId.id().equals(draftId.section().getID())) {
					brokenStages.add("draft " + (i + 1) + " changed its early ID");
				}
				if (Sections.get(draftId.id()) == draftId.section()) {
					brokenStages.add("completed draft " + (i + 1) + " is globally reachable by its early ID");
				}
			}
			firstDraft.destroy(null);
			if (Sections.get(id) != originalLine) brokenStages.add("discarding the first draft changed the live ID registry");
			secondDraft.destroy(null);
			if (Sections.get(id) != originalLine) brokenStages.add("discarding the second draft changed the live ID registry");
			assertTrue("Unqueued replacement drafts must preserve every live-owner stage: " + brokenStages, brokenStages.isEmpty());
		}
		finally {
			releaseDrafts.countDown();
			executor.shutdownNow();
			assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
		}
	}

	/** Restoring text through a new edit is not a rollback of the original Article instance. */
	@Test
	public void editingBackToOriginalTextDoesNotReviveOldIds() throws Exception {
		Article original = wiki.register("Page", "original\n");
		String originalId = line(original, 0).getID();
		Article changed = wiki.register("Page", "changed\n");
		String changedId = line(changed, 0).getID();
		Article restoredText = wiki.register("Page", original.getText());
		String restoredId = line(restoredText, 0).getID();
		assertNotEquals(originalId, restoredId);
		assertNotEquals(changedId, restoredId);
		assertNull(Sections.get(originalId));
		assertNull(Sections.get(changedId));
		assertSame(line(restoredText, 0), Sections.get(restoredId));
	}

	@Test
	public void deletingAndRecreatingSameTitleAndTextStartsNewNamespace() throws Exception {
		Article original = wiki.register("Page", "same\n");
		String oldId = original.getRootSection().getID();
		wiki.manager.deleteArticle("Page");
		wiki.awaitCompilation();
		Article recreated = wiki.register("Page", original.getText());
		assertNotEquals(oldId, recreated.getRootSection().getID());
		assertNull(Sections.get(oldId));
	}

	@Test
	public void parserRequestedIdsSurviveNormalizationAndUnchangedRecompile() throws Exception {
		List<String> earlyIds = new ArrayList<>();
		wiki.afterParse = parsed -> earlyIds.add(parsed.getChildren().get(0).getID());
		Article original = wiki.register("Page", "same\r\n");
		Article replacement = wiki.register("Page", "same\n");
		assertEquals(2, earlyIds.size());
		assertEquals(earlyIds.get(0), earlyIds.get(1));
		assertEquals(earlyIds.get(0), line(original, 0).getID());
		assertSame(line(replacement, 0), Sections.get(earlyIds.get(0)));
	}

	/** A parser/plugin change must not make an old ID address a different section type, even with identical text. */
	@Test
	public void changedSectionTypeDoesNotInheritIdDuringUnchangedRecompile() throws Exception {
		AtomicBoolean differentType = new AtomicBoolean();
		wiki.afterParse = parsed -> Section.createSection("", differentType.get()
				? wiki.alternativeLine : parsed.getChildren().get(0).get(), parsed);
		Article original = wiki.register("Page", "same\n");
		String oldId = line(original, 1).getID();
		differentType.set(true);
		Article replacement = wiki.register("Page", original.getText());
		assertEquals(original.getSectionIdNamespace(), replacement.getSectionIdNamespace());
		assertNotEquals(oldId, line(replacement, 1).getID());
		assertNull(Sections.get(oldId));
	}

	/** Equal source, type and position do not imply the same text range after a parser change. */
	@Test
	public void changedSectionLengthDoesNotInheritIdDuringUnchangedRecompile() throws Exception {
		Article original = wiki.register("Page", "same\n");
		String oldId = line(original, 0).getID();
		wiki.afterParse = parsed -> parsed.getChildren().get(0).setText("sam");
		Article replacement = wiki.register("Page", original.getText());
		assertEquals(original.getSectionIdNamespace(), replacement.getSectionIdNamespace());
		assertEquals(line(original, 0).getOffsetInArticle(), line(replacement, 0).getOffsetInArticle());
		assertNotEquals(line(original, 0).getTextLength(), line(replacement, 0).getTextLength());
		assertNotEquals(oldId, line(replacement, 0).getID());
		assertNull(Sections.get(oldId));
		assertSame(line(replacement, 0), Sections.get(line(replacement, 0).getID()));
	}

	/** A reentrant edit models a predecessor changing between parsing and queueing without timing sleeps. */
	@Test
	public void staleRecompileCannotReviveNamespaceAfterAnInterveningEdit() throws Exception {
		Article original = wiki.register("Page", "original\n");
		String originalId = line(original, 0).getID();
		AtomicBoolean injectEdit = new AtomicBoolean(true);
		wiki.on(KDOMCreatedEvent.class, event -> {
			if (event.getArticle().getTitle().equals("Page") && injectEdit.getAndSet(false)) {
				wiki.manager.registerArticle("Page", "changed\n");
			}
		});
		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> wiki.manager.registerArticle("Page", original.getText()));
		assertTrue(failure.getMessage().contains("retry"));
		wiki.awaitCompilation();
		assertEquals("changed\n", wiki.manager.getArticle("Page").getText());
		assertNull(Sections.get(originalId));
	}

	private record DraftId(Section<?> section, String id) { }

	private static void await(CountDownLatch latch) {
		try {
			assertTrue("Timed out waiting to continue article registration", latch.await(5, TimeUnit.SECONDS));
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError(e);
		}
	}
}
