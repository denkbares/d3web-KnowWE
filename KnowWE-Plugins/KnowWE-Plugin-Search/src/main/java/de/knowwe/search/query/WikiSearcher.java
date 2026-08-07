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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.uhighlight.DefaultPassageFormatter;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
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

	/** How many passages of the section to stitch together into a snippet. */
	private static final int SNIPPET_PASSAGES = 2;
	private static final int SNIPPET_LENGTH = 160;

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
			TopDocs topDocs = searcher.search(query, window);
			List<SearchHit> hits = collect(searcher, query, topDocs, request);
			return new SearchResults(hits, topDocs.totalHits.value(),
					topDocs.totalHits.relation() == TotalHits.Relation.EQUAL_TO, millisSince(start),
					relaxed && !hits.isEmpty(), List.of());
		});
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
			hits.add(new SearchHit(title, breadcrumb, snippet,
					new SectionAnchor(title,
							document.get(SearchFields.SECTION_ID),
							document.get(SearchFields.SECTION_PATH),
							headingOf(breadcrumb)),
					scoreDocs[i].score));
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
