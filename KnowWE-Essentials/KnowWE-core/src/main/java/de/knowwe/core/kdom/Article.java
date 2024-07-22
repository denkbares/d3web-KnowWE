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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.EventManager;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(Article.class);

	public static final int LOG_THRESHOLD = 50;
	private final ArticleManager articleManager;
	/**
	 * Name of this article (topic-name)
	 */
	private final String title;

	private final String web;
	private final String text;
	private boolean sectionized = false;

	/**
	 * The section representing the root-node of the KDOM-tree
	 */
	private Section<RootType> rootSection;

	private Article lastVersion;

	private final RootType rootType;

	private final boolean fullParse;

	/**
	 * Create a new article by parsing the given text into a section tree (KDOM). Use this method if you want to add the
	 * article to an article manager.
	 *
	 * @param text    the text of the article
	 * @param title   the title or name of the article, displayed at the top (and in the URL usually)
	 * @param manager the article manager the article belongs to
	 */
	public static Article createArticle(@NotNull String text, @NotNull String title, @NotNull ArticleManager manager) {
		return createArticle(text, title, manager, false);
	}

	/**
	 * Create a new article by parsing the given text into a section tree (KDOM). Use this method if you want to add the
	 * article to an article manager.
	 *
	 * @param text      the text of the article
	 * @param title     the title or name of the article, displayed at the top (and in the URL usually)
	 * @param manager   the article manager the article belongs to
	 * @param fullParse whether we should perform a full parse of the text or reuse section from previous article
	 *                  versions if possible
	 */
	public static Article createArticle(@NotNull String text, @NotNull String title, @NotNull ArticleManager manager, boolean fullParse) {
		return createArticle(text, title, manager.getWeb(), manager, fullParse);
	}

	/**
	 * Create a new temporary article by parsing the given text into a section tree (KDOM). Use this method if and only
	 * if the article will not be added to an article manager.
	 *
	 * @param text  the text of the article
	 * @param title the title or name of the article, displayed at the top (and in the URL usually)
	 * @param web   the web the article belongs to (currently unused)
	 */
	public static Article createTemporaryArticle(String text, String title, String web) {
		return createArticle(text, title, web, null, true);
	}

	public static Article createTemporaryArticle(String text, String title, String web, RootType root) {
		return createArticle(text, title, web, null, true, root);
	}


	private static Article createArticle(String text, String title, String web, @Nullable ArticleManager manager, boolean fullParse) {
		return createArticle(text, title, web, manager, fullParse, Environment.getInstance().getRootType());
	}

	/**
	 * Create a new article by parsing the given text into a section tree (KDOM)
	 *
	 * @param text      the text of the article
	 * @param title     the title or name of the article, displayed at the top (and in the URL usually)
	 * @param web       the web the article belongs to (currently unused)
	 * @param manager   the article manager the article belongs to
	 * @param fullParse whether we should perform a full parse of the text or reuse section from previous article
	 *                  versions if possible
	 * @param root		RootType to be used
	 */
	private static Article createArticle(String text, String title, String web, @Nullable ArticleManager manager, boolean fullParse, RootType root) {
		Article article = null;
		try {
			article = new Article(text, title, web, manager, fullParse, root);
		}
		catch (Exception e) {
			LOGGER.error("Exception while creating article", e);
		}
		return article;
	}

	private Article(@NotNull String text, @NotNull String title, @NotNull String web, @Nullable ArticleManager manager, boolean fullParse) {
		this(text, title, web, manager, fullParse, null);
	}

	private Article(@NotNull String text, @NotNull String title, @NotNull String web, @Nullable ArticleManager manager, boolean fullParse, @Nullable RootType root) {
		if (root == null) {
			rootType = RootType.getInstance();
		}
		else {
			rootType = root;
		}
		long start = System.currentTimeMillis();
		this.title = title;
		this.web = web;
		this.text = text;
		this.articleManager = manager;
		this.lastVersion = Environment.isInitialized() && articleManager != null ? Environment.getInstance()
				.getArticle(web, title) : null;

		this.fullParse = fullParse
				|| lastVersion == null
				|| Environment.getInstance().getCompilationMode() == CompilationMode.DEFAULT;

		sectionizeArticle(text);

		long time = System.currentTimeMillis() - start;
		if (time < LOG_THRESHOLD && articleManager != null && !articleManager.isInitialized() || articleManager == null) {
			LOGGER.debug("Sectionized article '" + title + "' in " + time + "ms");
		}
		else {
			LOGGER.info("Sectionized article '" + title + "' in " + time + "ms");
		}
		this.sectionized = true;
	}

	public boolean isSectionized() {
		return sectionized;
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
		return "Article: " + title + "\n----\n" + text;
	}

	public boolean isFullParse() {
		return this.fullParse;
	}

	public RootType getRootType() {
		return this.rootType;
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
		return articleManager == null;
	}
}
