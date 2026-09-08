/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.denkbares.plugin.test.InitPluginManager;
import connector.DummyConnector;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.parsing.Sections;

import static org.junit.Assert.*;

/**
 * Checks the lifecycle of the {@link Environment}: an environment can be shut down and a fresh one can be started in
 * the same JVM without inheriting state from the previous one, and article managers know their environment.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 * @created 07.09.2026
 */
@SuppressWarnings("deprecation") // the test exercises the singleton lifecycle itself
public class EnvironmentLifecycleTest {

	private static final String TITLE = "EnvironmentLifecycle";
	private static final String CONTENT = "Some plain text\n\n%%Package\nlifecycle\n%\n";

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		if (Environment.isInitialized()) Environment.getInstance().close();
	}

	@After
	public void tearDown() {
		if (Environment.isInitialized()) Environment.getInstance().close();
	}

	@Test
	public void articleManagerKnowsItsEnvironment() throws InterruptedException {
		Environment environment = start();
		ArticleManager manager = environment.getArticleManager(Environment.DEFAULT_WEB);
		assertSame(environment, manager.getEnvironment());

		Article article = createArticle(environment);
		assertSame(environment, article.getArticleManager().getEnvironment());
		assertSame(environment, article.getRootSection().getArticleManager().getEnvironment());
		assertSame(environment, manager.getCompilerManager().getArticleManager().getEnvironment());
	}

	@Test
	public void closeReleasesEverythingTheEnvironmentOwns() throws InterruptedException {
		Environment environment = start();
		Article article = createArticle(environment);
		String sectionId = article.getRootSection().getID();
		assertNotNull("section id must resolve while the environment is running", Sections.get(sectionId));
		assertSame(article.getRootSection(), environment.getSection(sectionId));
		assertFalse("decorating the type tree registers compile scripts", environment.getScriptManagers().isEmpty());
		CompilerManager compilerManager = environment.getArticleManager(Environment.DEFAULT_WEB).getCompilerManager();

		environment.close();

		assertTrue(environment.isClosed());
		assertFalse(Environment.isInitialized());
		assertNull("section ids of the old lifecycle must not resolve anymore", Sections.get(sectionId));
		assertNull(environment.getSection(sectionId));
		assertTrue("script managers must be gone", environment.getScriptManagers().isEmpty());
		assertTrue("compile threads must be stopped", compilerManager.isShutdown());
	}

	@Test
	public void servicesAndCloseTasksAreClosedInReverseOrder() {
		Environment environment = start();
		List<String> order = new ArrayList<>();
		ClosableService first = environment.getService(ClosableService.class, () -> new ClosableService("first", order));
		assertSame("service is created once per environment", first, environment.getService(ClosableService.class, () -> new ClosableService("again", order)));
		assertSame(first, environment.getService(ClosableService.class));
		environment.getService(OtherService.class, () -> new OtherService("second", order));
		environment.onClose(() -> order.add("task-1"));
		environment.onClose(() -> order.add("task-2"));

		environment.close();
		environment.close(); // idempotent

		assertEquals(List.of("task-2", "task-1", "second", "first"), order);
		assertNull("services are released on close", environment.getService(ClosableService.class));
		assertThrows(IllegalStateException.class, () -> environment.getService(ClosableService.class, () -> new ClosableService("late", order)));
		assertThrows(IllegalStateException.class, () -> environment.onClose(() -> order.add("late")));
	}

	@Test
	public void twoEnvironmentsDoNotShareState() throws InterruptedException {
		Environment first = start();
		Article article = createArticle(first);
		String sectionId = article.getRootSection().getID();
		first.close();

		Environment second = start();
		assertNull("sections of the first environment are unknown to the second", second.getSection(sectionId));
		assertNull(second.getService(ClosableService.class));
		assertNotSame(first.getArticleManager(Environment.DEFAULT_WEB), second.getArticleManager(Environment.DEFAULT_WEB));
	}

	@Test
	public void initInstanceClosesAStillRunningEnvironment() throws InterruptedException {
		Environment first = start();
		createArticle(first);
		List<String> closed = new ArrayList<>();
		first.onClose(() -> closed.add("first"));

		Environment second = start(); // without closing the first one
		assertTrue("the previous environment must be closed", first.isClosed());
		assertEquals(List.of("first"), closed);
		assertNotSame(first, second);
		assertTrue(Environment.isInitialized());
		assertSame(second, Environment.getInstance());
	}

	private record ClosableService(String name, List<String> order) implements AutoCloseable {
		@Override
		public void close() {
			order.add(name);
		}
	}

	private record OtherService(String name, List<String> order) implements AutoCloseable {
		@Override
		public void close() {
			order.add(name);
		}
	}

	@Test
	public void reinitializedEnvironmentDoesNotInheritDecorations() throws InterruptedException {
		Environment first = start();
		int rootChildren = first.getRootType().getChildrenTypes().size();
		int plainTextChildren = PlainText.getInstance().getChildrenTypes().size();
		PlainText firstPlainText = PlainText.getInstance();
		createArticle(first);
		first.close();

		Environment second = start();
		assertNotSame(first, second);
		assertNotSame("the type tree must be rebuilt", first.getRootType(), second.getRootType());
		assertEquals("plugged children must not accumulate", rootChildren, second.getRootType()
				.getChildrenTypes()
				.size());
		assertNotSame("singleton types must be recreated", firstPlainText, PlainText.getInstance());
		assertEquals(plainTextChildren, PlainText.getInstance().getChildrenTypes().size());
		for (Type child : second.getRootType().getChildrenTypes()) {
			assertFalse("type tree of the new environment must not contain the old root", child == first.getRootType());
		}

		Article article = createArticle(second);
		assertSame(second, article.getArticleManager().getEnvironment());
		assertNotNull(Sections.get(article.getRootSection().getID()));
	}

	private static Environment start() {
		Environment.initInstance(new DummyConnector());
		assertTrue(Environment.isInitialized());
		return Environment.getInstance();
	}

	private static Article createArticle(Environment environment) throws InterruptedException {
		environment.getWikiConnector().createArticle(TITLE, "test", CONTENT);
		ArticleManager manager = environment.getArticleManager(Environment.DEFAULT_WEB);
		manager.getCompilerManager().awaitTermination();
		Article article = manager.getArticle(TITLE);
		assertNotNull(article);
		return article;
	}
}
