package de.d3web.we.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.PlainText;
import de.d3web.we.kdom.Section;
import de.d3web.we.search.SearchEngine;
import de.d3web.we.kdom.store.KnowWESectionInfoStorage;
import de.d3web.we.utils.KnowWEUtils;

/**
 * @author Jochen
 * 
 * Manages all the articles of one web in a HashMap
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

	private String webname;
	private SemanticCore sc;

	public String jarsPath; 
	public String reportPath;
	
	public String getReportPath() {
		return reportPath;
	}

	private List<String> loopArticles = new ArrayList<String>();

	private static ResourceBundle rb = ResourceBundle
			.getBundle("WebParserConfig");

	public KnowWEArticleManager(KnowWEEnvironment env, String webname) {
		this.webname = webname;
		jarsPath = KnowWEUtils.getRealPath(env
				.getContext(), rb.getString("path_to_jars"));
		reportPath = KnowWEUtils.getRealPath(env.getContext(), rb
				.getString("path_to_reports"));
		sc=SemanticCore.getInstance(env);
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
		String user = map.getUser();
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
		saveUpdatedArticle(new KnowWEArticle(newArticleSourceText, articleName, KnowWEEnvironment
				.getInstance().getRootTypes(),this.webname));

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
	public String replaceKDOMNodeWithoutSave(KnowWEParameterMap map, String articleName, String nodeID, String text) {
		KnowWEArticle art = this.getArticle(articleName);
		if (art == null)
			return "article not found: " + articleName;
		Section root = art.getSection();
		StringBuffer newText = new StringBuffer();
		appendTextReplaceNode(root, nodeID, text, newText);

		String newArticleSourceText = newText.toString();
		saveUpdatedArticle(new KnowWEArticle(newArticleSourceText, articleName, KnowWEEnvironment
				.getInstance().getRootTypes(),this.webname));

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
		if (sec.getObjectType().equals(PlainText.getInstance())) {
			newText.append(sec.getOriginalText());
			return;
		}
		List<Section> children = sec.getChildren();
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
	
	public void addLoopArticles(List<String> loopArticles) {
		for (String art:loopArticles) {
			if (!this.loopArticles.contains(art)) {
				this.loopArticles.add(art);
			}
		}
	}
	
	public List<String> getLoopArticles() {
		return this.loopArticles;
	}
	
	public void updateLoopArticles() {
		List<String> loopCopy = new ArrayList<String>();
		loopCopy.addAll(this.loopArticles);
		for (String art:loopCopy) {
			this.loopArticles.remove(art);
			saveUpdatedArticle(new KnowWEArticle(KnowWEEnvironment.getInstance()
					.getWikiConnector().getArticleSource(art), art, KnowWEEnvironment
					.getInstance().getRootTypes(), loopArticles,this.webname));
		}
	}
	
	/**
	 * Creates and returns a new SearchEngine for the given articel manager.
	 * @return
	 */
	public SearchEngine getSearchEngine() {
		return new SearchEngine(this);
	}

}
