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

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.jetbrains.annotations.NotNull;

/**
 * Tokenizes a query <b>grouped by position</b>.
 * <p>
 * This distinction is not cosmetic. The analyzers emit several tokens at the same position on purpose: the unstemmed
 * form next to the stemmed one, the whole identifier next to its parts. A flat token list makes each of them look like
 * a separate word the user typed, which quietly breaks three things at once — a one word query demands two matches, a
 * prefix ends up on the stemmed variant instead of what was typed, and a three word phrase turns into a five term
 * phrase that can never match.
 * <p>
 * {@code LuceneUtils.tokenize} returns the flat list, so this exists next to it rather than reusing it.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
final class QueryTokens {

	/**
	 * @return one entry per position, each holding the alternatives at that position, in order
	 */
	static @NotNull List<List<String>> byPosition(@NotNull Analyzer analyzer, @NotNull String field,
												  @NotNull String text) {
		List<List<String>> positions = new ArrayList<>();
		try (TokenStream stream = analyzer.tokenStream(field, text)) {
			CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
			PositionIncrementAttribute increment = stream.addAttribute(PositionIncrementAttribute.class);
			stream.reset();
			while (stream.incrementToken()) {
				if (increment.getPositionIncrement() > 0 || positions.isEmpty()) {
					positions.add(new ArrayList<>(2));
				}
				List<String> alternatives = positions.get(positions.size() - 1);
				String value = term.toString();
				if (!alternatives.contains(value)) alternatives.add(value);
			}
			stream.end();
		}
		catch (IOException e) {
			// analysing a string in memory cannot fail unless an analyzer is misconfigured
			throw new IOError(e);
		}
		return positions;
	}

	private QueryTokens() {
	}
}
