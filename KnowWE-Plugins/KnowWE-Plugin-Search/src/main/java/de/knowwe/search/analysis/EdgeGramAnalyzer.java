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
import org.apache.lucene.analysis.de.GermanNormalizationFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Index side analyzer for the as-you-type fields, so that typing {@code steckver} already finds
 * <i>Steckverbinder</i> without a wildcard query.
 * <p>
 * Applied <b>only to titles and headings</b>. Edge-gramming section bodies would multiply the index for half a million
 * documents, and the body does not need prefix matching: a prefix query on {@code body} covers that case at query time
 * with a bounded rewrite.
 * <p>
 * No stemming here — a stem of a prefix is meaningless — and never used on the query side: the query is analysed with
 * {@link WikiTextAnalyzer}, so the typed prefix is matched against the stored grams rather than being grammed itself.
 * <p>
 * If German compounds turn out to need <i>infix</i> matching too ({@code mitte} finding <i>Bauteilmitte</i>), the
 * escalation is a second field on {@code LuceneUtils.TermAnalyzer.ngram_faster}, which this codebase has already tuned.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class EdgeGramAnalyzer extends Analyzer {

	/** One character is enough to start suggesting; beyond twenty the prefix is no longer a prefix. */
	private static final int MIN_GRAM = 1;
	private static final int MAX_GRAM = 20;

	private static final int DELIMITER_FLAGS =
			WordDelimiterGraphFilter.GENERATE_WORD_PARTS
			| WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS
			| WordDelimiterGraphFilter.SPLIT_ON_CASE_CHANGE
			| WordDelimiterGraphFilter.CATENATE_WORDS
			| WordDelimiterGraphFilter.PRESERVE_ORIGINAL;

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new StandardTokenizer();
		TokenStream stream = new WordDelimiterGraphFilter(source, DELIMITER_FLAGS, null);
		stream = new FlattenGraphFilter(stream);
		stream = new LowerCaseFilter(stream);
		stream = new GermanNormalizationFilter(stream);
		stream = new ASCIIFoldingFilter(stream, false);
		stream = new EdgeNGramTokenFilter(stream, MIN_GRAM, MAX_GRAM, true);
		stream = new RemoveDuplicatesTokenFilter(stream);
		return new TokenStreamComponents(source, stream);
	}

	@Override
	protected TokenStream normalize(String fieldName, TokenStream in) {
		TokenStream stream = new LowerCaseFilter(in);
		stream = new GermanNormalizationFilter(stream);
		return new ASCIIFoldingFilter(stream, false);
	}
}
