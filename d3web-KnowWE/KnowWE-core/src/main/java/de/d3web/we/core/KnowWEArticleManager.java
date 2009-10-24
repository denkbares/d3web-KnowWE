/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

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

	private List<String> initializingArticles = new ArrayList<String>();

	protected KnowWESectionInfoStorage typeStore = new KnowWESectionInfoStorage();

	public KnowWESectionInfoStorage getTypeStore() {
		return typeStore;
	}

	private String webname;
	private SemanticCore sc;

	public String jarsPath;
	public String reportPath;

	public String getReportPath() {
		return reportPath;
	}

	private static ResourceBundle rb = ResourceBundle
			.getBundle("WebParserConfig");

	public KnowWEArticleManager(KnowWEEnvironment env, String webname) {
		this.webname = webname;
		if (!(env.getWikiConnector() instanceof KnowWETestWikiConnector)) {
			jarsPath = KnowWEUtils.getRealPath(env.getWikiConnector()
					.getServletContext(), rb.getString("path_to_jars"));
			reportPath = KnowWEUtils.getRealPath(env.getWikiConnector()
					.getServletContext(), rb.getString("path_to_reports"));

		} else {
			jarsPath = System.getProperty("java.io.tmpdir")
					+ File.separatorChar + "jars";
			reportPath = System.getProperty("java.io.tmpdir")
					+ File.separatorChar + "reports";

		}
		sc = SemanticCore.getInstance(env);
	}

	public String getWebname() {
		return webname;
	}

	/**
	 * Servs the KnowWEArticle for a given article name
	 * 
	 * @param id
	 * @return
	 */
	public KnowWEArticle getArticle(String id) {
		return articleMap.get(id);
	}

	public Iterator<KnowWEArticle> getArticleIterator() {
		return articleMap.values().iterator();
	}

	public Collection<KnowWEArticle> getArticles() {
		return articleMap.values();
	}

	/**
	 * Replaces a KDOM-node with the given text
	 * 
	 * by now generates new KDOM from changed text TODO: implement proper
	 * algorithm to reuse old tree
	 * 
	 * @param map
	 * @param articleName
	 * @param nodeID
	 * @param text
	 * @return
	 */
	public String replaceKDOMNode(KnowWEParameterMap map, String articleName,
			String nodeID, String text) {
		// String user = map.getUser();
		String web = map.getWeb();
		KnowWEArticle art = this.getArticle(articleName);
		if (art == null)
			return "article not found: " + articleName;
		Section root = art.getSection();
		StringBuffer newText = new StringBuffer();
		appendTextReplaceNode(root, nodeID, text, newText);

		String newArticleSourceText = newText.toString();
		KnowWEEnvironment.getInstance().saveArticle(web, articleName,
				newArticleSourceText, map);
		saveUpdatedArticle(new KnowWEArticle(newArticleSourceText, articleName,
				KnowWEEnvironment.getInstance().getRootTypes(), this.webname));
		return "done";
	}

	/**
	 * Replaces a KDOM-node with the given text
	 * 
	 * by now generates new KDOM from changed text TODO: implement proper
	 * algorithm to reuse old tree
	 * 
	 * @param map
	 * @param articleName
	 * @param nodeID
	 * @param text
	 * @return
	 */
	public String replaceKDOMNodeWithoutSave(KnowWEParameterMap map,
			String articleName, String nodeID, String text) {
		KnowWEArticle art = this.getArticle(articleName);
		if (art == null)
			return "article not found: " + articleName;
		Section root = art.getSection();
		StringBuffer newText = new StringBuffer();
		appendTextReplaceNode(root, nodeID, text, newText);

		String newArticleSourceText = newText.toString();
		saveUpdatedArticle(new KnowWEArticle(newArticleSourceText, articleName,
				KnowWEEnvironment.getInstance().getRootTypes(), this.webname));
		return newArticleSourceText;
	}

	/**
	 * Looks in KDOM of given article for the Section object with given nodeID
	 * 
	 * @param articleName
	 * @param nodeID
	 * @return null if article or node not found
	 */
	public Section findNode(String articleName, String nodeID) {
		KnowWEArticle art = this.getArticle(articleName);
		if (art == null)
			return null;
		return art.findSection(nodeID);
	}

	private void appendTextReplaceNode(Section sec, String nodeID, String text,
			StringBuffer newText) {
		if (sec.getId().equals(nodeID)) {
			newText.append(text);
			return;
		}
		List<Section> children = sec.getChildren();
		if (children.size() == 0) {
			newText.append(sec.getOriginalText());
			return;
		}

		for (Section section : children) {
			appendTextReplaceNode(section, nodeID, text, newText);
		}
	}

	/**
	 * updates an article that has changed
	 * 
	 * 
	 * @param user
	 * @param text
	 * @param topic
	 * @return
	 */
	public KnowWEDomParseReport saveUpdatedArticle(KnowWEArticle art) {
		// store new article
		articleMap.put(art.getTitle(), art);

		sc.update(art.getTitle(), art);

		return art.getReport();
	}

	public List<String> getInitializingArticles() {
		return initializingArticles;
	}

	public void addInitializingArticle(String article) {
		this.initializingArticles.add(article);
	}

}
