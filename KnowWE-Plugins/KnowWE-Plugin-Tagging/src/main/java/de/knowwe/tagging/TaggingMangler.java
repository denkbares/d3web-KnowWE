/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.tagging;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.denkbares.collections.CountingSet;
import com.denkbares.strings.NumberAwareComparator;
import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Centralized management of tags. Takes care of adding/removing tags. And answers tag-queries.
 *
 * @author Fabian Haupt
 */
public class TaggingMangler {

	/**
	 * Singleton instance
	 */
	private static TaggingMangler me;

	/**
	 * Tag storage map
	 */
	private final Map<String, Set<String>> tagMap = new HashMap<>();

	/**
	 * The separator regex used to split tag strings
	 */
	public final static String TAG_SEPARATOR = "[\\h\\s\\v,;]+";

	private TaggingMangler() {
	}

	public static synchronized TaggingMangler getInstance() {
		if (me == null) {
			me = new TaggingMangler();
		}

		return me;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * Sets a set of tags for an article. Overwrites all existing tags.
	 *
	 * @param page The article containing the tag
	 * @param tags The tags as unsplitted text
	 */
	public void registerTags(String page, String tags) {
		registerTags(page, extractTags(tags));
	}

	/**
	 * Sets a set of tags for an article. Overwrites all existing tags.
	 *
	 * @param page The article containing the tag
	 * @param tags The tags
	 */
	public void registerTags(String page, Collection<String> tags) {
		if (page == null || tags == null) {
			return;
		}

		Set<String> set = new LinkedHashSet<>();
		for (String string : tags) {
			set.add(string.trim());
		}
		tagMap.put(page, set);
	}

	/**
	 * Adds a tag to a page. The new tag is added into the <tags></tags> part. If there is none, it's created at the end
	 * of the page Multiple <tags> sections are combined into a single one
	 *
	 * @param pageName the article to add the tag to
	 * @param tag      the tag to be added
	 */
	public void addTag(String pageName, String tag, UserActionContext context) throws IOException {
		// check if tags will change
		Set<String> tags = new LinkedHashSet<>(getPageTags(pageName));
		if (!tags.add(Strings.trim(tag))) return;

		// then add the tag to the page
		Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, pageName);
		setTags(article, tags, context);
	}

	/**
	 * removes a tag from a page. <tags></tags> is checked first.
	 *
	 * @param pageName the article to remove the tag to
	 * @param tag      the tag to be removed
	 */
	public void removeTag(String pageName, String tag, UserActionContext context) throws IOException {
		// check if tags will change
		Set<String> tags = new LinkedHashSet<>(getPageTags(pageName));
		if (!tags.remove(Strings.trim(tag))) return;

		// then add the tag to the page
		Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, pageName);
		setTags(article, tags, context);
	}

	/**
	 * Sets the specified tags to the article, cleaning up all existing %%tags into a single markup.
	 */
	private void setTags(Article article, Set<String> tags, UserActionContext context) throws IOException {

		// Look for <tags> sections in article
		List<Section<Tags>> tagsSections = $(article).successor(Tags.class).asList();
		if (tagsSections.isEmpty()) {
			// if there are no, create a new tag section and return
			addNewTagSection(article, Strings.concat(" ", tags), context);
			return;
		}

		// replace all sections by ""
		Map<String, String> nodesMap = new HashMap<>();
		for (Section<Tags> section : tagsSections) {
			nodesMap.put(section.getID(), "");
		}

		// and the last one by all the tags
		String output = createTagSectionString(Strings.concat(" ", tags));
		nodesMap.put(tagsSections.get(tagsSections.size() - 1).getID(), output);

		// and modify the article
		Sections.replace(context, nodesMap).sendErrors(context);
	}

	/**
	 * returns a list of pages that are tagged with the given tag.
	 */
	public List<String> getPages(String tag) {
		List<String> result = new LinkedList<>();

		for (String pageName : tagMap.keySet()) {
			if (tagMap.get(pageName).contains(tag)) {
				result.add(pageName);
			}
		}

		return result;
	}

	/**
	 * Creates a list of tags the given page is tagged with. Always returns a list unless the page parameter is null.
	 * (No tags -> empty list)
	 *
	 * @param page The query page
	 * @return List The list of tags, or null if page was null
	 */
	public Set<String> getPageTags(String page) {
		if (page == null) return null;
		return tagMap.getOrDefault(page, Collections.emptySet());
	}

	/**
	 * Returns a list of all existing tags
	 *
	 * @return List of Strings with all existing, unique tags
	 */
	public Set<String> getAllTags() {
		LinkedHashSet<String> result = new LinkedHashSet<>();
		tagMap.values().forEach(result::addAll);
		return result;
	}

	/**
	 * returns a hashmap of the tags and an integer, that can be used as font-size (scaled between minSize and maxSize)
	 *
	 * @param minSize the minimum font size
	 * @param maxSize the maximum font size
	 * @return the font size for the tags
	 */
	public Map<String, Integer> getCloudList(Predicate<String> pageFilter, int minSize, int maxSize) {
		Map<String, Integer> result = new HashMap<>();
		float factor = Math.abs(maxSize - minSize);
		getAllTagsWithWeight(pageFilter).forEach((tag, weight) ->
				result.put(tag, Math.round(Math.min(minSize, maxSize) + (weight * factor))));
		return result;
	}

	/**
	 * returns a list of all existing tags with normalized weights
	 */
	private Map<String, Float> getAllTagsWithWeight(Predicate<String> pageFilter) {
		CountingSet<String> allTags = new CountingSet<>();
		tagMap.forEach((page, pageTags) -> {
			if (pageFilter.test(page)) allTags.addAll(pageTags);
		});
		float max = allTags.stream().mapToInt(allTags::getCount).max().orElse(0);

		// remap the tags to the normalized value
		HashMap<String, Float> weighted = new HashMap<>();
		allTags.toMap().forEach((tag, weight) ->
				weighted.put(tag, (max <= 1) ? 0.5f : ((weight - 1f) / (max - 1f))));
		return weighted;
	}

	/**
	 * sets tags to tag and replaces old ones
	 *
	 * @param pageName the name of the article
	 * @param tags     comma/space separated list of tags
	 */
	public void setTags(String pageName, String tags, UserActionContext context) throws IOException {
		Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, pageName);
		setTags(article, extractTags(tags), context);
	}

	/**
	 * Forcibly adds a new tags-section - the hardcore way
	 */
	public void addNewTagSection(Article article, String content, UserActionContext context) throws IOException {
		String articleText = article.getText() + "\n\n" + createTagSectionString(content);
		Map<String, String> map = Collections.singletonMap(article.getRootSection().getID(), articleText);
		Sections.replace(context, map).sendErrors(context);
	}

	/**
	 * Forcibly adds a new tags-section - the hardcore way
	 */
	public void addNewTagSection(String topic, String content, UserActionContext context) throws IOException {
		Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB, topic);
		addNewTagSection(article, content, context);
	}

	private String createTagSectionString(String content) {
		return "%%tags\n" + processTagString(content) + "\n%\n";
	}

	/**
	 * Processes a user-provided tag string into the the proper format
	 *
	 * @param tagString A string of tags separated by TAG_SEPARATOR
	 * @return A trimmed tag list separated by spaces
	 */
	private String processTagString(String tagString) {
		return Strings.concat(" ", extractTags(tagString));
	}

	/**
	 * Processes a user-provided tag string into a ordered set of tag names.
	 *
	 * @param tagString A string of tags separated by TAG_SEPARATOR
	 * @return A trimmed tag set, ordered as in the original string
	 */
	private Set<String> extractTags(String tagString) {
		Set<String> tags = new LinkedHashSet<>();
		for (String tag : tagString.split(TAG_SEPARATOR)) {
			// due to the splitting regex, the tags are automatically trimmed
			if (!tag.isEmpty()) {
				tags.add(tag);
			}
		}
		return tags;
	}

	/**
	 * Searches for pages containing all of the requested tags
	 *
	 * @param querytags Space-separated string of tags the pages must contain
	 * @return List of {@link TaggingSearchResult} instances for the found pages
	 */
	public List<TaggingSearchResult> searchPages(String querytags) {
		String[] tags = querytags.split(" ");
		List<TaggingSearchResult> result = new LinkedList<>();

		Iterator<String> it = tagMap.keySet().iterator();

		article_loop:
		while (it.hasNext()) {
			String article = it.next();
			Set<String> articleTags = tagMap.get(article);

			for (String tag : tags) {
				if (!articleTags.contains(tag)) {
					continue article_loop;
				}
			}

			// The page is tagged with all query tags, add to result set
			result.add(new TaggingSearchResult(article, new String[] {}, 1));
		}

		return result;
	}

	/**
	 * Performs a search for articles tagged with all of the tags and renders a result UI
	 *
	 * @param queryString Space-separated list of tags to search for
	 * @return Wiki markup displaying the results
	 */
	public String getResultPanel(String queryString) {
		if (Strings.nonBlank(queryString)) {
			List<TaggingSearchResult> pages = searchPages(queryString);
			pages.sort(Comparator.comparing(TaggingSearchResult::getPagename, NumberAwareComparator.CASE_INSENSITIVE));
			return renderResults(pages, queryString);
		}
		else {
			return "No query.";
		}
	}

	public String renderResults(Collection<TaggingSearchResult> pages, String queryString) {
		if (pages.isEmpty()) {
			return "No pages for query '" + queryString + "'.";
		}

		TaggingMangler tm = TaggingMangler.getInstance();
		StringBuilder html = new StringBuilder();
		// html.append("<ul>\n");
		html.append("\n|| Page || Tags \n");

		for (TaggingSearchResult cur : pages) {
			String pagename = cur.getPagename();
			html.append("| [").append(pagename);
			html.append("]\t| ");

			for (String tag : tm.getPageTags(pagename)) {
				boolean matched = tag.equalsIgnoreCase(queryString);
				if (matched) html.append("__");
				html.append(tag);
				if (matched) html.append("__");
				html.append(" ");
			}

			html.append("\n");
		}

		// html.append("</ul>\n");
		html.append("\n");
		return html.toString();
	}

	/**
	 * Unregisters all tags of the given article.
	 *
	 * @param article the article for which the tags are to be unregistered
	 * @created 10.02.2013
	 */
	public void unregisterTags(Article article) {
		tagMap.remove(article.getTitle());
	}
}
