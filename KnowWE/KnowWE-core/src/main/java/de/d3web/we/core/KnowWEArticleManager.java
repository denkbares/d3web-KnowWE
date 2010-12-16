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

package de.d3web.we.core;

import java.io.File;
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

import de.d3web.we.event.EventManager;
import de.d3web.we.event.UpdatingDependenciesEvent;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.store.KnowWESectionInfoStorage;
import de.d3web.we.utils.KnowWEUtils;
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
	private HashMap<String, KnowWEArticle> articleMap = new java.util.HashMap<String, KnowWEArticle>();

	/**
	 * List that keeps track of all articles that are sectionizing at the
	 * moment.
	 */
	private final Set<String> sectionizingArticles = new HashSet<String>();

	private Set<String> articlesToRefresh = new TreeSet<String>();

	/**
	 * List that keeps track of all articles that are updating their
	 * dependencies at the moment.
	 */
	private final Set<String> updatingArticles = new HashSet<String>();

	private boolean initializedArticles = false;

	protected KnowWESectionInfoStorage typeStore = new KnowWESectionInfoStorage();

	public KnowWESectionInfoStorage getTypeStore() {
		return typeStore;
	}

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
	 * (instead of the originalText) for the nodes with an ID in the nodesMap.
	 * Finally the article is saved with this new content.
	 * 
	 * @param map
	 * @param title
	 * @param nodesMap containing pairs of the nodeID and the new text for this
	 *        node
	 * @return
	 */
	public String replaceKDOMNodesSaveAndBuild(KnowWEParameterMap map, String title,
			Map<String, String> nodesMap) {
		// String user = map.getUser();
		String web = map.getWeb();
		KnowWEArticle art = this.getArticle(title);
		if (art == null) return "article not found: " + title;
		Section<KnowWEArticle> root = art.getSection();
		StringBuffer newText = new StringBuffer();
		appendTextReplaceNode(root, nodesMap, newText);

		String newArticleSourceText = newText.toString();
		KnowWEEnvironment.getInstance().getWikiConnector().writeArticleToWikiEnginePersistence(
				title, newArticleSourceText, map);
		KnowWEEnvironment.getInstance().buildAndRegisterArticle(map.getUser(),
				newArticleSourceText, title, web);

		return "done";
	}

	/**
	 * Replaces KDOM-nodes with the given texts, but not in the KDOM itself. It
	 * collects the originalTexts deep through the KDOM and appends the new text
	 * (instead of the originalText) for the nodes with an ID in the nodesMap.
	 * 
	 * @param map
	 * @param title
	 * @param nodesMap containing pairs of the nodeID and the new text for this
	 *        node
	 * @return The content of the article with the text changes or an error
	 *         String if the article wasn't found.
	 */
	public String replaceKDOMNodesWithoutSave(KnowWEParameterMap map,
			String title, Map<String, String> nodesMap) {
		KnowWEArticle art = this.getArticle(title);
		if (art == null) return "article not found: " + title;
		Section<?> root = art.getSection();
		StringBuffer newText = new StringBuffer();
		appendTextReplaceNode(root, nodesMap, newText);

		String newArticleSourceText = newText.toString();
		// saveUpdatedArticle(new KnowWEArticle(newArticleSourceText,
		// articleName,
		// KnowWEEnvironment.getInstance().getRootTypes(), this.web));
		return newArticleSourceText;
	}

	/**
	 * Looks in KDOM of given article for the Section object with given nodeID
	 * 
	 * @param title
	 * @param nodeID
	 * @return null if article or node not found
	 * @see findNode(String nodeID)
	 */
	public Section<?> findNode(String title, String nodeID) {
		if (nodeID == null || title == null) return null;

		KnowWEArticle art = this.getArticle(title);
		if (art == null) return null;
		return art.findSection(nodeID);
	}

	/**
	 * Looks in KDOM for the Section object with given nodeID The article name
	 * is not needed because it is part of the nodeID
	 * 
	 * @param nodeID
	 * @return null if article or node not found
	 */
	public Section<?> findNode(String nodeID) {
		String articleName;
		if (nodeID.contains("/")) {
			articleName = nodeID.substring(0, nodeID.indexOf("/"));
		}
		else {
			articleName = nodeID;
		}
		return findNode(articleName, nodeID);
	}

	private void appendTextReplaceNode(Section<?> sec,
			Map<String, String> nodesMap, StringBuffer newText) {
		if (nodesMap.containsKey(sec.getID())) {
			newText.append(nodesMap.get(sec.getID()));
			return;
		}
		List<Section<?>> children = sec.getChildren();
		if (children == null || children.isEmpty()
				|| sec.hasPossiblySharedChildren()) {
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

		// store new article
		articleMap.put(article.getTitle(), article);

		long startTime = System.currentTimeMillis();

		Logger.getLogger(this.getClass().getName()).log(
				Level.FINE,
				"-> Starting to update dependencies to article '" + article.getTitle()
						+ "' ->");
		updatingArticles.add(article.getTitle());

		EventManager.getInstance().fireEvent(new UpdatingDependenciesEvent(article));

		// if (initializedArticles)
		buildArticlesToRefresh();

		updatingArticles.remove(article.getTitle());
		Logger.getLogger(this.getClass().getName()).log(
				Level.FINE,
				"<- Finished updating dependencies to article '" + article.getTitle()
						+ "' in " + (System.currentTimeMillis() - startTime)
						+ "ms <-");

		Logger.getLogger(this.getClass().getName()).log(
				Level.INFO,
				"<<==== Finished building article '" + article.getTitle() + "' in "
						+ web + " in "
						+ (System.currentTimeMillis() - article.getStartTime())
						+ "ms <<====");
	}

	public void buildArticlesToRefresh() {
		for (String title : new ArrayList<String>(articlesToRefresh)) {
			if (updatingArticles.contains(title)) continue;
			KnowWEArticle newArt = KnowWEArticle.createArticle(
					articleMap.get(title).getSection().getOriginalText(), title,
					KnowWEEnvironment.getInstance().getRootType(), web, false);

			registerArticle(newArt);
		}
		this.articlesToRefresh = new TreeSet<String>();
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
		KnowWEEnvironment.getInstance().buildAndRegisterArticle("", "",
				article.getTitle(), web, true);

		articleMap.remove(article.getTitle());

		Logger.getLogger(this.getClass().getName()).log(Level.INFO,
				"-> Deleted article '" + article.getTitle() + "'" + " from " + web);
	}

	public void unregisterSectionizingArticles(String title) {
		this.sectionizingArticles.remove(title);
	}

	public void registerSectionizingArticle(String title) {
		this.sectionizingArticles.add(title);
	}

	public Set<String> getSectionizingArticles() {
		return Collections.unmodifiableSet(this.sectionizingArticles);
	}

	public Set<String> getDependenciesUpdatingArticles() {
		return Collections.unmodifiableSet(this.updatingArticles);
	}

	public void addArticleToRefresh(String title) {
		this.articlesToRefresh.add(title);
	}

	public void addAllArticlesToRefresh(Collection<String> titles) {
		this.articlesToRefresh.addAll(titles);
	}

	public boolean hasInitializedArticles() {
		return initializedArticles;
	}

	public void setInitializedArticles(boolean b) {
		initializedArticles = true;
	}

}
