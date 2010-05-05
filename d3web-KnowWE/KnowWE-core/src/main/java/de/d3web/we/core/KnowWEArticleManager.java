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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.include.Include;
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

	protected KnowWESectionInfoStorage typeStore = new KnowWESectionInfoStorage();

	public KnowWESectionInfoStorage getTypeStore() {
		return typeStore;
	}

	private final String web;
	private final SemanticCore sc;

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

		} else {
			jarsPath = System.getProperty("java.io.tmpdir")
					+ File.separatorChar + "jars";
			reportPath = System.getProperty("java.io.tmpdir")
					+ File.separatorChar + "reports";

		}
		sc = SemanticCore.getInstance(env);
	}

	public String getWebname() {
		return web;
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
	 * Replaces KDOM-nodes with the given texts, but not in the KDOM itself. It
	 * collects the originalTexts deep through the KDOM and appends the new text
	 * (instead of the originalText) for the nodes with an ID in the nodesMap.
	 * Finally the article is saved with this new content.
	 * 
	 * @param map
	 * @param articleName
	 * @param nodesMap
	 *            containing pairs of the nodeID and the new text for this node
	 * @return
	 */
	public String replaceKDOMNodes(KnowWEParameterMap map, String articleName,
			Map<String, String> nodesMap) {
		// String user = map.getUser();
		String web = map.getWeb();
		KnowWEArticle art = this.getArticle(articleName);
		if (art == null)
			return "article not found: " + articleName;
		Section<KnowWEArticle> root = art.getSection();
		StringBuffer newText = new StringBuffer();
		appendTextReplaceNode(root, nodesMap, newText);

		String newArticleSourceText = newText.toString();
		KnowWEEnvironment.getInstance().saveArticle(web, articleName,
				newArticleSourceText, map);
		// saveUpdatedArticle(new KnowWEArticle(newArticleSourceText,
		// articleName,
		// KnowWEEnvironment.getInstance().getRootTypes(), this.web));
		return "done";
	}

	/**
	 * Replaces KDOM-nodes with the given texts, but not in the KDOM itself. It
	 * collects the originalTexts deep through the KDOM and appends the new text
	 * (instead of the originalText) for the nodes with an ID in the nodesMap.
	 * 
	 * @param map
	 * @param articleName
	 * @param nodesMap
	 *            containing pairs of the nodeID and the new text for this node
	 * @return The content of the article with the text changes or an error
	 *         String if the article wasn't found.
	 */
	public String replaceKDOMNodesWithoutSave(KnowWEParameterMap map,
			String articleName, Map<String, String> nodesMap) {
		KnowWEArticle art = this.getArticle(articleName);
		if (art == null)
			return "article not found: " + articleName;
		Section root = art.getSection();
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
	 * @param articleName
	 * @param nodeID
	 * @return null if article or node not found
	 * @see findNode(String nodeID)
	 */
	public Section findNode(String articleName, String nodeID) {
		KnowWEArticle art = this.getArticle(articleName);
		if (art == null)
			return null;
		return art.findSection(nodeID);
	}

	/**
	 * Looks in KDOM for the Section object with given nodeID The article name
	 * is not needed because it is part of the nodeID
	 * 
	 * @param nodeID
	 * @return null if article or node not found
	 */
	public Section findNode(String nodeID) {
		String articleName;
		if (nodeID.contains("/")) {
			articleName = nodeID.substring(0, nodeID.indexOf("/"));
		} else {
			articleName = nodeID;
		}
		return findNode(articleName, nodeID);
	}

	private void appendTextReplaceNode(Section sec,
			Map<String, String> nodesMap, StringBuffer newText) {
		if (nodesMap.containsKey(sec.getId())) {
			newText.append(nodesMap.get(sec.getId()));
			return;
		}
		List<Section> children = sec.getChildren();
		if (children == null || children.isEmpty()
				|| sec.getObjectType() instanceof Include) {
			newText.append(sec.getOriginalText());
			return;
		}
		for (Section section : children) {
			appendTextReplaceNode(section, nodesMap, newText);
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

		// clear the semantic core of all statements from the article to clear
		// out remaining statements which don't have a section anymore
		SemanticCore.getInstance().cleanOrphans(art);
		// store new article
		articleMap.put(art.getTitle(), art);
		long startTime = System.currentTimeMillis();

		Logger.getLogger(this.getClass().getName()).log(
				Level.FINE,
				"-> Starting to update Includes to article '" + art.getTitle()
						+ "' ->");

		KnowWEEnvironment.getInstance().getIncludeManager(web)
				.updateIncludesToArticle(art);

		Logger.getLogger(this.getClass().getName()).log(
				Level.FINE,
				"<- Finished updating Includes to article '" + art.getTitle()
						+ "' in " + (System.currentTimeMillis() - startTime)
						+ "ms <-");

		art.getSection().setReusedStateRecursively(art.getTitle(), false);

		Logger.getLogger(this.getClass().getName()).log(
				Level.INFO,
				"<<==== Finished building article '" + art.getTitle() + "' in "
						+ web + " in "
						+ (System.currentTimeMillis() - art.getStartTime())
						+ "ms <<====");

		// commit all changes to the triplestore the article updating has
		// produced.
		// moved back to sectionwise updates due to a heisenbug
		// try {
		// SemanticCore.getInstance().getUpper().getConnection().commit();
		// } catch (RepositoryException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return art.getReport();
	}

	public void clearArticleMap() {
		this.articleMap = new java.util.HashMap<String, KnowWEArticle>();
	}

	/**
	 * Deletes the given article from the article map and invalidates all
	 * knowledge content that was in the article.
	 * 
	 * @param art
	 *            The article to delete
	 */
	public void deleteArticle(KnowWEArticle art) {
		KnowWEEnvironment.getInstance().processAndUpdateArticle("", "",
				art.getTitle(), web);
		SemanticCore.getInstance().clearContext(art);
		articleMap.remove(art.getTitle());

		Logger.getLogger(this.getClass().getName()).log(Level.INFO,
				"-> Deleted article '" + art.getTitle() + "'" + " from " + web);
	}

}
