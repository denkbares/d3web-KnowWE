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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Centralized management of tags. Takes care of adding/removing tags. And
 * answers tag-queries.
 * 
 * @author Fabian Haupt
 * 
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
	public final static String TAG_SEPARATOR = " |,";

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
	 * @param tag The tag
	 */
	public void registerTags(String page, Collection<String> tags) {
		if (page == null || tags == null) {
			return;
		}

		Set<String> set = new HashSet<>();
		for (String string : tags) {
			set.add(string.trim());
		}
		tagMap.put(page, set);
	}

	/**
	 * Adds a tag to a page. The new tag is added into the <tags></tags> part.
	 * If there is none, it's created at the end of the page Multiple <tags>
	 * sections are combined into a single one
	 * 
	 * @param pagename
	 * @param tag
	 * @throws IOException
	 */
	public void addTag(String pagename, String tag, UserActionContext context) throws IOException {
		Article article = Environment.getInstance().getArticle(
				Environment.DEFAULT_WEB, pagename);

		// Look for <tags> sections
		List<Section<Tags>> tagsSections = Sections.successors(
				article.getRootSection(), Tags.class);
		Set<String> tags = new HashSet<>();

		if (!tagsSections.isEmpty()) {

			for (Section<?> section : tagsSections) {
				Section<TagsContent> content = Sections.successor(section, TagsContent.class);
				for (String temptag : content.getText().split(TAG_SEPARATOR)) {
					tags.add(temptag.trim());
				}
			}

			// tags is a set, dupe checking isn't needed
			tags.add(tag.trim());

			String output = createTagSectionString(Strings.concat(" ", tags));
			Section<Tags> firstTagsSection = tagsSections.remove(0);

			Map<String, String> nodesMap = new HashMap<>();
			nodesMap.put(firstTagsSection.getID(), output);
			for (Section<Tags> section : tagsSections) {
				nodesMap.put(section.getID(), "");
			}
			Sections.replace(context, nodesMap).sendErrors(context);
		}
		else {
			addNewTagSection(pagename, tag, context);
		}
	}

	/**
	 * removes a tag from a page. <tags></tags> is checked first. If the tag is
	 * not found there, the page is checked for inline annotations.
	 * 
	 * @param pagename
	 * @param tag
	 * @throws IOException
	 */
	public void removeTag(String pagename, String tag, UserActionContext context) throws IOException {
		Article article = Environment.getInstance().getArticle(
				Environment.DEFAULT_WEB, pagename);

		// Look for <tags> sections
		List<Section<TagsContent>> tagsSections = new ArrayList<>();
		Sections.successors(article.getRootSection(), TagsContent.class, tagsSections);
		Set<String> tags = new HashSet<>();

		for (Section<TagsContent> cur : tagsSections) {
			for (String temptag : cur.getText().split(TAG_SEPARATOR)) {
				tags.add(temptag.trim());
			}
		}

		StringBuilder sb = new StringBuilder();

		for (String temptag : tags) {
			if (!temptag.equals(tag)) {
				sb.append(temptag.trim()).append(' ');
			}
		}

		String output = sb.toString().trim();

		Section<?> keep = tagsSections.get(0);

		Map<String, String> nodesMap = new HashMap<>();
		nodesMap.put(keep.getID(), output);
		Sections.replace(context, nodesMap).sendErrors(context);
	}

	/**
	 * returns a list of pages that are tagged with the given tag.
	 * 
	 * @param tag
	 * @return
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
	 * Creates a list of tags the given page is tagged with. Always returns a
	 * list unless the page parameter is null. (No tags -> empty list)
	 * 
	 * @param page The query page
	 * @return List The list of tags, or null if page was null
	 */
	public List<String> getPageTags(String page) {
		if (page == null) {
			return null;
		}

		List<String> result = new LinkedList<>();
		Set<String> tagsForPage = tagMap.get(page);

		if (tagsForPage != null) {
			result.addAll(tagsForPage);
		}

		return result;
	}

	/**
	 * Returns a list of all existing tags
	 * 
	 * @return List of Strings with all existing, unique tags
	 */
	public List<String> getAllTags() {
		Set<String> set = new HashSet<>(getAllTagsWithDuplicates());
		return new ArrayList<>(set);
	}

	/**
	 * returns a hashmap of the tags and an integer, that can be used as
	 * font-size (scaled between minSize and maxSize)
	 * 
	 * @param minSize
	 * @param maxSize
	 * @return
	 */
	public Map<String, Integer> getCloudList(int minSize, int maxSize) {
		if (minSize > maxSize) {
			int t = minSize;
			minSize = maxSize;
			maxSize = t;
		}

		Map<String, Integer> result = new HashMap<>();
		Map<String, Float> weighted = getAllTagsWithWeight();
		float factor = maxSize - minSize;

		for (Entry<String, Float> cur : weighted.entrySet()) {
			result.put(cur.getKey(), Math.round(minSize
					+ (cur.getValue() * factor)));
		}

		return result;
	}

	/**
	 * returns a list of all existing tags with normalized weights
	 * 
	 * @return
	 */
	public Map<String, Float> getAllTagsWithWeight() {
		List<String> tags = getAllTagsWithDuplicates();
		HashMap<String, Float> countlist = new HashMap<>();
		float max = 0;

		for (String cur : tags) {
			float c = 0;

			if (countlist.get(cur) == null) {
				countlist.put(cur, 1f);
				c = 1;
			}
			else {
				c = countlist.get(cur) + 1;
				countlist.put(cur, c);
			}

			max = c > max ? c : max;
		}

		HashMap<String, Float> weighted = new HashMap<>();

		for (Entry<String, Float> cur : countlist.entrySet()) {
			weighted.put(cur.getKey(), (float) (max - 1 == 0 ? 0.5 : (cur
					.getValue() - 1)
					/ (max - 1)));
		}

		return weighted;
	}

	/**
	 * Compiles a list containing all concatenated tag lists
	 * 
	 * @return
	 */
	private List<String> getAllTagsWithDuplicates() {
		List<String> result = new LinkedList<>();

		for (String s : tagMap.keySet()) {
			result.addAll(tagMap.get(s));
		}

		return result;
	}

	/**
	 * sets tags to tag and replaces old ones
	 * 
	 * @param topic
	 * @param tags comma/space separated list of tags
	 * @throws IOException
	 */
	public void setTags(String topic, String tags, UserActionContext context) throws IOException {
		Article article = Environment.getInstance().getArticle(Environment.DEFAULT_WEB,
				topic);
		List<Section<Tags>> tagslist = Sections.successors(
				article.getRootSection(), Tags.class);
		String tagSectionString = createTagSectionString(tags);

		if (!tagslist.isEmpty()) {
			Section<?> keep = tagslist.remove(0);

			Map<String, String> nodesMap = new HashMap<>();
			nodesMap.put(keep.getID(), tagSectionString);
			for (Section<Tags> section : tagslist) {
				nodesMap.put(section.getID(), "");
			}
			Sections.replace(context, nodesMap).sendErrors(context);
		}
		else {
			addNewTagSection(topic, tags, context);
		}
	}

	/**
	 * Forcibly adds a new tags-section - the hardcore way
	 * 
	 * @throws IOException
	 */
	public void addNewTagSection(String topic, String content,
			UserActionContext context) throws IOException {
		Article article = Environment.getInstance().getArticle(
				Environment.DEFAULT_WEB, topic);

		Section<?> rootSection = article.getRootSection();
		String articleText = rootSection.getText();

		articleText += createTagSectionString(content);

		Map<String, String> nodesMap = new HashMap<>();
		nodesMap.put(rootSection.getID(), articleText);
		Sections.replace(context, nodesMap).sendErrors(context);
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
		Set<String> tags = new HashSet<>();
		for (String rawTag : tagString.split(TAG_SEPARATOR)) {
			String trimmed = rawTag.trim();
			if (!trimmed.isEmpty()) {
				tags.add(trimmed);
			}
		}
		return Strings.concat(" ", tags);
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

		article_loop: while (it.hasNext()) {
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
	 * Performs a search for articles tagged with all of the tags and renders a
	 * result UI
	 * 
	 * @param queryString Space-separated list of tags to search for
	 * @return Wiki markup displaying the results
	 */
	public String getResultPanel(String queryString) {
		if (queryString != null) {
			List<TaggingSearchResult> pages = searchPages(queryString);
			Collections.sort(pages, new Comparator<TaggingSearchResult>() {

				@Override
				public int compare(TaggingSearchResult o1, TaggingSearchResult o2) {
					return String.CASE_INSENSITIVE_ORDER.compare(
							o1.getPagename(), o2.getPagename());
				}
			});

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
	 * @created 10.02.2013
	 * @param article the article for which the tags are to be unregistered
	 */
	public void unregisterTags(Article article) {
		tagMap.remove(article.getTitle());
	}

}
