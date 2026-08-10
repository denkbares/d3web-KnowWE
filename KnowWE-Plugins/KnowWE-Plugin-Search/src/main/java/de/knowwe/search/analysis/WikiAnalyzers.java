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

import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.jetbrains.annotations.NotNull;

import de.knowwe.search.index.SearchFields;

/**
 * Assembles the per field analyzers for the section index.
 * <p>
 * Index and query side differ in two places, and both differences matter: the token graph may only be flattened while
 * indexing, and the edge-grammed fields are grammed on the way in but not on the way out — otherwise a typed prefix
 * would be chopped up again instead of being matched against the stored grams.
 * <p>
 * Bump {@link SearchFields#SCHEMA_VERSION} whenever anything here changes, or existing indexes will keep answering with
 * terms that no query can produce any more.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public final class WikiAnalyzers {

	private static final Analyzer INDEX = build(true);
	private static final Analyzer QUERY = build(false);

	/** Analyzer for writing documents. */
	public static @NotNull Analyzer forIndexing() {
		return INDEX;
	}

	/** Analyzer for turning user input into terms. */
	public static @NotNull Analyzer forQuerying() {
		return QUERY;
	}

	private static Analyzer build(boolean indexTime) {
		Analyzer text = new WikiTextAnalyzer(indexTime);
		Analyzer markup = new MarkupTokenAnalyzer(indexTime);
		// grammed on the way in, plain on the way out
		Analyzer gram = indexTime ? new EdgeGramAnalyzer() : new WikiTextAnalyzer(false);
		return new PerFieldAnalyzerWrapper(text, Map.of(
				SearchFields.MARKUP, markup,
				SearchFields.TITLE_GRAM, gram,
				SearchFields.HEADING_GRAM, gram));
	}

	private WikiAnalyzers() {
	}
}
