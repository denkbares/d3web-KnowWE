/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.denkbares.plugin.test.InitPluginManager;
import connector.DummyConnector;
import utils.TypeTrees;
import de.knowwe.core.compile.CompilerManager;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Sections;

import static org.junit.Assert.*;

/**
 * Checks the lifecycle of the {@link Environment}: an environment can be shut down and a fresh one can be started in
 * the same JVM without inheriting state from the previous one, and article managers know their environment.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for env-instance
 * @created 07.09.2026
 */
public class EnvironmentLifecycleTest {

	private static final String TITLE = "EnvironmentLifecycle";
	private static final String CONTENT = "Some plain text\n\n%%Package\nlifecycle\n%\n";

	private final List<Environment> started = new ArrayList<>();

	/** Records what the test services and close tasks did, in order. Static, so the service factories can reach it. */
	private static final List<String> ORDER = new ArrayList<>();

	@Before
	public void setUp() throws IOException {
		ORDER.clear();
		InitPluginManager.init();
	}

	@After
	public void tearDown() {
		started.forEach(Environment::close);
		started.clear();
	}

	@Test
	public void articleManagerKnowsItsEnvironment() throws InterruptedException {
		Environment environment = start();
		ArticleManager manager = environment.getArticleManager();
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
		assertNotNull("section id must resolve while the environment is running", Sections.get(environment, sectionId));
		assertSame(article.getRootSection(), environment.getSection(sectionId));
		assertFalse("decorating the type tree registers compile scripts", environment.getScriptManagers().isEmpty());
		CompilerManager compilerManager = environment.getArticleManager().getCompilerManager();

		environment.close();

		assertTrue(environment.isClosed());
		assertNull("section ids of the old lifecycle must not resolve anymore", Sections.get(environment, sectionId));
		assertNull(environment.getSection(sectionId));
		assertTrue("script managers must be gone", environment.getScriptManagers().isEmpty());
		assertTrue("compile threads must be stopped", compilerManager.isShutdown());
	}

	@Test
	public void servicesAndCloseTasksAreClosedInReverseOrder() {
		Environment environment = start();
		ClosableService first = environment.getService(ClosableService.SERVICE);
		assertSame("service is created once per environment", first, environment.getService(ClosableService.SERVICE));
		assertSame(first, environment.getService(ClosableService.class));
		environment.getService(OtherService.SERVICE);
		environment.onClose(() -> ORDER.add("task-1"));
		environment.onClose(() -> ORDER.add("task-2"));

		environment.close();
		environment.close(); // idempotent

		assertEquals(List.of("task-2", "task-1", "second", "first"), ORDER);
		assertNull("services are released on close", environment.getService(ClosableService.class));
		assertThrows(IllegalStateException.class, () -> environment.getService(ClosableService.SERVICE));
		assertThrows(IllegalStateException.class, () -> environment.onClose(() -> ORDER.add("late")));
	}

	/** A service is declared once, by its own class; a second declaration of the same type is a programming error. */
	@Test
	public void aSecondDeclarationOfTheSameServiceIsRefused() {
		Environment environment = start();
		environment.getService(ClosableService.SERVICE);

		ServiceKey<ClosableService> other = ServiceKey.of(ClosableService.class, env -> new ClosableService("other"));
		assertThrows(IllegalStateException.class, () -> environment.getService(other));
		assertSame("the service of the first declaration stays", environment.getService(ClosableService.class),
				environment.getService(ClosableService.SERVICE));
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
		assertNotSame(first.getArticleManager(), second.getArticleManager());
	}

	@Test
	public void environmentsRunSideBySide() throws InterruptedException {
		Environment first = start();
		Article firstArticle = createArticle(first);
		List<String> closed = new ArrayList<>();
		first.onClose(() -> closed.add("first"));

		Environment second = start(); // without closing the first one
		assertFalse("the previous environment must stay open", first.isClosing());
		assertTrue(closed.isEmpty());
		assertNotSame(first, second);
		Article secondArticle = createArticle(second);
		assertSame(first, firstArticle.getEnvironment());
		assertSame(second, secondArticle.getEnvironment());
		assertNotSame(firstArticle, secondArticle);
		assertNull("section ids are scoped to their environment", second.getSection(firstArticle.getRootSection().getID()));

		Set<String> markups = TypeTrees.markupTypeNames(first.getRootType());
		assertFalse("the type tree must contain markups, otherwise the comparison says nothing", markups.isEmpty());
		assertEquals("both environments must know the same markups", markups,
				TypeTrees.markupTypeNames(second.getRootType()));
		assertEquals("markup types must be built per environment", List.of(),
				TypeTrees.sharedMarkupTypes(first.getRootType(), second.getRootType()));
		assertEquals("no markup may have two types for the same annotation", List.of(),
				TypeTrees.duplicateAnnotationTypes(second.getRootType()));

		first.close();
		assertEquals(List.of("first"), closed);
		assertFalse("closing one environment leaves the other one running", second.isClosing());
		assertSame(secondArticle, second.getArticleManager().getArticle(TITLE));
	}

	@Test
	public void serviceCreatedWhileClosingIsClosedAsWell() {
		Environment environment = start();
		// a service that reaches for another service while it is shut down itself
		environment.getService(ResurrectingService.SERVICE);

		environment.close();

		assertEquals("a service created while closing must be closed as well",
				List.of("resurrecting", "first"), ORDER);
		assertNull(environment.getService(ClosableService.class));
	}

	private record ResurrectingService(Environment environment) implements AutoCloseable {

		private static final ServiceKey<ResurrectingService> SERVICE =
				ServiceKey.of(ResurrectingService.class, ResurrectingService::new);

		@Override
		public void close() {
			ORDER.add("resurrecting");
			environment.getService(ClosableService.SERVICE);
		}
	}

	private record ClosableService(String name) implements AutoCloseable {

		private static final ServiceKey<ClosableService> SERVICE =
				ServiceKey.of(ClosableService.class, environment -> new ClosableService("first"));

		@Override
		public void close() {
			ORDER.add(name);
		}
	}

	private record OtherService(String name) implements AutoCloseable {

		private static final ServiceKey<OtherService> SERVICE =
				ServiceKey.of(OtherService.class, environment -> new OtherService("second"));

		@Override
		public void close() {
			ORDER.add(name);
		}
	}

	@Test
	public void reinitializedEnvironmentDoesNotInheritDecorations() throws InterruptedException {
		Environment first = start();
		int rootChildren = first.getRootType().getChildrenTypes().size();
		createArticle(first);
		first.close();

		Environment second = start();
		assertNotSame(first, second);
		assertNotSame("the type tree must be rebuilt", first.getRootType(), second.getRootType());
		assertEquals("plugged children must not accumulate", rootChildren, second.getRootType()
				.getChildrenTypes()
				.size());
		for (Type child : second.getRootType().getChildrenTypes()) {
			assertFalse("type tree of the new environment must not contain the old root", child == first.getRootType());
		}

		Article article = createArticle(second);
		assertSame(second, article.getArticleManager().getEnvironment());
		assertNotNull(Sections.get(second, article.getRootSection().getID()));
	}

	@Test
	public void temporaryArticlesResolveTheirEnvironmentAndArticleManager() throws InterruptedException {
		Environment environment = start();
		Article temporary = Article.createTemporaryArticle(environment, CONTENT, "Preview");
		assertTrue(temporary.isTemporary());
		assertNull(temporary.getArticleManager());
		assertSame(environment, temporary.getEnvironment());
		assertSame(environment, temporary.getRootSection().getEnvironment());
		assertSame(environment.getArticleManager(), KnowWEUtils.getArticleManager(temporary));
		assertSame(environment.getArticleManager(), KnowWEUtils.getArticleManager(temporary.getRootSection()));
		// compilers can be looked up for preview sections
		assertNotNull(Compilers.getCompilers(null, temporary.getRootSection(), Compiler.class));
	}

	private Environment start() {
		Environment environment = Environment.create(new DummyConnector());
		assertTrue(environment.isReady());
		started.add(environment);
		return environment;
	}

	private static Article createArticle(Environment environment) throws InterruptedException {
		environment.getWikiConnector().createArticle(TITLE, "test", CONTENT);
		ArticleManager manager = environment.getArticleManager();
		manager.getCompilerManager().awaitTermination();
		Article article = manager.getArticle(TITLE);
		assertNotNull(article);
		return article;
	}
}
