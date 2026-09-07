/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core;

import java.io.IOException;

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
		if (Environment.isInitialized()) Environment.getInstance().shutdown();
	}

	@After
	public void tearDown() {
		if (Environment.isInitialized()) Environment.getInstance().shutdown();
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
	public void shutdownResetsStaticRegistries() throws InterruptedException {
		Environment environment = start();
		Article article = createArticle(environment);
		String sectionId = article.getRootSection().getID();
		assertNotNull("section id must resolve while the environment is running", Sections.get(sectionId));
		assertFalse("decorating the type tree registers compile scripts", CompilerManager.getScriptManagers().isEmpty());

		environment.shutdown();

		assertFalse(Environment.isInitialized());
		assertNull("section ids of the old lifecycle must not resolve anymore", Sections.get(sectionId));
		assertTrue("script managers must be gone", CompilerManager.getScriptManagers().isEmpty());
	}

	@Test
	public void reinitializedEnvironmentDoesNotInheritDecorations() throws InterruptedException {
		Environment first = start();
		int rootChildren = first.getRootType().getChildrenTypes().size();
		int plainTextChildren = PlainText.getInstance().getChildrenTypes().size();
		PlainText firstPlainText = PlainText.getInstance();
		createArticle(first);
		first.shutdown();

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
