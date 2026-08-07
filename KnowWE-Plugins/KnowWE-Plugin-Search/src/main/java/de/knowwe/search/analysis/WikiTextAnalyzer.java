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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.de.GermanLightStemFilter;
import org.apache.lucene.analysis.de.GermanNormalizationFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.KeywordRepeatFilter;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * The analyzer for readable wiki text: page titles, headings, breadcrumbs and section bodies.
 * <p>
 * What it buys over JSPWiki's {@code ClassicAnalyzer}:
 * <ul>
 * <li><b>Compound identifiers split.</b> {@code getPageName} also matches {@code page} and {@code name}, {@code ISO9001}
 * also matches {@code ISO} and {@code 9001}. This is what mkdocs' custom tokenizer does, and in a wiki full of type and
 * markup names it is what makes half the queries work at all.</li>
 * <li><b>German handled.</b> Umlaut and sharp-s normalisation plus light stemming, so {@code Prüfung} matches
 * {@code pruefung} and {@code Steckverbinder} matches {@code Steckverbindern}.</li>
 * <li><b>Exact forms preserved.</b> {@link KeywordRepeatFilter} keeps the unstemmed token next to the stemmed one at the
 * same position, so an exact hit still outscores a stem-only hit.</li>
 * </ul>
 * Deliberately <b>no stop words</b>, following {@code LuceneUtils.TermAnalyzer.standard}: in a technical wiki a query
 * like {@code "die Prüfung"} or a markup named {@code %%Die} must not silently lose a term.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class WikiTextAnalyzer extends Analyzer {

	private static final int DELIMITER_FLAGS =
			WordDelimiterGraphFilter.GENERATE_WORD_PARTS
			| WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS
			| WordDelimiterGraphFilter.SPLIT_ON_CASE_CHANGE
			| WordDelimiterGraphFilter.SPLIT_ON_NUMERICS
			| WordDelimiterGraphFilter.CATENATE_WORDS
			| WordDelimiterGraphFilter.CATENATE_NUMBERS
			| WordDelimiterGraphFilter.PRESERVE_ORIGINAL;

	private final boolean indexTime;

	/**
	 * @param indexTime whether this instance analyses documents. Only then may the token graph be flattened; doing it
	 *                  at query time would drop the alternative paths that make a phrase query match.
	 */
	public WikiTextAnalyzer(boolean indexTime) {
		this.indexTime = indexTime;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new StandardTokenizer();
		TokenStream stream = new WordDelimiterGraphFilter(source, DELIMITER_FLAGS, null);
		if (indexTime) stream = new FlattenGraphFilter(stream);
		stream = new LowerCaseFilter(stream);
		stream = new GermanNormalizationFilter(stream);
		stream = new ASCIIFoldingFilter(stream, false);
		stream = new KeywordRepeatFilter(stream);
		stream = new GermanLightStemFilter(stream);
		stream = new RemoveDuplicatesTokenFilter(stream);
		return new TokenStreamComponents(source, stream);
	}

	/**
	 * Applied to the terms of range and prefix queries, which are not tokenized. Only the case and character folding
	 * steps make sense there, splitting and stemming do not.
	 */
	@Override
	protected TokenStream normalize(String fieldName, TokenStream in) {
		TokenStream stream = new LowerCaseFilter(in);
		stream = new GermanNormalizationFilter(stream);
		return new ASCIIFoldingFilter(stream, false);
	}
}
