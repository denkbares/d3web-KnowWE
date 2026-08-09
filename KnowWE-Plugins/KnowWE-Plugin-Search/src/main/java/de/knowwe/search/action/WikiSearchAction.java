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

package de.knowwe.search.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.search.WikiSearchService;
import de.knowwe.search.query.HitGrouping;
import de.knowwe.search.query.SearchHit;
import de.knowwe.search.query.SearchRequest;
import de.knowwe.search.query.SearchResults;
import de.knowwe.search.query.WikiSearcher;
import de.knowwe.search.render.SearchResultRenderer;

/**
 * The JSON endpoint of the section search, at {@code /action/WikiSearchAction}.
 * <p>
 * Answers with everything a result list needs and nothing it has to fetch again: breadcrumb, highlighted snippet, the
 * link to the section, and the anchors that let a following request render the section's preview.
 * <p>
 * Results are filtered by read permission before they leave the server. That is the second line of defence; the
 * intended one is a filter clause inside the query, which follows with the ACL fields.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude for wiki-search
 */
public class WikiSearchAction extends AbstractAction {

	public static final String PARAM_QUERY = "query";
	public static final String PARAM_PARTIAL = "partial";
	public static final String PARAM_OFFSET = "offset";
	public static final String PARAM_LIMIT = "limit";
	/** The quick search switches previews off; a rendered section would not fit into a dropdown. */
	public static final String PARAM_PREVIEW = "preview";
	/** Answers with the hits folded away under one page instead of a result list -- used when unfolding them. */
	public static final String PARAM_EXPAND = "expand";

	private static final int MAX_LIMIT = 50;

	/**
	 * How many hits to look at to build one page of entries. Folding needs the weaker hits of a page even when they
	 * rank far below the entry they belong to, so a page of entries cannot be read off the top of the result list.
	 * Beyond this window a page's counter can undercount; the alternative would be scanning the whole result set on
	 * every keystroke.
	 */
	private static final int SCAN_FACTOR = 8;
	private static final int MAX_SCAN = 300;

	private final SearchResultRenderer renderer = new SearchResultRenderer();

	@Override
	public void execute(UserActionContext context) throws IOException {
		WikiSearchService service = WikiSearchService.getInstance();
		WikiSearcher searcher = service.getSearcher();

		JSONObject answer = new JSONObject();
		answer.put("indexing", service.isBuilding());

		if (searcher == null) {
			// the index could not be opened; say so rather than pretending there are no hits
			answer.put("error", "search index unavailable");
			write(context, answer);
			return;
		}

		String expand = context.getParameter(PARAM_EXPAND);
		int offset = parseInt(context.getParameter(PARAM_OFFSET), 0);
		int limit = Math.min(parseInt(context.getParameter(PARAM_LIMIT), SearchRequest.DEFAULT_LIMIT), MAX_LIMIT);

		// grouping needs a window rather than just this page's worth of hits, see SCAN_FACTOR
		SearchRequest request = new SearchRequest(
				context.getParameter(PARAM_QUERY, ""),
				Boolean.parseBoolean(context.getParameter(PARAM_PARTIAL, "false")),
				0, Math.min((offset + limit) * SCAN_FACTOR, MAX_SCAN));

		SearchResults results = searcher.search(request);

		answer.put("query", request.query());
		answer.put("total", results.total());
		answer.put("exact", results.exact());
		answer.put("tookMs", results.tookMs());
		answer.put("relaxed", results.relaxed());
		answer.put("unmatched", new JSONArray(results.unmatched()));

		List<SearchHit> readable = new ArrayList<>();
		for (SearchHit hit : results.hits()) {
			Article article = context.getArticleManager().getArticle(hit.title());
			// a hit whose page vanished or may not be read must not leave the server
			if (article == null || !KnowWEUtils.canView(article, context)) continue;
			readable.add(hit);
		}
		List<HitGrouping.Group> groups = HitGrouping.group(readable);

		boolean withPreview = !"false".equals(context.getParameter(PARAM_PREVIEW, "true"));
		JSONArray hits = new JSONArray();

		if (expand != null) {
			// the same grouping the entry was built from, so the unfolded hits are exactly the ones it counted
			for (HitGrouping.Group group : groups) {
				if (!group.primary().title().equalsIgnoreCase(expand)) continue;
				for (SearchHit folded : group.folded()) {
					hits.put(toJson(folded, withPreview, context));
				}
			}
			answer.put("hits", hits);
			write(context, answer);
			return;
		}

		for (int i = offset; i < Math.min(offset + limit, groups.size()); i++) {
			HitGrouping.Group group = groups.get(i);
			JSONObject json = toJson(group.primary(), withPreview, context);
			if (!group.folded().isEmpty()) json.put("folded", group.folded().size());
			hits.put(json);
		}
		answer.put("hasMore", groups.size() > offset + limit);
		answer.put("hits", hits);

		write(context, answer);
	}

	private JSONObject toJson(SearchHit hit, boolean withPreview, UserActionContext context) {
		JSONObject json = toJson(hit);
		// only for the hits actually shown: rendering costs a wiki-syntax pass each
		if (withPreview) {
			SearchResultRenderer.Rendered rendered = renderer.render(hit.anchor(), context);
			if (rendered != null) {
				json.put("previewHtml", rendered.html());
				if (rendered.stale()) json.put("stale", true);
			}
		}
		return json;
	}

	private static JSONObject toJson(SearchHit hit) {
		JSONObject json = new JSONObject();
		json.put("page", hit.title());
		json.put("title", hit.title());
		json.put("breadcrumb", hit.breadcrumb());
		json.put("snippet", hit.snippet());
		json.put("score", hit.score());
		if (hit.heading() != null) json.put("heading", hit.heading());
		json.put("sectionId", hit.anchor().sectionId() == null ? "" : hit.anchor().sectionId());
		json.put("sectionPath", hit.anchor().sectionPath() == null ? "" : hit.anchor().sectionPath());
		json.put("url", "Wiki.jsp?page=" + hit.title().replace(" ", "+")
						+ (hit.anchor().sectionId() == null ? "" : "#" + hit.anchor().sectionId()));
		return json;
	}

	private static void write(UserActionContext context, JSONObject answer) throws IOException {
		context.setContentType(JSON);
		answer.write(context.getWriter());
	}

	private static int parseInt(String value, int fallback) {
		if (value == null) return fallback;
		try {
			return Math.max(0, Integer.parseInt(value.trim()));
		}
		catch (NumberFormatException e) {
			return fallback;
		}
	}
}
