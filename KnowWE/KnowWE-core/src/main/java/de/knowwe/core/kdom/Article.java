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

import de.d3web.utils.Log;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.Environment.CompilationMode;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Messages;
import de.knowwe.event.ArticleCreatedEvent;
import de.knowwe.event.KDOMCreatedEvent;

/**
 * This class is the representation of one wiki article in KnowWE. It is a Type
 * that always forms the root node and only the root node of each KDOM
 * document-parse-tree.
 *
 * @author Jochen
 */
public class Article {

	/**
	 * Name of this article (topic-name)
	 */
	private final String title;

	private final String web;

	/**
	 * The section representing the root-node of the KDOM-tree
	 */
	private Section<RootType> rootSection;

	private Article lastVersion;

	private final boolean fullParse;

	public static Article createArticle(String text, String title, String web) {
		return createArticle(text, title, web, false);
	}

	public static Article createEmptyArticle(String title, String web) {
		return new Article(title, web);
	}

	public static Article createArticle(String text, String title,
										String web, boolean fullParse) {
		Article article = null;
		try {
			article = new Article(text, title, web, fullParse);
			EventManager.getInstance().fireEvent(new ArticleCreatedEvent(article));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return article;
	}

	/**
	 * Constructor: starts recursive parsing by creating new Section object
	 */
	private Article(String text, String title, String web, boolean fullParse) {

		long startTimeOverall = System.currentTimeMillis();
		this.title = title;
		this.web = web;
		this.lastVersion = Environment.getInstance().getArticle(web, title);

		this.fullParse = fullParse
				|| lastVersion == null
				|| Environment.getInstance().getCompilationMode() == CompilationMode.DEFAULT;

		sectionizeArticle(text);

		Log.info("Sectionized article '" + title + "' in "
				+ (System.currentTimeMillis() - startTimeOverall) + "ms");
	}

	/**
	 * Constructor to create an empty article. This constructor is intended to be used
	 * in test scenarios. Under normally "user edits' an article" conditions, this
	 * constructor shall not be used.
	 */
	private Article(String title, String web) {
		this.title = title;
		this.web = web;
		this.lastVersion = null;
		this.fullParse = false;
		this.rootSection = Section.createSection("", getRootType(), null);
		this.rootSection.setArticle(this);
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
		rootSection.setParent(null);

		if (lastVersion != null) {
			lastVersion.destroy();
		}

		EventManager.getInstance().fireEvent(new KDOMCreatedEvent(this));
	}

	/**
	 * Destroy and cleans up stuff that was registered for the Sections of this article (like IDs and message caches).
	 */
	public void destroy() {
		unregisterSectionRecursively(this.getRootSection());
	}

	private void unregisterSectionRecursively(Section<?> section) {
		Messages.unregisterMessagesSection(section);
		Section.unregisterOrUpdateSectionID(section, this);
		for (Section<?> childSection : section.getChildren()) {
			unregisterSectionRecursively(childSection);
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
	 * The last version is only available during the initialization of the
	 * article
	 */
	public Article getLastVersionOfArticle() {
		return lastVersion;
	}

	/**
	 * Returns the simple name of this class, NOT THE NAME (Title) OF THIS
	 * ARTICLE! For the articles title, use getTitle() instead!
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
		return getRootSection().getText();
	}

	@Override
	public String toString() {
		return rootSection.getText();
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
			if (other.web != null) return false;
		}
		else if (!web.equals(other.web)) return false;
		return true;
	}

	public void setArticleManager(ArticleManager articleManager) {
		this.articleManager = articleManager;
	}

	public ArticleManager getArticleManager() {
		return articleManager;
	}

}
