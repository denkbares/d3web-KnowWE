/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.include;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import connector.DummyConnector;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.TestUserContext;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.user.UserContext;

import static de.knowwe.core.kdom.parsing.Sections.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests that rendering included sections detects cyclic includes, but does not serialize all renderings of included
 * sections of the wiki: a slow included section must not block the page views of other users.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 26.11.2026
 */
public class FramedIncludedSectionRendererTest {

	private static final long TIMEOUT_SECONDS = 10;
	private static final String CYCLE_ERROR = "Cyclic include detected";

	private final CountDownLatch entered = new CountDownLatch(1);
	private final CountDownLatch released = new CountDownLatch(1);
	private final AtomicInteger slowRenderings = new AtomicInteger();

	private Environment environment;
	private ExecutorService executor;

	@Before
	public void setUp() {
		RootType root = new RootType();
		// the first rendering of a slow section waits until released, the following ones do not
		root.addChildType(new MarkerType("SLOW", (section, user, result) -> {
			if (slowRenderings.getAndIncrement() == 0) {
				entered.countDown();
				try {
					released.await(TIMEOUT_SECONDS * 3, TimeUnit.SECONDS);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			result.append("slow content");
		}));
		root.addChildType(new MarkerType("CHAPTER", (section, user, result) -> result.append("[chapter content]")));
		root.addChildType(new IncludeType());
		environment = Environment.create(new DummyConnector(), root);
		executor = Executors.newCachedThreadPool();
	}

	@After
	public void tearDown() {
		released.countDown();
		executor.shutdownNow();
		environment.close();
	}

	@Test(timeout = 60000)
	public void slowIncludedSectionDoesNotBlockOtherRenderings() throws Exception {
		Section<MarkerType> slow = section("Slow", "SLOW");
		Future<String> first = executor.submit(() -> renderIncluded(slow));
		assertTrue("first rendering did not start", entered.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

		// the same section, as on two page views at the same time: no cycle, and no waiting for the other view
		Future<String> second = executor.submit(() -> renderIncluded(slow));
		String content;
		try {
			content = second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		}
		catch (TimeoutException e) {
			fail("rendering an included section was blocked by the slow rendering of another one");
			return;
		}
		assertTrue(content, content.contains("slow content"));
		assertFalse(content, content.contains(CYCLE_ERROR));

		released.countDown();
		assertTrue(first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS).contains("slow content"));
	}

	@Test(timeout = 60000)
	public void pagesIncludingEachOtherAreACycle() throws Exception {
		Section<RootType> a = page("A", "INCLUDE:B CHAPTER");
		Section<RootType> b = page("B", "INCLUDE:A CHAPTER");
		for (Section<RootType> page : List.of(a, b)) {
			// the include of the other page reports the cycle at once, instead of rendering into it
			String content = executor.submit(() -> view(page)).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			assertEquals(content, 1, count(content, CYCLE_ERROR));
			assertEquals(content, 1, count(content, "[chapter content]"));
			// the same again, the detection does not remember the former rendering
			assertEquals(content, view(page));
		}
	}

	@Test(timeout = 60000)
	public void pageIncludingItselfIsACycle() throws Exception {
		Section<RootType> self = page("Self", "INCLUDE:Self CHAPTER");
		String content = executor.submit(() -> view(self)).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		assertEquals(content, 1, count(content, CYCLE_ERROR));
		assertEquals(content, 1, count(content, "[chapter content]"));
	}

	@Test(timeout = 60000)
	public void includeOfACycleReportsIt() throws Exception {
		page("A", "INCLUDE:B CHAPTER");
		page("B", "INCLUDE:A CHAPTER");
		Section<RootType> book = page("Book", "INCLUDE:A");
		String content = executor.submit(() -> view(book)).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		assertEquals(content, 1, count(content, CYCLE_ERROR));
	}

	@Test(timeout = 60000)
	public void sectionIncludedTwiceIsNoCycle() throws Exception {
		page("Chapter", "CHAPTER");
		page("Part", "INCLUDE:Chapter");
		// the chapter directly and through the part: two paths to the same section, but no cycle
		Section<RootType> book = page("Book", "INCLUDE:Chapter INCLUDE:Part INCLUDE:Part");
		String content = view(book);
		assertFalse(content, content.contains(CYCLE_ERROR));
		assertEquals(content, 3, count(content, "[chapter content]"));
	}

	private Section<MarkerType> section(String title, String text) {
		Section<MarkerType> section = $(page(title, text)).successor(MarkerType.class).getFirst();
		assertNotNull("no section for " + text, section);
		return section;
	}

	private Section<RootType> page(String title, String text) {
		environment.getArticleManager().registerArticle(title, text);
		Article article = environment.getArticleManager().getArticle(title);
		assertNotNull(article);
		return article.getRootSection();
	}

	/**
	 * Renders the given section as included section, as e.g. <code>%%Include</code> does.
	 */
	private static String renderIncluded(Section<?> section) {
		UserContext user = new TestUserContext(section.getArticle());
		RenderResult result = new RenderResult(user);
		FramedIncludedSectionRenderer.renderTargetSections(section, false, user, result);
		return result.toStringRaw();
	}

	/**
	 * Renders the given page as on a page view.
	 */
	private static String view(Section<RootType> page) {
		UserContext user = new TestUserContext(page.getArticle());
		return new RenderResult(user).append(page, user).toStringRaw();
	}

	private static int count(String text, String part) {
		int count = 0;
		for (int index = text.indexOf(part); index >= 0; index = text.indexOf(part, index + 1)) count++;
		return count;
	}

	/**
	 * Type of a section of the text of a marker, rendered by the specified renderer.
	 */
	private static class MarkerType extends AbstractType {
		MarkerType(String marker, Renderer renderer) {
			setSectionFinder(new RegexSectionFinder(marker));
			setRenderer(renderer);
		}
	}

	/**
	 * Includes the page of the name after <code>INCLUDE:</code>, like a (much simplified) <code>%%Include</code>.
	 */
	private static class IncludeType extends AbstractType implements IncludingType {
		IncludeType() {
			setSectionFinder(new RegexSectionFinder("INCLUDE:\\w+"));
			setRenderer((section, user, result) -> {
				for (Section<?> included : getIncludedSections(section, user)) {
					FramedIncludedSectionRenderer.renderTargetSections(included, false, user, result);
				}
			});
		}

		@Override
		public @NotNull List<Section<?>> getIncludedSections(Section<?> section, UserContext user) {
			Article article = section.getArticleManager().getArticle(section.getText().substring("INCLUDE:".length()));
			return article == null ? List.of() : List.of(article.getRootSection());
		}
	}
}
