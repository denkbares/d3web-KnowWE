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

package de.knowwe.search.render;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.denkbares.plugin.test.InitPluginManager;
import connector.DummyConnector;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.rendering.RenderResult;
import utils.TestUserContext;
import utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Guards the rule that keeps a rendered markup block in one piece.
 * <p>
 * Without it, JSPWiki's parser makes a paragraph out of every rendered line and puts the {@code <p>} inside a
 * {@code <span>}; that nesting is invalid, so the browser tears the block apart while parsing. A rule block showed
 * twelve paragraphs where the article itself has none.
 * <p>
 * The mask tokens are taken from {@link RenderResult} rather than written out, so this still tests the right thing if
 * the encoding ever changes.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class SearchResultRendererTest {

	private RenderResult result;
	private String tagEnd;
	private String tagStart;
	private String lineBreak;

	@Before
	public void setUp() throws IOException {
		InitPluginManager.init();
		if (!Environment.isInitialized()) {
			DummyConnector connector = new DummyConnector();
			connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
			Environment.initInstance(connector);
		}
		result = new RenderResult(new TestUserContext("Test"));
		tagEnd = RenderResult.mask(">", result);
		tagStart = RenderResult.mask("<", result);
		lineBreak = RenderResult.mask("<br/>", result);
	}

	@Test
	public void aNewlineBetweenTwoTagsBecomesOneBreak() {
		assertEquals(tagEnd + lineBreak + tagStart, join(tagEnd + "\n" + tagStart));
	}

	@Test
	public void aBlankLineBecomesTwoBreaks() {
		// this is what separates one rule from the next; a single break would make them look like one block
		assertEquals(tagEnd + lineBreak + lineBreak + tagStart, join(tagEnd + "\n\n" + tagStart));
	}

	@Test
	public void theBlankLineCaseWinsOverTheSingleLineCase() {
		// order matters: matched the other way round, the simpler pattern swallows the blank line
		String mixed = tagEnd + "\n" + tagStart + "x" + tagEnd + "\n\n" + tagStart;
		assertEquals(tagEnd + lineBreak + tagStart + "x" + tagEnd + lineBreak + lineBreak + tagStart, join(mixed));
	}

	@Test
	public void indentationAroundTheNewlineGoesAway() {
		assertEquals(tagEnd + lineBreak + tagStart, join(tagEnd + "   \n   " + tagStart));
	}

	@Test
	public void aNewlineInRunningTextIsLeftAlone() {
		// this is what keeps prose previews their paragraphs
		String prose = tagEnd + "Der Steckverbinder\nwird geprueft." + tagStart;
		assertEquals(prose, join(prose));
	}

	@Test
	public void textNextToATagIsLeftAlone() {
		assertEquals(tagEnd + "Text\n" + tagStart, join(tagEnd + "Text\n" + tagStart));
		assertEquals(tagEnd + "\nText" + tagStart, join(tagEnd + "\nText" + tagStart));
	}

	@Test
	public void aLongBlockKeepsItsLinesUnderTheParserLimit() {
		// the parser does not wrap a longer line, it drops everything past 10239 characters
		StringBuilder block = new StringBuilder();
		for (int i = 0; i < 2000; i++) {
			block.append(tagEnd).append("\n").append(tagStart).append("Zeile ").append(i);
		}
		String joined = join(block.toString());
		int longest = 0;
		for (String line : joined.split("\n", -1)) {
			longest = Math.max(longest, line.length());
		}
		assertTrue("laengste Zeile: " + longest, longest < 10 * 1024);
	}

	@Test
	public void nothingIsLostWhileJoining() {
		// every line of the block has to survive, whether it was joined or left on a line of its own
		StringBuilder block = new StringBuilder();
		for (int i = 0; i < 2000; i++) {
			block.append(tagEnd).append("\n").append(tagStart).append("Zeile ").append(i);
		}
		String joined = join(block.toString());
		for (int i = 0; i < 2000; i++) {
			assertTrue("Zeile " + i + " fehlt", joined.contains("Zeile " + i));
		}
	}

	@Test
	public void unmaskedWikiMarkupIsNotTouched() {
		// wiki syntax still has to reach the parser; only rendered HTML is joined
		String wiki = "!! Heading\n\nEin Absatz.\n";
		assertEquals(wiki, join(wiki));
	}

	private String join(String maskedHtml) {
		return SearchResultRenderer.joinRenderedLines(maskedHtml, result);
	}
}
