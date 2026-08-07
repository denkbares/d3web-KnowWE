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

package de.knowwe.search.analysis;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

import de.knowwe.search.index.SearchFields;

import static org.junit.Assert.assertTrue;

/**
 * Asserts the properties the search depends on, at the level of the produced terms. A term stream test is the cheapest
 * place to catch an analyzer regression -- once it only shows up as "the search feels worse", it is far harder to pin
 * down.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class WikiAnalyzersTest {

	@Test
	public void camelCaseIsSplitAndTheOriginalIsKept() throws IOException {
		Set<String> terms = index(SearchFields.BODY, "getPageName");
		assertContains(terms, "get", "page", "name", "getpagename");
	}

	@Test
	public void lettersAndDigitsAreSplitApart() throws IOException {
		Set<String> terms = index(SearchFields.BODY, "ISO9001");
		assertContains(terms, "iso", "9001");
	}

	@Test
	public void germanUmlautsAndSharpSAreNormalised() throws IOException {
		Set<String> terms = index(SearchFields.BODY, "Prüfung Straße");
		Set<String> typedWithoutUmlauts = query(SearchFields.BODY, "pruefung strasse");
		assertTrue("a query typed without umlauts must reach the indexed terms: "
				   + terms + " vs " + typedWithoutUmlauts,
				typedWithoutUmlauts.stream().anyMatch(terms::contains));
	}

	@Test
	public void germanPluralStillMatchesTheSingular() throws IOException {
		Set<String> indexed = index(SearchFields.BODY, "Steckverbindern");
		Set<String> queried = query(SearchFields.BODY, "Steckverbinder");
		assertTrue("light stemming must bridge the inflection: " + indexed + " vs " + queried,
				queried.stream().anyMatch(indexed::contains));
	}

	@Test
	public void exactFormSurvivesStemming() throws IOException {
		// KeywordRepeatFilter keeps the unstemmed token, so an exact hit can still outscore a stem-only hit
		assertContains(index(SearchFields.BODY, "Leitungen"), "leitungen");
	}

	@Test
	public void noStopWordsAreRemoved() throws IOException {
		Set<String> terms = index(SearchFields.BODY, "die Pruefung und der Test");
		assertContains(terms, "die", "und", "der");
	}

	@Test
	public void markupIsFoundWithAndWithoutItsSigil() throws IOException {
		Set<String> indexed = index(SearchFields.MARKUP, "%%Question");

		assertContains(indexed, "%%question", "question");
		assertTrue("typing the markup with sigil must hit",
				query(SearchFields.MARKUP, "%%Question").stream().anyMatch(indexed::contains));
		assertTrue("typing the bare name must hit",
				query(SearchFields.MARKUP, "Question").stream().anyMatch(indexed::contains));
	}

	@Test
	public void annotationIsFoundWithAndWithoutItsSigil() throws IOException {
		Set<String> indexed = index(SearchFields.MARKUP, "@file");

		assertContains(indexed, "@file", "file");
		assertTrue("typing @file must hit",
				query(SearchFields.MARKUP, "@file").stream().anyMatch(indexed::contains));
		assertTrue("typing file must hit",
				query(SearchFields.MARKUP, "file").stream().anyMatch(indexed::contains));
	}

	@Test
	public void compoundMarkupNameIsAlsoFoundByItsWords() throws IOException {
		Set<String> indexed = index(SearchFields.MARKUP, "%%KnowledgeBase");
		assertContains(indexed, "%%knowledgebase", "knowledge", "base");
	}

	@Test
	public void titlesAreEdgeGrammedButQueriesAreNot() throws IOException {
		Set<String> indexed = index(SearchFields.TITLE_GRAM, "Steckverbinder");
		assertContains(indexed, "s", "ste", "steckver", "steckverbinder");

		Set<String> typed = query(SearchFields.TITLE_GRAM, "steckver");
		assertTrue("the typed prefix must stay whole so it can match a stored gram: " + typed,
				typed.contains("steckver"));
	}

	@Test
	public void bodyIsNotEdgeGrammed() throws IOException {
		// gramming half a million section bodies would multiply the index for no gain
		Set<String> terms = index(SearchFields.BODY, "Steckverbinder");
		assertTrue("body must not contain prefix grams, but was " + terms, terms.stream().noneMatch("ste"::equals));
	}

	private static Set<String> index(String field, String text) throws IOException {
		return terms(WikiAnalyzers.forIndexing(), field, text);
	}

	private static Set<String> query(String field, String text) throws IOException {
		return terms(WikiAnalyzers.forQuerying(), field, text);
	}

	private static Set<String> terms(Analyzer analyzer, String field, String text) throws IOException {
		Set<String> terms = new LinkedHashSet<>();
		try (TokenStream stream = analyzer.tokenStream(field, new StringReader(text))) {
			CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
			stream.reset();
			while (stream.incrementToken()) {
				terms.add(term.toString());
			}
			stream.end();
		}
		return terms;
	}

	private static void assertContains(Set<String> terms, String... expected) {
		for (String one : expected) {
			assertTrue("expected term '" + one + "' in " + terms, terms.contains(one));
		}
	}
}
