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

import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.pattern.PatternCaptureGroupTokenFilter;

/**
 * The analyzer for the markup vocabulary: {@code %%Question}, {@code @file} and friends.
 * <p>
 * It exists because {@link org.apache.lucene.analysis.standard.StandardTokenizer} throws {@code %} and {@code @} away,
 * which would make it impossible to search for a markup by the name a user actually sees. Here the sigil survives
 * tokenization, and {@link PatternCaptureGroupTokenFilter} then emits the bare name at the same position, so
 * <b>{@code %%Question} and plain {@code Question} both find the block</b> — which is exactly what was asked for.
 * <p>
 * The same chain runs at index and at query time, so whichever way the user spells it, the terms line up. The low
 * weight of markup hits comes from the query boost, not from this analyzer.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class MarkupTokenAnalyzer extends Analyzer {

	/** Captures the name behind any number of leading sigils: {@code %%question} also yields {@code question}. */
	private static final Pattern WITHOUT_SIGIL = Pattern.compile("^[%@]+(.+)$");

	private static final int DELIMITER_FLAGS =
			WordDelimiterGraphFilter.GENERATE_WORD_PARTS
			| WordDelimiterGraphFilter.SPLIT_ON_CASE_CHANGE
			| WordDelimiterGraphFilter.PRESERVE_ORIGINAL;

	private final boolean indexTime;

	public MarkupTokenAnalyzer(boolean indexTime) {
		this.indexTime = indexTime;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new WhitespaceTokenizer();
		TokenStream stream = new PatternCaptureGroupTokenFilter(source, true, WITHOUT_SIGIL);
		// so that %%KnowledgeBase is also found by "knowledge base" -- must run before lower casing,
		// because SPLIT_ON_CASE_CHANGE needs the case that LowerCaseFilter would already have removed
		stream = new WordDelimiterGraphFilter(stream, DELIMITER_FLAGS, null);
		if (indexTime) stream = new FlattenGraphFilter(stream);
		stream = new LowerCaseFilter(stream);
		stream = new RemoveDuplicatesTokenFilter(stream);
		return new TokenStreamComponents(source, stream);
	}

	@Override
	protected TokenStream normalize(String fieldName, TokenStream in) {
		return new LowerCaseFilter(in);
	}
}
