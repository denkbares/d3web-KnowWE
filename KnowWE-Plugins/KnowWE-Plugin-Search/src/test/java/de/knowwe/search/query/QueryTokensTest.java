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

package de.knowwe.search.query;

import java.util.List;

import org.junit.Test;

import de.knowwe.search.analysis.WikiAnalyzers;
import de.knowwe.search.index.SearchFields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Grouping by position is what keeps a one word query from demanding two matches. Worth its own test, because getting
 * it wrong shows up only as bad results, never as an exception.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class QueryTokensTest {

	@Test
	public void stemmedAndUnstemmedFormShareOnePosition() {
		List<List<String>> positions = body("Package");

		assertEquals("one word must stay one position", 1, positions.size());
		assertTrue("both forms belong to it: " + positions, positions.get(0).containsAll(List.of("package", "packag")));
	}

	@Test
	public void oneWordPerPositionForAPlainSentence() {
		assertEquals(3, body("Montage der Steckverbinder").size());
	}

	@Test
	public void theTypedFormComesFirstAtItsPosition() {
		// the prefix query must be built from what the user typed, not from its stem
		assertEquals("steckver", body("steckver").get(0).get(0));
	}

	@Test
	public void aSplitIdentifierYieldsSeveralPositions() {
		List<List<String>> positions = body("getPageName");
		assertTrue("the parts must occupy their own positions: " + positions, positions.size() > 1);
	}

	@Test
	public void markupKeepsSigilAndBareNameAtTheSamePosition() {
		List<List<String>> positions =
				QueryTokens.byPosition(WikiAnalyzers.forQuerying(), SearchFields.MARKUP, "%%Question");

		assertEquals(1, positions.size());
		assertTrue(positions.get(0).containsAll(List.of("%%question", "question")));
	}

	@Test
	public void blankInputYieldsNoPositions() {
		assertEquals(List.of(), body("   "));
	}

	private static List<List<String>> body(String text) {
		return QueryTokens.byPosition(WikiAnalyzers.forQuerying(), SearchFields.BODY, text);
	}
}
