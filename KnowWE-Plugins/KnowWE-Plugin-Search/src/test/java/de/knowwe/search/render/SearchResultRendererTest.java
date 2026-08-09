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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Guards the rule that keeps a rendered markup block in one piece.
 * <p>
 * Without it, JSPWiki's parser turns every rendered line into a paragraph and puts {@code <p>} inside a {@code <span>};
 * since that nesting is invalid, the browser tears the block apart while parsing. A rule block showed twelve paragraphs
 * where the article itself has none.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class SearchResultRendererTest {

	/** How KnowWE masks: every {@code >} and {@code <} becomes its own token, index 5 and 6. */
	private static final String GT = "@@k1_5@@";
	private static final String LT = "@@k1_6@@";
	private static final String BR = LT + "br/" + GT;

	@Test
	public void aNewlineBetweenTwoTagsBecomesABreak() {
		assertEquals(GT + BR + LT, SearchResultRenderer.joinRenderedLines(GT + "\n" + LT));
	}

	@Test
	public void indentationAroundSuchANewlineGoesAway() {
		assertEquals(GT + BR + LT, SearchResultRenderer.joinRenderedLines(GT + "   \n   " + LT));
	}

	@Test
	public void everyLineOfARuleBlockIsJoined() {
		String block = GT + "\n" + LT + "span" + GT + "\n" + LT;
		assertEquals(2, countBreaks(SearchResultRenderer.joinRenderedLines(block)));
	}

	@Test
	public void aNewlineInsideRunningTextIsLeftAlone() {
		// this is what keeps prose previews their paragraphs
		String prose = GT + "Der Steckverbinder\nwird geprueft." + LT;
		assertEquals(prose, SearchResultRenderer.joinRenderedLines(prose));
	}

	@Test
	public void textNextToATagIsLeftAlone() {
		assertEquals(GT + "Text\n" + LT, SearchResultRenderer.joinRenderedLines(GT + "Text\n" + LT));
		assertEquals(GT + "\nText" + LT, SearchResultRenderer.joinRenderedLines(GT + "\nText" + LT));
	}

	@Test
	public void unmaskedMarkupIsNotTouched() {
		// wiki syntax must still reach the parser, only rendered HTML is joined
		String wiki = "!! Heading\n\nEin Absatz.\n";
		assertEquals(wiki, SearchResultRenderer.joinRenderedLines(wiki));
	}

	private static int countBreaks(String html) {
		return html.split(java.util.regex.Pattern.quote(BR), -1).length - 1;
	}
}
