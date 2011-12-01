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

package de.knowwe.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.KnowWEWikiConnector;
import de.knowwe.event.ArticleRegisteredEvent;
import de.knowwe.event.ArticleUpdatesFinishedEvent;
import de.knowwe.event.UpdatingDependenciesEvent;
import dummies.KnowWETestWikiConnector;

/**
 * @author Jochen
 * 
 *         Manages all the articles of one web in a HashMap
 * 
 */
public class KnowWEArticleManager {

	/**
	 * Stores KnowWEArticles for article-names
	 */
	private HashMap<String, KnowWEArticle> articleMap = new HashMap<String, KnowWEArticle>();

	private final TreeSet<String> currentRefreshQueue = new TreeSet<String>();

	/**
	 * List that keeps track of all articles that are updating their
	 * dependencies at the moment.
	 */
	private final Set<String> updatingArticles = new HashSet<String>();

	/**
	 * List that keeps track of all articles, that are already queued for
	 * updating and don't need to be queued again.
	 */
	private final HashSet<String> globalRefreshQueue = new HashSet<String>();

	private boolean initializedArticles = false;

	private final String web;

	public String jarsPath;
	public String reportPath;

	public String getReportPath() {
		return reportPath;
	}

	private static ResourceBundle rb = ResourceBundle
			.getBundle("KnowWE_config");

	public KnowWEArticleManager(KnowWEEnvironment env, String webname) {
		this.web = webname;
		if (!(env.getWikiConnector() instanceof KnowWETestWikiConnector)) {
			jarsPath = KnowWEUtils.getRealPath(env.getWikiConnector()
					.getServletContext(), rb.getString("path_to_jars"));
			reportPath = KnowWEUtils.getRealPath(env.getWikiConnector()
					.getServletContext(), rb.getString("path_to_reports"));

		}
		else {
			jarsPath = System.getProperty("java.io.tmpdir")
					+ File.separatorChar + "jars";
			reportPath = System.getProperty("java.io.tmpdir")
					+ File.separatorChar + "reports";

		}
	}

	public String getWebname() {
		return web;
	}

	/**
	 * Servs the KnowWEArticle for a given article name
	 * 
	 * @param title
	 * @return
	 */
	public KnowWEArticle getArticle(String title) {
		return articleMap.get(title);
	}

	public Iterator<KnowWEArticle> getArticleIterator() {
		return articleMap.values().iterator();
	}

	public Collection<KnowWEArticle> getArticles() {
		return Collections.unmodifiableCollection(articleMap.values());
	}

	public Set<String> getTitles() {
		return Collections.unmodifiableSet(articleMap.keySet());
	}

	/**
	 * Replaces KDOM-nodes with the given texts, but not in the KDOM itself. It
	 * collects the originalTexts deep through the KDOM and appends the new text
	 * (instead of the original text) for the nodes with an ID in the nodesMap.
	 * Finally the article is saved with this new content.
	 * 
	 * @param context
	 * @param title
	 * @param nodesMap containing pairs of the nodeID and the new text for this
	 *        node
	 * @return true if the nodes were successfully replaced, false else
	 * @throws IOException
	 */
	public boolean replaceKDOMNodesSaveAndBuild(UserActionContext context, String title,
			Map<String, String> nodesMap) throws IOException {

		// check if article exists
		KnowWEArticle art = getArticle(title);
		if (art == null) {
			context.sendError(404, "Page '" + title + "' could not be found.");
			return false;
		}

		// check if all the ids exist
		for (String id : nodesMap.keySet()) {
			if (Sections.getSection(id) == null) {
				context.sendError(409, "Section '" + id
						+ "' could not be found, possibly because somebody else"
						+ " has edited the page.");
				return false;
			}
		}

		// check for user access
		if (!KnowWEEnvironment.getInstance().getWikiConnector().userCanEditPage(title,
				context.getRequest())) {
			context.sendError(403, "You do not have the permission to edit this page.");
			return false;
		}

		StringBuffer newText = new StringBuffer();
		appendTextReplaceNode(art.getSection(), nodesMap, newText);

		String newArticleText = newText.toString();
		KnowWEWikiConnector wikiConnector = KnowWEEnvironment.getInstance().getWikiConnector();
		wikiConnector.writeArticleToWikiEnginePersistence(
				title, newArticleText, context);

		if (wikiConnector instanceof KnowWETestWikiConnector) {
			// This is only needed for the test environment. In the running
			// wiki, this is automatically called after the change to the
			// persistence.
			KnowWEEnvironment.getInstance().buildAndRegisterArticle(newArticleText, title,
					context.getWeb());
		}
		return true;
	}

	/**
	 * Replaces KDOM-nodes with the given texts, but not in the KDOM itself. It
	 * collects the originalTexts deep through the KDOM and appends the new text
	 * (instead of the originalText) for the nodes with an ID in the nodesMap.
	 * 
	 * @param context
	 * @param title
	 * @param nodesMap containing pairs of the nodeID and the new text for this
	 *        node
	 * @return The content of the article with the text changes or an error
	 *         String if the article wasn't found.
	 */
	public String replaceKDOMNodesWithoutSave(UserActionContext context,
			String title, Map<String, String> nodesMap) {
		KnowWEArticle art = this.getArticle(title);
		if (art == null) return "article not found: " + title;

		StringBuffer newText = new StringBuffer();
		appendTextReplaceNode(art.getSection(), nodesMap, newText);

		return newText.toString();
	}

	private void appendTextReplaceNode(Section<?> sec,
			Map<String, String> nodesMap, StringBuffer newText) {

		String text = nodesMap.get(sec.getID());
		if (text != null) {
			newText.append(text);
			return;
		}

		List<Section<?>> children = sec.getChildren();
		if (children == null || children.isEmpty()
				|| sec.hasSharedChildren()) {
			newText.append(sec.getOriginalText());
			return;
		}
		for (Section<?> section : children) {
			appendTextReplaceNode(section, nodesMap, newText);
		}
	}

	/**
	 * Registers an changed article in the manager and also updates depending
	 * articles.
	 * 
	 * @created 14.12.2010
	 * @param article is the changed article to register
	 */
	public void registerArticle(KnowWEArticle article) {
		registerArticle(article, true);
	}

	/**
	 * Registers an changed article in the manager and also updates depending
	 * articles.
	 * 
	 * @created 14.12.2010
	 * @param article is the changed article to register
	 * @param updateDependencies determines whether to update dependencies with
	 *        this registration
	 */
	public void registerArticle(KnowWEArticle article, boolean updateDependencies) {

		// store new article
		String title = article.getTitle();
		articleMap.put(title, article);

		long startTime = System.currentTimeMillis();

		Logger.getLogger(this.getClass().getName()).log(
				Level.FINE,
				"-> Starting to update dependencies to article '" + title
						+ "' ->");
		updatingArticles.add(title);

		EventManager.getInstance().fireEvent(new UpdatingDependenciesEvent(article));

		if (updateDependencies) updateQueuedArticles();

		updatingArticles.remove(title);
		Logger.getLogger(this.getClass().getName()).log(
				Level.FINE,
				"<- Finished updating dependencies to article '" + title
						+ "' in " + (System.currentTimeMillis() - startTime)
						+ "ms <-");

		Logger.getLogger(this.getClass().getName()).log(
				Level.INFO,
				"<<==== Finished building article '" + title + "' in "
						+ web + " in "
						+ (System.currentTimeMillis() - article.getStartTime())
						+ "ms <<====");
		EventManager.getInstance().fireEvent(new ArticleRegisteredEvent(article));
	}

	public void updateQueuedArticles() {

		List<String> localQueue = new ArrayList<String>();
		while (!currentRefreshQueue.isEmpty()) {
			String title = currentRefreshQueue.pollFirst();
			if (!globalRefreshQueue.contains(title)) {
				// Since this method is called recursively, we need a global
				// queue to keep track of which articles are already queued.
				// Don't queue (or update) articles in this call, if they are
				// already queued further up in the call stack.
				localQueue.add(title);
				globalRefreshQueue.add(title);
			}
		}

		for (String title : localQueue) {
			if (!updatingArticles.contains(title)) {
				KnowWEArticle newArt = KnowWEArticle.createArticle(
						articleMap.get(title).getSection().getOriginalText(), title,
						KnowWEEnvironment.getInstance().getRootType(), web, false);
				registerArticle(newArt, true);
			}
			globalRefreshQueue.remove(title);
		}

		if (globalRefreshQueue.isEmpty()) {
			EventManager.getInstance().fireEvent(new ArticleUpdatesFinishedEvent());
		}
	}

	public void clearArticleMap() {
		this.articleMap = new java.util.HashMap<String, KnowWEArticle>();
	}

	/**
	 * Deletes the given article from the article map and invalidates all
	 * knowledge content that was in the article.
	 * 
	 * @param article The article to delete
	 */
	public void deleteArticle(KnowWEArticle article) {
		KnowWEEnvironment.getInstance().buildAndRegisterArticle("",
				article.getTitle(), web, true);

		articleMap.remove(article.getTitle());

		Logger.getLogger(this.getClass().getName()).log(Level.INFO,
				"-> Deleted article '" + article.getTitle() + "'" + " from " + web);
	}

	public Set<String> getUpdatingArticles() {
		return Collections.unmodifiableSet(this.updatingArticles);
	}

	public void addArticleToUpdate(String title) {
		this.currentRefreshQueue.add(title);
	}

	public void addAllArticlesToUpdate(Collection<String> titles) {
		this.currentRefreshQueue.addAll(titles);
	}

	public boolean areArticlesInitialized() {
		return initializedArticles;
	}

	public void setArticlesInitialized(boolean b) {
		initializedArticles = true;
	}

}
