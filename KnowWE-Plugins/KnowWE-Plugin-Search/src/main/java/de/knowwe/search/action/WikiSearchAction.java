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

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.GroupingCompiler;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.basicType.AttachmentCompileType;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
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
	/** Search page filters. The quick search sends none of them and gets everything, ordered. */
	public static final String PARAM_TITLE_ONLY = "titleOnly";
	public static final String PARAM_ATTACHMENTS_ONLY = "attachmentsOnly";
	public static final String PARAM_OTHER_VARIANTS = "otherVariants";
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
				0, Math.min((offset + limit) * SCAN_FACTOR, MAX_SCAN),
				Boolean.parseBoolean(context.getParameter(PARAM_TITLE_ONLY, "false")),
				Boolean.parseBoolean(context.getParameter(PARAM_ATTACHMENTS_ONLY, "false")));

		SearchResults results = searcher.search(request);

		answer.put("query", request.query());
		answer.put("total", results.total());
		answer.put("exact", results.exact());
		answer.put("tookMs", results.tookMs());
		answer.put("relaxed", results.relaxed());
		answer.put("unmatched", new JSONArray(results.unmatched()));
		// solange der Anhangs-Durchlauf noch laeuft, findet der Filter dafuer noch nicht alles -- das soll die
		// Oberflaeche sagen duerfen, statt eine leere Liste als Ergebnis auszugeben
		answer.put("attachmentsIndexed", service.isAttachmentsIndexed());
		answer.put("attachmentsIndexing", service.isAttachmentsIndexing());

		List<SearchHit> readable = new ArrayList<>();
		for (SearchHit hit : results.hits()) {
			Article article = context.getArticleManager().getArticle(hit.title());
			// a hit whose page vanished or may not be read must not leave the server
			if (article == null || !KnowWEUtils.canView(article, context)) continue;
			readable.add(hit);
		}
		// The quick search shows everything and only sorts; the search page has a switch and then really leaves it out.
		boolean otherVariants = !"false".equals(context.getParameter(PARAM_OTHER_VARIANTS, "true"));
		List<HitGrouping.Group> grouped = HitGrouping.group(readable);
		PackageCompiler mine = defaultCompiler(context);
		List<HitGrouping.Group> groups = otherVariants
				? byDefaultCompilerFirst(grouped, offset + limit, mine, context)
				: onlyDefaultCompiler(grouped, mine, context);

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
			// the page's further sections travel with it: they belong under this entry, not somewhere further down
			JSONArray sections = new JSONArray();
			for (SearchHit section : group.shown()) {
				sections.put(toJson(section, withPreview, context));
			}
			if (!sections.isEmpty()) json.put("sections", sections);
			if (!group.folded().isEmpty()) json.put("folded", group.folded().size());
			hits.put(json);
		}
		answer.put("hasMore", groups.size() > offset + limit);
		answer.put("hits", hits);

		write(context, answer);
	}

	private JSONObject toJson(SearchHit hit, boolean withPreview, UserActionContext context) {
		JSONObject json = toJson(hit);
		if (hit.attachment()) {
			// the wiki's own attachment view of the page -- Upload.jsp, what AttachmentTab.jsp links to as
			// context='upload'. It lists every attachment with its versions and a "click to view", which is more use
			// than handing over the bytes of one file.
			String view = "Upload.jsp?page=" + hit.title().replace(" ", "+");
			json.put("pageUrl", view);
			json.put("url", view);
			json.put("attachment", true);
			return json;
		}
		String rendering = renderingArticle(hit, context);
		// beides dorthin, wo der Inhalt tatsaechlich zu sehen ist -- der Abschnittsanker des Anhangs gilt dort nicht
		json.put("pageUrl", rendering != null ? rendering : "Wiki.jsp?page=" + hit.title().replace(" ", "+"));
		if (rendering != null) json.put("url", rendering);
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

	/**
	 * Puts what the current default compiler compiles first, the rest after it.
	 * <p>
	 * The wiki already greys out a markup that another compiler owns, because it is not what the reader is working on
	 * right now. The same thing should not sit above what they are working on. Sorting is stable, so within each of the
	 * two parts the ranking stays untouched.
	 * <p>
	 * This cannot go into the index: {@link DefaultMarkupRenderer#isInCurrentDefaultCompiler} asks the user's context
	 * which compiler is theirs, so the answer differs per reader and per moment.
	 */
	/**
	 * The user's default compiler, asked for once.
	 * <p>
	 * {@link DefaultMarkupRenderer#isInCurrentDefaultCompiler} answers the same question per section, but it derives
	 * the default compiler again on every call -- and every hit would pay for that. Asked once, the remaining question
	 * per hit is a single {@link Compiler#isCompiling}.
	 *
	 * @return null when nothing groups this wiki, in which case every hit belongs to the reader's view
	 */
	private static @Nullable PackageCompiler defaultCompiler(UserActionContext context) {
		if (Compilers.getCompilers(context.getArticleManager(), GroupingCompiler.class).isEmpty()) return null;
		for (PackageCompiler compiler : Compilers.getCompilers(context.getArticleManager(), PackageCompiler.class)) {
			if (Compilers.isDefaultCompiler(context, compiler)) return compiler;
		}
		return null;
	}

	private static List<HitGrouping.Group> byDefaultCompilerFirst(List<HitGrouping.Group> groups, int needed,
																  @Nullable PackageCompiler mine,
																  UserActionContext context) {
		List<HitGrouping.Group> ours = new ArrayList<>(needed);
		List<HitGrouping.Group> others = new ArrayList<>();
		int asked = 0;
		for (HitGrouping.Group group : groups) {
			// Asking costs a section lookup and a walk through the compilers, so we stop as soon as the page is full:
			// what follows cannot reach it any more, and the next page asks again from further in.
			if (ours.size() >= needed) break;
			asked++;
			(isDefaultCompiled(group.primary(), mine, context) ? ours : others).add(group);
		}
		ours.addAll(others);
		ours.addAll(groups.subList(asked, groups.size()));
		return ours;
	}

	private static List<HitGrouping.Group> onlyDefaultCompiler(List<HitGrouping.Group> groups,
															  @Nullable PackageCompiler mine,
															  UserActionContext context) {
		if (mine == null) return groups;
		List<HitGrouping.Group> ours = new ArrayList<>(groups.size());
		for (HitGrouping.Group group : groups) {
			if (isDefaultCompiled(group.primary(), mine, context)) ours.add(group);
		}
		return ours;
	}

	private static boolean isDefaultCompiled(SearchHit hit, @Nullable PackageCompiler mine,
											 UserActionContext context) {
		if (mine == null) return true;
		Section<?> section = hit.anchor().resolve(context.getArticleManager()).section();
		// a section we cannot resolve is not pushed down for it -- that would punish a stale index twice
		if (section == null) return true;
		if (mine.isCompiling(section)) return true;
		// a section nobody compiles belongs to everybody: no package statement, no other variant it could belong to
		return Compilers.getCompilers(section, PackageCompiler.class).isEmpty();
	}

	/**
	 * Where a click should land.
	 * <p>
	 * The content of an attachment can be compiled into an article of its own -- the {@code %%Attachment} markup does
	 * that -- and a hit inside it belongs to that article, not to the file. Linking to the file would hand the user a
	 * download of something they were reading. So the link goes to the markup that pulls the attachment in, and to its
	 * section, which is where the text they searched for is actually rendered.
	 */
	private static @Nullable String renderingArticle(SearchHit hit, UserActionContext context) {
		Article article = context.getArticleManager().getArticle(hit.title());
		if (article == null || !KnowWEUtils.isAttachmentArticle(article)) return null;
		if (!(context.getArticleManager() instanceof DefaultArticleManager manager)) return null;
		for (Section<AttachmentCompileType> compiling :
				manager.getAttachmentManager().getCompilingAttachmentSections(article)) {
			return "Wiki.jsp?page=" + compiling.getTitle().replace(" ", "+") + "#" + compiling.getID();
		}
		return null;
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
