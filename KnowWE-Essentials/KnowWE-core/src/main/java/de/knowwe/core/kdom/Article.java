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

package de.knowwe.core.kdom;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.events.EventManager;
import com.denkbares.utils.Log;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.Environment.CompilationMode;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;
import de.knowwe.event.KDOMCreatedEvent;

/**
 * This class is the representation of one wiki article in KnowWE. It is a Type that always forms the root node and only
 * the root node of each KDOM document-parse-tree.
 *
 * @author Jochen
 */
public final class Article {

	public static final int LOG_THRESHOLD = 50;
	/**
	 * Name of this article (topic-name)
	 */
	private final String title;

	private final String web;
	private final String text;

	/**
	 * The section representing the root-node of the KDOM-tree
	 */
	private Section<RootType> rootSection;

	private Article lastVersion;

	private final boolean fullParse;
	private boolean temporary;

	public static Article createArticle(String text, String title, String web) {
		return createArticle(text, title, web, false);
	}

	public static Article createEmptyArticle(String title, String web) {
		return new Article(title, web);
	}

	public static Article createArticle(String text, String title, String web, boolean fullParse) {
		return createArticle(text, title, web, fullParse, false);
	}

	public static Article createArticle(String text, String title, String web, boolean fullParse, boolean temporary) {
		Article article = null;
		try {
			article = new Article(text, title, web, fullParse, temporary);
		}
		catch (Exception e) {
			Log.severe("Exception while creating article", e);
		}
		return article;
	}

	/**
	 * Constructor: starts recursive parsing by creating new Section object
	 */
	private Article(@NotNull String text, @NotNull String title, @NotNull String web, boolean fullParse, boolean temporary) {

		long start = System.currentTimeMillis();
		this.title = title;
		this.web = web;
		this.text = text;
		this.temporary = temporary;
		this.lastVersion = Environment.isInitialized() && !temporary ? Environment.getInstance()
				.getArticle(web, title) : null;

		this.fullParse = fullParse
				|| lastVersion == null
				|| Environment.getInstance().getCompilationMode() == CompilationMode.DEFAULT;

		sectionizeArticle(text);

		long time = System.currentTimeMillis() - start;
		if (time < LOG_THRESHOLD) {
			Log.fine("Sectionized article '" + title + "' in " + time + "ms");
		}
		else {
			Log.info("Sectionized article '" + title + "' in " + time + "ms");
		}
	}

	/**
	 * Constructor to create an empty article. This constructor is intended to be used in test scenarios. Under normally
	 * "user edits' an article" conditions, this constructor shall not be used.
	 */
	private Article(String title, String web) {
		this.title = title;
		this.web = web;
		this.lastVersion = null;
		this.fullParse = false;
		this.rootSection = Section.createSection("", getRootType(), null);
		this.rootSection.setArticle(this);
		this.text = "";
	}

	public void clearLastVersion() {
		// important! prevents memory leak
		lastVersion = null;
	}

	private void sectionizeArticle(String text) {

		// create Sections recursively
		Section<?> dummySection = Section.createSection(text, getRootType(), null);
		dummySection.setArticle(this);
		getRootType().getParser().parse(text, dummySection);
		rootSection = Sections.child(dummySection, RootType.class);
		//noinspection ConstantConditions
		rootSection.setParent(null);

		if (lastVersion != null) {
			lastVersion.destroy(this);
		}

		EventManager.getInstance().fireEvent(new KDOMCreatedEvent(this));
	}

	/**
	 * Destroy and cleans up stuff that was registered for the Sections of this article (like IDs and message caches).
	 * Pass the new version of the article if available, there might be stuff that can be salvaged for it.
	 *
	 * @param newArticle the new version of the article, if there is on (allowed to be null in this case only)
	 */
	public void destroy(Article newArticle) {
		unregisterSectionRecursively(this.getRootSection(), newArticle);
	}

	private void unregisterSectionRecursively(Section<?> section, Article newArticle) {
		Messages.unregisterMessagesSection(section);
		Section.unregisterOrUpdateSectionID(section, newArticle);
		for (Section<?> childSection : section.getChildren()) {
			unregisterSectionRecursively(childSection, newArticle);
		}
	}

	/**
	 * Returns the title of this Article.
	 */
	public String getTitle() {
		return title;
	}

	public String getWeb() {
		return web;
	}

	/**
	 * The last version is only available during the initialization of the article
	 */
	public Article getLastVersionOfArticle() {
		return lastVersion;
	}

	/**
	 * Returns the simple name of this class, NOT THE NAME (Title) OF THIS ARTICLE! For the articles title, use
	 * getTitle() instead!
	 */
	public String getName() {
		return this.getClass().getSimpleName();
	}

	public Section<RootType> getRootSection() {
		return rootSection;
	}

	private ArticleManager articleManager;

	public String collectTextsFromLeaves() {
		return this.rootSection.collectTextsFromLeaves();
	}

	/**
	 * Returns the full text this article is build from.
	 *
	 * @return the full text of this article
	 * @created 25.11.2013
	 */
	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return "Article: " + title + "\n----\n" + rootSection.getText();
	}

	public boolean isFullParse() {
		return this.fullParse;
	}

	public RootType getRootType() {
		return RootType.getInstance();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((web == null) ? 0 : web.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Article other = (Article) obj;
		if (title == null) {
			if (other.title != null) return false;
		}
		else if (!title.equals(other.title)) return false;
		if (web == null) {
			return other.web == null;
		}
		else {
			return web.equals(other.web);
		}
	}

	public void setArticleManager(ArticleManager articleManager) {
		this.articleManager = articleManager;
	}

	/**
	 * Provides the {@link ArticleManager} this article belongs to.
	 * <p>Attention:</p> Articles will not always be added to an article manager, e.g. when creating temp articles for
	 * viewing older versions, so this might return null!
	 *
	 * @return the article manager of this article or null, if it doesn't belong to one
	 */
	@Nullable
	public ArticleManager getArticleManager() {
		return articleManager;
	}

	public boolean isTemporary() {
		return temporary;
	}
}
