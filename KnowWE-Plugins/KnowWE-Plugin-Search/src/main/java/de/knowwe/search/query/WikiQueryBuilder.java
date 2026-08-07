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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchNoDocsQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.QueryBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.util.lucene.DisjunctionMaxRewrite;
import de.knowwe.search.analysis.WikiAnalyzers;
import de.knowwe.search.index.SearchFields;

/**
 * Builds the Lucene query from what the user typed.
 * <p>
 * Hand built rather than handed to a {@code QueryParser}. {@code MultiFieldQueryParser}, which JSPWiki uses, throws
 * {@code ParseException} on an unbalanced quote or a stray colon, cannot express per field boosts, and its implicit OR
 * puts a document matching one of five words next to one matching all five. All three are visible as bad results.
 * <p>
 * The shape is a disjunction maximum per <b>position</b>: a word scores by its best field rather than by the sum over
 * all of them, which keeps a page whose title matches from being buried under one that merely repeats the word in its
 * body. Positions rather than tokens, because the analyzers deliberately emit alternatives at the same position; see
 * {@link QueryTokens}.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class WikiQueryBuilder {

	private static final float BOOST_TITLE = 8f;
	private static final float BOOST_HEADING = 5f;
	private static final float BOOST_BREADCRUMB = 2f;
	private static final float BOOST_BODY = 1f;
	private static final float BOOST_FUZZY = 0.2f;
	private static final float BOOST_PREFIX_TITLE = 4f;
	private static final float BOOST_PREFIX_HEADING = 3f;
	private static final float BOOST_PREFIX_BODY = 0.5f;

	/** Markup names are searchable but must not drown out prose. */
	private static final float BOOST_MARKUP = 0.3f;
	/**
	 * Unless the user spelled out the sigil. Typing {@code %%Question} is a deliberate request for the markup, not for
	 * pages that happen to contain the word.
	 */
	private static final float BOOST_MARKUP_EXPLICIT = 6f;

	/** Below this length an edit distance of one matches almost anything. */
	private static final int MIN_FUZZY_LENGTH = 4;
	/** Shorter prefixes expand to too much of the index to be useful. */
	private static final int MIN_PREFIX_LENGTH = 2;
	private static final float TIE_BREAKER = 0.1f;

	private static final Pattern PHRASE = Pattern.compile("\"([^\"]+)\"");
	private static final Pattern EXPLICIT_MARKUP = Pattern.compile("(^|\\s)[%@]+\\S");

	public @NotNull Query build(@NotNull SearchRequest request) {
		if (request.isBlank()) return new MatchNoDocsQuery();

		List<String> phrases = new ArrayList<>();
		String freeText = extractPhrases(request.query(), phrases);

		List<List<String>> positions =
				QueryTokens.byPosition(WikiAnalyzers.forQuerying(), SearchFields.BODY, freeText);

		if (positions.isEmpty() && phrases.isEmpty()) return new MatchNoDocsQuery();

		BooleanQuery.Builder query = new BooleanQuery.Builder();
		if (!positions.isEmpty()) {
			query.add(scorePositions(positions, request.partial()), BooleanClause.Occur.MUST);
		}
		Query explicitMarkup = explicitMarkup(freeText, request.query());
		if (explicitMarkup != null) {
			query.add(explicitMarkup, BooleanClause.Occur.SHOULD);
		}
		for (String phrase : phrases) {
			query.add(phrase(phrase), BooleanClause.Occur.MUST);
		}
		return query.build();
	}

	/**
	 * Every word must contribute, but a long query may miss one without collapsing to nothing — that is what turns a
	 * five word question from zero hits into useful ones.
	 */
	private Query scorePositions(List<List<String>> positions, boolean partial) {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		for (int i = 0; i < positions.size(); i++) {
			boolean last = i == positions.size() - 1;
			builder.add(scorePosition(positions.get(i), partial && last), BooleanClause.Occur.SHOULD);
		}
		builder.setMinimumNumberShouldMatch(minimumShouldMatch(positions.size()));
		return builder.build();
	}

	static int minimumShouldMatch(int wordCount) {
		if (wordCount <= 3) return wordCount;
		return wordCount - 1;
	}

	/** One clause per word, matching whichever of its forms and fields scores best. */
	private Query scorePosition(List<String> alternatives, boolean asPrefix) {
		List<Query> options = new ArrayList<>();
		for (String term : alternatives) {
			options.add(boosted(new TermQuery(new Term(SearchFields.TITLE_TEXT, term)), BOOST_TITLE));
			options.add(boosted(new TermQuery(new Term(SearchFields.HEADING, term)), BOOST_HEADING));
			options.add(boosted(new TermQuery(new Term(SearchFields.BREADCRUMB, term)), BOOST_BREADCRUMB));
			options.add(boosted(new TermQuery(new Term(SearchFields.BODY, term)), BOOST_BODY));
			// inside the disjunction, not as a mere bonus: a block whose only match is its markup name must still
			// be able to satisfy the query
			options.add(boosted(new TermQuery(new Term(SearchFields.MARKUP, term)), BOOST_MARKUP));

			if (term.length() >= MIN_FUZZY_LENGTH) {
				// the rewrite is a constructor argument since lucene 10; DisjunctionMaxRewrite because otherwise the
				// scores of several near matches add up and beat the exact hit
				options.add(boosted(new FuzzyQuery(new Term(SearchFields.BODY, term), 1, MIN_PREFIX_LENGTH,
						FuzzyQuery.defaultMaxExpansions, true, DisjunctionMaxRewrite.INSTANCE), BOOST_FUZZY));
			}
			if (asPrefix && term.length() >= MIN_PREFIX_LENGTH) {
				options.add(boosted(new TermQuery(new Term(SearchFields.TITLE_GRAM, term)), BOOST_PREFIX_TITLE));
				options.add(boosted(new TermQuery(new Term(SearchFields.HEADING_GRAM, term)), BOOST_PREFIX_HEADING));
				// bounded on purpose: user input must never expand without a limit over a large index
				options.add(boosted(new PrefixQuery(new Term(SearchFields.BODY, term),
						MultiTermQuery.CONSTANT_SCORE_BLENDED_REWRITE), BOOST_PREFIX_BODY));
			}
		}
		return new DisjunctionMaxQuery(options, TIE_BREAKER);
	}

	/**
	 * An extra, strongly boosted clause for markup spelled with its sigil, so {@code %%Question} puts the question
	 * markups first rather than every page that mentions the word.
	 *
	 * @return null when the query contains no sigil
	 */
	private @Nullable Query explicitMarkup(String freeText, String rawQuery) {
		if (!EXPLICIT_MARKUP.matcher(rawQuery).find()) return null;

		Set<String> sigilTerms = new LinkedHashSet<>();
		for (List<String> alternatives : QueryTokens.byPosition(
				WikiAnalyzers.forQuerying(), SearchFields.MARKUP, freeText)) {
			for (String term : alternatives) {
				if (term.startsWith("%") || term.startsWith("@")) sigilTerms.add(term);
			}
		}
		if (sigilTerms.isEmpty()) return null;

		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		for (String term : sigilTerms) {
			builder.add(new TermQuery(new Term(SearchFields.MARKUP, term)), BooleanClause.Occur.SHOULD);
		}
		builder.setMinimumNumberShouldMatch(1);
		return boosted(builder.build(), BOOST_MARKUP_EXPLICIT);
	}

	/**
	 * Built by Lucene's own {@link QueryBuilder}, which lays the terms out by position and produces a multi phrase
	 * query where the analyzer offered alternatives. Assembling a phrase from a flat token list cannot match.
	 */
	private Query phrase(String phrase) {
		Query query = new QueryBuilder(WikiAnalyzers.forQuerying()).createPhraseQuery(SearchFields.BODY, phrase);
		return query == null ? new MatchNoDocsQuery() : query;
	}

	/** Pulls quoted phrases out of the query and returns what is left as free text. */
	private static String extractPhrases(String query, List<String> phrases) {
		Matcher matcher = PHRASE.matcher(query);
		StringBuilder rest = new StringBuilder();
		int last = 0;
		while (matcher.find()) {
			phrases.add(matcher.group(1));
			rest.append(query, last, matcher.start()).append(' ');
			last = matcher.end();
		}
		rest.append(query.substring(last));
		return rest.toString();
	}

	private static Query boosted(Query query, float boost) {
		return boost == 1f ? query : new BoostQuery(query, boost);
	}
}
