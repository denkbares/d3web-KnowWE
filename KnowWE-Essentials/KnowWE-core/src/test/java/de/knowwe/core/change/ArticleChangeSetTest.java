/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.change;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.change.ArticleChangeException.Reason;
import de.knowwe.core.change.ArticleChangeSet.Outcome;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.core.wikiConnector.WikiPageInfo;
import de.knowwe.event.ArticleDeletedEvent;
import de.knowwe.event.ArticleRenamedEvent;
import de.knowwe.event.ArticleUpdateEvent;

import static org.junit.Assert.*;

/**
 * Applies change sets against the real article manager and compiler with an in-memory persistence connector.
 */
public class ArticleChangeSetTest {

	private final Map<String, String> disk = new LinkedHashMap<>();
	private final Map<String, Integer> versions = new LinkedHashMap<>();
	private final Set<String> pending = new HashSet<>();
	private final List<String> transactions = new ArrayList<>();
	private final List<Event> notifications = new ArrayList<>();
	private final Set<String> denied = new HashSet<>();
	private Map<String, String> snapshot;
	private final Map<String, String> referrerRewrites = new LinkedHashMap<>();
	private String failingWrite;
	private Runnable onRollback = () -> { };
	private Environment environment;
	private ArticleManager manager;

	private final EventListener listener = new EventListener() {
		@Override
		public Collection<Class<? extends Event>> getEvents() {
			return List.of(ArticleUpdateEvent.class, ArticleDeletedEvent.class, ArticleRenamedEvent.class);
		}

		@Override
		public void notify(Event event) {
			assertEquals("Notifications must follow the persistence commit", "commit", transactions.get(transactions.size() - 1));
			notifications.add(event);
		}
	};

	@Before
	public void setUp() throws Exception {
		WikiConnector connector = (WikiConnector) Proxy.newProxyInstance(WikiConnector.class.getClassLoader(),
				new Class<?>[] { WikiConnector.class }, (proxy, method, args) -> switch (method.getName()) {
					case "setEnvironment" -> null;
					case "getEnvironment" -> environment;
					case "getApplicationName" -> "article-change-test";
					case "getApplicationRootPath" -> "target/article-change-test";
					case "getWikiProperty" -> null;
					case "getVersion" -> versions.getOrDefault((String) args[0], 0);
					case "getArticleHistory" -> List.of(new WikiPageInfo((WikiConnector) proxy, (String) args[0],
							"author", versions.getOrDefault((String) args[0], 0), new Date(), "note"));
					case "userCanEditArticle", "userCanDeleteArticle" -> !denied.contains((String) args[0]);
					case "userCanCreateArticles" -> !denied.contains("*create*");
					case "isArticleLocked" -> false;
					case "openPageTransaction" -> {
						transactions.add("open");
						snapshot = new LinkedHashMap<>(disk);
						yield null;
					}
					case "rollbackPageTransaction" -> {
						transactions.add("rollback");
						onRollback.run();
						disk.clear();
						disk.putAll(snapshot);
						pending.clear();
						yield null;
					}
					case "commitPageTransaction" -> {
						transactions.add("commit");
						pending.forEach(title -> versions.merge(title, 1, Integer::sum));
						pending.clear();
						yield null;
					}
					case "createArticle" -> {
						disk.put((String) args[0], (String) args[2]);
						pending.add((String) args[0]);
						yield args[0];
					}
					case "writeArticleToWikiPersistence" -> {
						if (args[0].equals(failingWrite)) yield false;
						disk.put((String) args[0], (String) args[1]);
						pending.add((String) args[0]);
						yield true;
					}
					case "getArticleText" -> disk.get((String) args[0]);
					case "renameArticle" -> {
						String from = (String) args[0];
						String to = (String) args[1];
						transactions.add("rename " + from + " -> " + to + " by " + args[2]);
						disk.put(to, disk.remove(from));
						versions.put(to, versions.remove(from));
						pending.add(to);
						disk.putAll(referrerRewrites);
						pending.addAll(referrerRewrites.keySet());
						yield List.copyOf(referrerRewrites.keySet());
					}
					case "deleteArticle" -> {
						disk.remove((String) args[0]);
						versions.remove((String) args[0]);
						yield null;
					}
					default -> throw new UnsupportedOperationException(method.getName());
				});
		environment = Environment.create(connector, new RootType());
		manager = environment.getArticleManager();
		for (String title : List.of("Update", "Delete", "Rename", "Other")) {
			disk.put(title, "original");
			versions.put(title, 1);
			manager.registerArticle(title, "original");
		}
		manager.getCompilerManager().awaitTermination();
		environment.getEventManager().registerListener(listener);
	}

	@After
	public void tearDown() throws Exception {
		if (environment != null) {
			manager.getCompilerManager().awaitTermination();
			environment.close();
		}
	}

	@Test
	public void appliesAllKindsInOneCommitAndOneCompilation() throws Exception {
		int compilation = manager.getCompilerManager().getCompilationId();

		ArticleChangeSet.Result result = ArticleChangeSet.builder()
				.update("update", "updated")
				.create("Created", "created")
				.write("Written", "written")
				.delete("Delete")
				.rename("Rename", "Renamed")
				.build()
				.applyAs(environment, "author", "note");

		manager.getCompilerManager().awaitTermination();
		assertEquals(compilation + 1, manager.getCompilerManager().getCompilationId());
		assertEquals(List.of("open", "rename Rename -> Renamed by author", "commit"), transactions);
		assertEquals("updated", disk.get("Update"));
		assertEquals("original", disk.get("Renamed"));
		assertFalse(disk.containsKey("Delete"));
		assertFalse(disk.containsKey("Rename"));
		assertNull(manager.getArticle("Rename"));
		assertEquals("original", manager.getArticle("Renamed").getText());
		assertNotNull(manager.getArticle("Created"));

		assertEquals(List.of("Created", "Written"), result.created());
		assertEquals(List.of("Update"), result.updated());
		assertEquals(List.of("Delete"), result.deleted());
		assertEquals(Map.of("Rename", "Renamed"), result.renamed());
		Outcome update = result.outcomes().get(0);
		assertEquals(Integer.valueOf(1), update.oldVersion());
		assertEquals(Integer.valueOf(2), update.newVersion());
		Outcome created = result.outcomes().get(1);
		assertNull(created.oldVersion());
		assertEquals(Integer.valueOf(1), created.newVersion());
		assertNull(result.outcomes().get(3).newVersion());

		// update, created, written, deleted, renamed and renamed target updated
		assertEquals(6, notifications.size());
		ArticleUpdateEvent updateEvent = (ArticleUpdateEvent) notifications.get(0);
		assertEquals(2, updateEvent.getVersion().getWikiVersionNumber());
		assertFrameReleased();
	}

	@Test
	public void renameMovesThePageAndRegistersUpdatedReferrers() throws Exception {
		Article original = manager.getArticle("Rename");
		referrerRewrites.put("Other", "links to Renamed");
		int compilation = manager.getCompilerManager().getCompilationId();

		ArticleChangeSet.Result result = ArticleChangeSet.builder()
				.rename("Rename", "Renamed")
				.build()
				.applyAs(environment, "author", "note");

		manager.getCompilerManager().awaitTermination();
		assertEquals(compilation + 1, manager.getCompilerManager().getCompilationId());
		assertEquals(Map.of("Rename", "Renamed"), result.renamed());
		assertEquals(Integer.valueOf(2), result.outcomes().get(0).newVersion());
		assertNull(manager.getArticle("Rename"));
		assertEquals("original", manager.getArticle("Renamed").getText());
		assertEquals("links to Renamed", manager.getArticle("Other").getText());
		assertEquals(3, notifications.size());
		ArticleRenamedEvent renamed = (ArticleRenamedEvent) notifications.get(0);
		assertSame(original, renamed.getOldArticle());
		assertEquals("Renamed", renamed.getArticle().getTitle());
		assertEquals("Renamed", ((ArticleUpdateEvent) notifications.get(1)).getTitle());
		assertEquals("Other", ((ArticleUpdateEvent) notifications.get(2)).getTitle());
		assertFrameReleased();
	}

	@Test
	public void applyFailureRollsBackPersistenceBeforeTheArticleManager() throws Exception {
		Article original = manager.getArticle("Update");
		int compilation = manager.getCompilerManager().getCompilationId();
		failingWrite = "Other";
		List<Article> seenDuringRollback = new ArrayList<>();
		onRollback = () -> seenDuringRollback.add(manager.getArticle("Update"));

		ArticleChangeException failure = assertThrows(ArticleChangeException.class, () -> ArticleChangeSet.builder()
				.update("Update", "updated")
				.create("Created", "created")
				.delete("Delete")
				.rename("Rename", "Renamed", "renamed")
				.update("Other", "updated")
				.build()
				.applyAs(environment, "author", "note"));

		assertEquals(Reason.FAILED, failure.reason());
		assertEquals("Other", failure.title());
		assertNotSame("Persistence must be rolled back while the frame still holds the change", original,
				seenDuringRollback.get(0));
		assertSame(original, manager.getArticle("Update"));
		assertNotNull(manager.getArticle("Delete"));
		assertNotNull(manager.getArticle("Rename"));
		assertNull(manager.getArticle("Created"));
		assertNull(manager.getArticle("Renamed"));
		assertEquals("original", disk.get("Update"));
		assertEquals(List.of("open", "rename Rename -> Renamed by author", "rollback"), transactions);
		assertEquals("original", disk.get("Rename"));
		assertFalse(disk.containsKey("Renamed"));
		assertTrue(manager.getQueuedArticles().isEmpty());
		assertEquals(compilation, manager.getCompilerManager().getCompilationId());
		assertTrue(notifications.isEmpty());
		assertFrameReleased();
	}

	@Test
	public void persistenceRollbackFailureIsSuppressedAndTheFrameIsStillReleased() throws Exception {
		failingWrite = "Other";
		IllegalStateException rollbackFailure = new IllegalStateException("rollback failed");
		onRollback = () -> { throw rollbackFailure; };

		ArticleChangeException failure = assertThrows(ArticleChangeException.class, () -> ArticleChangeSet.builder()
				.create("Created", "created")
				.update("Other", "updated")
				.build()
				.applyAs(environment, "author", "note"));

		assertArrayEquals(new Throwable[] { rollbackFailure }, failure.getSuppressed());
		assertNull(manager.getArticle("Created"));
		assertFrameReleased();
	}

	@Test
	public void versionPreconditionsAreCheckedBeforeAnyWrite() throws Exception {
		ArticleChangeException failure = assertThrows(ArticleChangeException.class, () -> ArticleChangeSet.builder()
				.write("Update", "updated").expectVersion("Update", 1)
				.write("Other", "updated").expectVersion("Other", 7)
				.build()
				.applyAs(environment, "author", "note"));

		assertEquals(Reason.CONFLICT, failure.reason());
		assertEquals("Other", failure.title());
		assertEquals("original", disk.get("Update"));
		assertEquals(List.of("open", "rollback"), transactions);

		transactions.clear();
		ArticleChangeSet.Result result = ArticleChangeSet.builder()
				.write("Update", "updated").expectVersion("Update", 1)
				.write("New", "new").expectVersion("New", null)
				.build()
				.applyAs(environment, "author", "note");
		assertEquals(List.of("New"), result.created());
		assertEquals(List.of("open", "commit"), transactions);
	}

	@Test
	public void existenceIsAPrecondition() {
		for (ArticleChangeSet changes : List.of(
				ArticleChangeSet.builder().create("Update", "x").build(),
				ArticleChangeSet.builder().update("Missing", "x").build(),
				ArticleChangeSet.builder().delete("Missing").build(),
				ArticleChangeSet.builder().rename("Rename", "Other").build())) {
			ArticleChangeException failure = assertThrows(ArticleChangeException.class,
					() -> changes.applyAs(environment, "author", "note"));
			assertEquals(Reason.CONFLICT, failure.reason());
		}
		assertTrue(notifications.isEmpty());
	}

	@Test
	public void userContextChecksPermissions() throws Exception {
		UserContext user = user("user");
		denied.add("Other");
		ArticleChangeException failure = assertThrows(ArticleChangeException.class, () -> ArticleChangeSet.builder()
				.update("Update", "updated")
				.update("Other", "updated")
				.build()
				.apply(user, "note"));
		assertEquals(Reason.FORBIDDEN, failure.reason());
		assertEquals("Other", failure.title());
		assertEquals("original", disk.get("Update"));

		denied.clear();
		denied.add("*create*");
		failure = assertThrows(ArticleChangeException.class,
				() -> ArticleChangeSet.builder().write("New", "new").build().apply(user, "note"));
		assertEquals(Reason.FORBIDDEN, failure.reason());

		denied.clear();
		ArticleChangeSet.builder().write("New", "new").build().apply(user, "note");
		assertEquals("new", disk.get("New"));
		assertFrameReleased();
	}

	@Test
	public void guardsVetoBeforeTheTransactionOpens() {
		ArticleChangeSet changes = ArticleChangeSet.builder().update("Update", "updated").build();
		ArticleChangeGuard guard = (environment, author, set) -> {
			assertEquals(Set.of("Update"), set.titles());
			throw new ArticleChangeException(Reason.LOCKED, "Update", "locked by task 1");
		};

		ArticleChangeException failure = assertThrows(ArticleChangeException.class,
				() -> new ArticleChangeTransaction(changes, environment, "author", null, "note", List.of(guard)).run());

		assertEquals(Reason.LOCKED, failure.reason());
		assertTrue(transactions.isEmpty());
		assertEquals("original", disk.get("Update"));
	}

	@Test
	public void emptySetDoesNotOpenATransaction() throws Exception {
		ArticleChangeSet.Result result = ArticleChangeSet.builder().build().applyAs(environment, "author", "note");
		assertTrue(result.outcomes().isEmpty());
		assertTrue(transactions.isEmpty());
	}

	@Test
	public void builderRejectsTouchingAnArticleTwice() {
		assertThrows(IllegalArgumentException.class,
				() -> ArticleChangeSet.builder().update("A", "x").delete("a"));
		assertThrows(IllegalArgumentException.class,
				() -> ArticleChangeSet.builder().rename("A", "B").create("B", "x"));
		assertThrows(IllegalStateException.class,
				() -> ArticleChangeSet.builder().update("A", "x").expectVersion("B", 1).build());
	}

	private UserContext user(String name) {
		return (UserContext) Proxy.newProxyInstance(UserContext.class.getClassLoader(), new Class<?>[] { UserContext.class },
				(proxy, method, args) -> switch (method.getName()) {
					case "getUserName" -> name;
					case "getEnvironment" -> environment;
					case "getRequest" -> null;
					case "hashCode" -> System.identityHashCode(proxy);
					case "equals" -> proxy == args[0];
					default -> throw new UnsupportedOperationException(method.getName());
				});
	}

	private void assertFrameReleased() throws Exception {
		// a same-thread check would miss a leaked reentrant hold
		ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			return thread;
		});
		try {
			executor.submit(() -> {
				manager.open();
				manager.rollback();
			}).get(5, TimeUnit.SECONDS);
		}
		finally {
			executor.shutdownNow();
		}
	}
}
