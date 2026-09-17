/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.search.index;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.denkbares.plugin.test.InitPluginManager;
import connector.DummyConnector;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.jspwiki.types.HeaderType;
import utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class SectionAnchorTest {

	private static final String CONTENT = """
			!! Montage
			Den Steckverbinder pruefen.
			!! Lagerung
			Trocken lagern.
			""";

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		if (!Environment.isInitialized()) {
			DummyConnector connector = new DummyConnector();
			connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
			Environment.initInstance(connector);
		}
	}

	@Test
	public void aValidIdResolvesDirectlyAndIsNotStale() {
		Article article = register("Direkt", CONTENT);
		Section<?> lagerung = header(article, "Lagerung");

		SectionAnchor.Resolution resolution = new SectionAnchor("Direkt", lagerung.getID(),
				SectionDocumentBuilder.positionPath(lagerung), "Lagerung").resolve(manager());

		assertEquals(lagerung, resolution.section());
		assertFalse(resolution.stale());
	}

	@Test
	public void anUnknownIdFallsBackToThePositionInKdom() {
		Article article = register("Fallback", CONTENT);
		Section<?> lagerung = header(article, "Lagerung");

		SectionAnchor.Resolution resolution = new SectionAnchor("Fallback", "cafebabe",
				SectionDocumentBuilder.positionPath(lagerung), "Lagerung").resolve(manager());

		assertEquals("the position must find the same section", lagerung, resolution.section());
		assertTrue("and the hit must be marked as possibly outdated", resolution.stale());
	}

	@Test
	public void anIdOfAnotherPageIsNotAccepted() {
		register("Eigen", CONTENT);
		Article other = register("Fremd", CONTENT);
		Section<?> foreign = header(other, "Montage");

		SectionAnchor.Resolution resolution =
				new SectionAnchor("Eigen", foreign.getID(), "", "Montage").resolve(manager());

		assertEquals("must not silently show a section of a different page",
				"Eigen", resolution.section().getTitle());
	}

	@Test
	public void aChangedStructureStillFindsTheHeadingByItsText() {
		register("Verschoben", CONTENT);
		// a paragraph is inserted at the top, so every position shifts
		register("Verschoben", "Neue Einleitung.\n" + CONTENT);

		SectionAnchor.Resolution resolution =
				new SectionAnchor("Verschoben", "cafebabe", "0.99", "Lagerung").resolve(manager());

		assertNotNull(resolution.section());
		assertTrue("expected the Lagerung heading, got " + resolution.section().getText().trim(),
				resolution.section().getText().contains("Lagerung"));
		assertTrue(resolution.stale());
	}

	@Test
	public void aVanishedSectionFallsBackToTheArticle() {
		Article article = register("Geschrumpft", CONTENT);
		register("Geschrumpft", "Nur noch ein Satz.\n");

		SectionAnchor.Resolution resolution =
				new SectionAnchor("Geschrumpft", "cafebabe", "0.42", "Lagerung").resolve(manager());

		assertNotNull("a hit must still render something rather than nothing", resolution.section());
		assertTrue(resolution.stale());
		assertEquals("Geschrumpft", resolution.section().getTitle());
	}

	@Test
	public void aDeletedPageResolvesToNothing() {
		SectionAnchor.Resolution resolution =
				new SectionAnchor("GibtEsNicht", "cafebabe", "0", "Egal").resolve(manager());

		assertFalse(resolution.isResolved());
		assertTrue(resolution.stale());
	}

	private static ArticleManager manager() {
		return Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
	}

	private static Article register(String title, String content) {
		manager().registerArticle(title, content);
		return Environment.getInstance().getArticle(Environment.DEFAULT_WEB, title);
	}

	private static Section<?> header(Article article, String text) {
		for (Section<HeaderType> header : de.knowwe.core.kdom.parsing.Sections.successors(article, HeaderType.class)) {
			if (text.equals(header.get().getHeaderText(header))) return header;
		}
		throw new IllegalArgumentException("no heading '" + text + "' in " + article.getTitle());
	}
}
