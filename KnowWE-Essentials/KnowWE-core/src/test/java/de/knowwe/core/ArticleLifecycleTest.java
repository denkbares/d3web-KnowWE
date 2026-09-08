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
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Ignore;
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
 * Contract tests for separating article construction from publication at queue time. They intentionally assert the
 * desired lifecycle, rather than expecting today's defects: draft isolation and identity-safe cleanup tests are ignored
 * until that refactoring is implemented. Successful replacement/collision tests protect already working behavior.
 */
public class ArticleLifecycleTest {

	@Rule
	public final ArticleLifecycleFixture wiki = new ArticleLifecycleFixture();

	/** Creating and discarding a draft must not redirect the ID of the still-published article. */
	@Ignore("Pending lifecycle change: Article construction currently destroys the previous version and registers replacement IDs")
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
	@Ignore("Pending lifecycle change: constructing a replacement currently unregisters messages of the live version")
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
	@Ignore("Pending lifecycle change: draft message tracking is currently global immediately when a message is stored")
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
	@Ignore("Pending lifecycle change: getID() currently registers sections even when their article is not registered")
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
	public void partialReplacementKeepsUnchangedSectionsAndRemovesObsoleteIds() throws Exception {
		Article original = wiki.register("Page", "keep\nold\nremoved\n");
		String keepId = line(original, 0).getID();
		String changedId = line(original, 1).getID();
		String removedId = line(original, 2).getID();
		wiki.manager.open();
		try {
			wiki.manager.queueArticle("Page", "keep\nnew\n");
			Article replacement = wiki.manager.getArticle("Page");
			assertNotSame(original, replacement);
			assertSame("New sections must be reachable before commit", line(replacement, 0), Sections.get(keepId));
			assertNull(Sections.get(changedId));
			assertNull(Sections.get(removedId));
			assertSame(line(replacement, 1), Sections.get(line(replacement, 1).getID()));
		}
		finally {
			wiki.manager.commit();
		}
	}

	/** Aa and BB are distinct, equal-length Java-hash collisions, also inside the complete ID signature. */
	@Test
	public void realHashCollisionRemainsDistinctAcrossReplacementAndDeletion() throws Exception {
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
	@Ignore("Pending lifecycle change: Article.destroy() currently removes the replacement's ID when called late")
	public void lateCleanupOfOldArticleCannotUnregisterReplacementIds() throws Exception {
		Article original = wiki.register("Page", "same\n");
		String id = line(original, 0).getID();
		Article replacement = wiki.register("Page", original.getText());
		assertSame(line(replacement, 0), Sections.get(id));

		original.destroy(null);
		original.destroy(null);
		assertSame("Cleanup must only remove IDs still owned by the old instance", line(replacement, 0), Sections.get(id));
	}

	/** Aa and BB collide in the complete root-section signature, so this verifies reuse after the original owner is gone. */
	@Test
	@Ignore("Pending lifecycle change: Section.unregisterOrUpdateSectionID() blindly removes a reused colliding ID during late cleanup")
	public void lateCleanupOfDeletedCollidingOwnerCannotUnregisterReusedRootId() throws Exception {
		assertEquals("Aa".hashCode(), "BB".hashCode());
		Article deletedOwner = wiki.register("Aa", "same\n");
		String reusedId = deletedOwner.getRootSection().getID();
		wiki.manager.deleteArticle("Aa");
		wiki.awaitCompilation();
		assertNull(Sections.get(reusedId));

		Article newOwner = wiki.register("BB", "same\n");
		assertEquals("The released colliding ID must be reusable", reusedId, newOwner.getRootSection().getID());
		assertSame(newOwner.getRootSection(), Sections.get(reusedId));

		deletedOwner.destroy(null);
		assertSame("Late cleanup must not unregister an ID now owned by another article", newOwner.getRootSection(), Sections.get(reusedId));
	}

	@Test
	@Ignore("Pending lifecycle change: a retired section can currently register itself again on a late ID request")
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
	@Ignore("Pending lifecycle change: a parser failure after partial section creation currently leaks the partial ID")
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
	@Ignore("Pending lifecycle change: construction publishes replacement sections before queueing, so concurrent readers can observe them")
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
	@Ignore("Pending lifecycle change: constructing overlapping replacement drafts unregisters the live ID mapping before either draft is queued")
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
