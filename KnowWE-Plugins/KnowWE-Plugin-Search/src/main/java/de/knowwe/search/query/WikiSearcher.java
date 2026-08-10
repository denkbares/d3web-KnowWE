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

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.QueryRescorer;
import org.apache.lucene.search.TopScoreDocCollectorManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.uhighlight.DefaultPassageFormatter;
import org.apache.lucene.search.uhighlight.LengthGoalBreakIterator;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;

import de.knowwe.search.analysis.WikiAnalyzers;
import de.knowwe.search.index.SearchFields;
import de.knowwe.search.index.SectionAnchor;
import de.knowwe.search.index.WikiSearchIndex;

/**
 * Runs a search against the index and turns the raw hits into what a result list needs.
 * <p>
 * Two behaviours live here rather than in the query builder, because both need to see the outcome of a search:
 * highlighting, which needs the matching documents, and relaxation, which needs to know that the strict interpretation
 * found nothing.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class WikiSearcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikiSearcher.class);

	/**
	 * Up to here the number of hits is exact, beyond it the answer says "100+".
	 * <p>
	 * The point is not the number, it is what the number costs: while Lucene has to count exactly it may not skip any
	 * match, however hopeless its score.
	 */
	private static final int COUNT_EXACTLY_UP_TO = 100;

	/** What the proximity rescoring is worth against the original score. */
	private static final double PROXIMITY_WEIGHT = 1.0;

	/** How many hits an explanation covers -- enough to see why the top few are in that order. */
	private static final int EXPLAINED_HITS = 5;

	/** How many passages of the section to stitch together into a snippet. */
	private static final int SNIPPET_PASSAGES = 3;
	/**
	 * Characters a single passage should aim for. Without this the highlighter uses whole sentences, and wiki text
	 * full of markup and lists often has no sentence end for a screenful, so one "passage" swallows the entire block.
	 */
	private static final int PASSAGE_LENGTH = 120;
	private static final int SNIPPET_LENGTH = 240;

	private final WikiSearchIndex index;
	private final WikiQueryBuilder queryBuilder;

	public WikiSearcher(@NotNull WikiSearchIndex index) {
		this(index, new WikiQueryBuilder());
	}

	public WikiSearcher(@NotNull WikiSearchIndex index, @NotNull WikiQueryBuilder queryBuilder) {
		this.index = index;
		this.queryBuilder = queryBuilder;
	}

	public @NotNull SearchResults search(@NotNull SearchRequest request) throws IOException {
		long start = System.nanoTime();
		if (request.isBlank()) return SearchResults.empty(0);

		SearchResults results = run(request, false, start);
		if (!results.isEmpty()) return results;

		// Asking for every word and getting nothing is the worst possible answer, and it is what the strict
		// interpretation produces as soon as one word of a short query is missing from the wiki. Try again with
		// every word optional, and say so.
		SearchResults relaxed = run(request, true, start);
		if (relaxed.isEmpty()) {
			return new SearchResults(List.of(), 0, true, millisSince(start), false, unmatchedWords(request));
		}
		return relaxed;
	}

	private SearchResults run(SearchRequest request, boolean relaxed, long start) throws IOException {
		Query query = relaxed
				? queryBuilder.buildRelaxed(request)
				: queryBuilder.build(request);

		return index.search(searcher -> {
			int window = Math.max(1, request.offset() + request.limit());
			// Counting exactly is what forbids Lucene to skip: below this threshold it must visit every match, above it
			// it may drop whole blocks that cannot reach the top (block-max WAND). We do not need an exact count -- the
			// answer says "100+" and nobody pages that far.
			TopDocs topDocs = searcher.search(query,
					new TopScoreDocCollectorManager(window, null, COUNT_EXACTLY_UP_TO));
			topDocs = closeTogetherFirst(searcher, query, topDocs, request, window);
			explain(searcher, query, topDocs, request);
			List<SearchHit> hits = collect(searcher, query, topDocs, request);
			return new SearchResults(hits, topDocs.totalHits.value(),
					topDocs.totalHits.relation() == TotalHits.Relation.EQUAL_TO, millisSince(start),
					relaxed && !hits.isEmpty(), List.of());
		});
	}

	/**
	 * Adds what the words standing close together in the text are worth -- but only for the hits already found.
	 * <p>
	 * As a clause in the query this would have to walk position lists across every document in the index; measured in a
	 * wiki of 96.000 sections that was the one change that made the search noticeably slower. Rescoring asks the same
	 * question of a few hundred documents instead, so its cost hangs on the window and not on the wiki.
	 */
	private TopDocs closeTogetherFirst(IndexSearcher searcher, Query query, TopDocs topDocs,
									   SearchRequest request, int window) throws IOException {
		if (request.titleOnly() || topDocs.scoreDocs.length == 0) return topDocs;
		Query near = queryBuilder.nearInBody(request);
		if (near == null) return topDocs;
		return QueryRescorer.rescore(searcher, topDocs, near, PROXIMITY_WEIGHT, window);
	}

	/**
	 * Writes Lucene's own account of the top scores into the log, if asked for with
	 * {@code -Dknowwe.search.explain=true}.
	 * <p>
	 * There is no other way to answer "why is this hit above that one": the score is a sum over fields, boosts, term
	 * frequencies and document lengths, and guessing at it from the outside is how one ends up believing the wrong
	 * cause. Off by default -- an explanation costs about as much as the search itself.
	 */
	private static void explain(IndexSearcher searcher, Query query, TopDocs topDocs, SearchRequest request) {
		if (!Boolean.getBoolean("knowwe.search.explain")) return;
		try {
			LOGGER.info("Scores for \"{}\":", request.query());
			for (int i = 0; i < Math.min(EXPLAINED_HITS, topDocs.scoreDocs.length); i++) {
				ScoreDoc hit = topDocs.scoreDocs[i];
				String title = searcher.storedFields().document(hit.doc).get(SearchFields.BREADCRUMB);
				LOGGER.info("  {}. {} — {}\n{}", i + 1, hit.score, title,
						searcher.explain(query, hit.doc));
			}
		}
		catch (IOException | RuntimeException e) {
			LOGGER.warn("Could not explain the scores", e);
		}
	}

	private List<SearchHit> collect(IndexSearcher searcher, Query query, TopDocs topDocs, SearchRequest request)
			throws IOException {
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		if (scoreDocs.length <= request.offset()) return List.of();

		String[] snippets = highlight(searcher, query, topDocs);
		StoredFields stored = searcher.storedFields();

		List<SearchHit> hits = new ArrayList<>();
		for (int i = request.offset(); i < scoreDocs.length; i++) {
			Document document = stored.document(scoreDocs[i].doc);
			String title = document.get(SearchFields.TITLE);
			String breadcrumb = document.get(SearchFields.BREADCRUMB);
			String snippet = snippets != null && i < snippets.length && snippets[i] != null
					? snippets[i]
					: shorten(document.get(SearchFields.BODY));
			boolean attachment = SearchFields.TYPE_ATTACHMENT.equals(document.get(SearchFields.TYPE));
			hits.add(new SearchHit(title, breadcrumb, snippet,
					new SectionAnchor(title,
							document.get(SearchFields.SECTION_ID),
							document.get(SearchFields.SECTION_PATH),
							headingOf(breadcrumb)),
					scoreDocs[i].score, attachment));
		}
		return hits;
	}

	/**
	 * Snippets come from the stored body with postings offsets, so the highlighter does not have to re-analyse the
	 * whole text of every hit the way JSPWiki's does.
	 */
	private String[] highlight(IndexSearcher searcher, Query query, TopDocs topDocs) {
		try {
			UnifiedHighlighter highlighter = UnifiedHighlighter
					.builder(searcher, WikiAnalyzers.forQuerying())
					// escape=true is not optional: the body is wiki text, and without escaping anything a page
					// contains would be injected into the result list as markup
					.withFormatter(new DefaultPassageFormatter("<mark>", "</mark>", " … ", true))
					// a line iterator as the base, not a sentence one: LengthGoalBreakIterator only accumulates
					// whole segments of its base, and a "sentence" of wiki text without punctuation can be the
					// entire block -- which is exactly how a snippet ends up two thousand characters long
					.withBreakIterator(() -> LengthGoalBreakIterator.createClosestToLength(
							BreakIterator.getLineInstance(Locale.GERMAN), PASSAGE_LENGTH, 0.4f))
					.build();
			Map<String, String[]> fields = highlighter.highlightFields(
					new String[]{ SearchFields.BODY }, query, topDocs, new int[]{ SNIPPET_PASSAGES });
			return fields.get(SearchFields.BODY);
		}
		catch (IOException | RuntimeException e) {
			// a snippet is a nicety; never let it cost the result
			return null;
		}
	}

	/** The words of the query that occur nowhere, so an empty result can explain itself. */
	private List<String> unmatchedWords(SearchRequest request) throws IOException {
		Set<String> unmatched = new LinkedHashSet<>();
		List<List<String>> positions =
				QueryTokens.byPosition(WikiAnalyzers.forQuerying(), SearchFields.BODY, request.query());
		index.search(searcher -> {
			for (List<String> alternatives : positions) {
				boolean found = false;
				for (String term : alternatives) {
					for (String field : new String[]{
							SearchFields.BODY, SearchFields.TITLE_TEXT, SearchFields.HEADING, SearchFields.MARKUP }) {
						if (searcher.getIndexReader().docFreq(new Term(field, term)) > 0) {
							found = true;
							break;
						}
					}
					if (found) break;
				}
				if (!found && !alternatives.isEmpty()) unmatched.add(alternatives.get(0));
			}
			return null;
		});
		return List.copyOf(unmatched);
	}

	private static String headingOf(String breadcrumb) {
		int last = breadcrumb.lastIndexOf(" › ");
		return last < 0 ? null : breadcrumb.substring(last + 3);
	}

	/**
	 * The fallback when highlighting produced nothing. Escaped like the highlighted variant: both end up as HTML in
	 * the result list, and the body is wiki text that may contain anything.
	 */
	private static String shorten(String body) {
		if (body == null) return "";
		String single = body.replace('\n', ' ').trim();
		String cut = single.length() <= SNIPPET_LENGTH ? single : single.substring(0, SNIPPET_LENGTH) + " …";
		return cut.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	private static long millisSince(long startNanos) {
		return (System.nanoTime() - startNanos) / 1_000_000;
	}
}
