package de.d3web.we.refactoring.management;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.dashTree.SubTree;
import de.d3web.we.kdom.include.Include;
import de.d3web.we.kdom.objects.QuestionID;

public class RefactoringManager {
	
	private static final String REFACTORING_WEB = "refactoring_web";

	private KnowWEArticleManager defaultWebManager;
	private KnowWEArticleManager refactoringWebManager;
	//private HashMap<String, KnowWEArticle> changedArticles = new HashMap<String, KnowWEArticle>();
	
	public RefactoringManager(String web) {
		this.defaultWebManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		this.refactoringWebManager = KnowWEEnvironment.getInstance().getArticleManager(REFACTORING_WEB);
		this.refactoringWebManager.clearArticleMap();
	}

	public Section<? extends KnowWEObjectType> findNode(String articleName, String nodeID) {
		//FIXME hier wurde getArticle zu getCachedArticle geändert um CoarsenValueRange zum laufen zu bekommen.
		KnowWEArticle art = this.getCachedArticle(articleName);
		if (art == null) {
			return null;
		} else {
			return art.findSection(nodeID);
		}
	}
	
	public Section<? extends KnowWEObjectType> findNode(String nodeID) {
		return findNode(getArticleName(nodeID), nodeID);
	}
	
	public String getArticleName(String sectionID) {
		String title;
		if (sectionID.contains("/")) {
			title = sectionID.substring(0, sectionID.indexOf("/"));
		} else {
			title = sectionID;
		}
		return title;
	}
	
	public Iterator<KnowWEArticle> getArticleIterator() {
		return getArticles().iterator();
	}
	
	public Collection<KnowWEArticle> getArticles() {
		HashMap<String, KnowWEArticle> mixedArticles = new HashMap<String, KnowWEArticle>();
		for (KnowWEArticle a: defaultWebManager.getArticles()) {
			mixedArticles.put(a.getTitle(), a);
		}
		for (KnowWEArticle a: refactoringWebManager.getArticles()) {
			mixedArticles.put(a.getTitle(), a);
		}
		return mixedArticles.values();
	}
	
	public Collection<KnowWEArticle> getChangedArticles() {
		return refactoringWebManager.getArticles();
	}
	
	public KnowWEArticle getArticle(String id) {
		KnowWEArticle article = refactoringWebManager.getArticle(id);
		return (article != null) ? article : defaultWebManager.getArticle(id);
	}
	
	public KnowWEArticle getCachedArticle(String id) {
		KnowWEArticle article = refactoringWebManager.getArticle(id);
		if (article != null) {
			return article;
		} else {
			article = defaultWebManager.getArticle(id);
			KnowWEArticle cachedArticle = new KnowWEArticle(article.getSection().getOriginalText(), article.getTitle(),
					article.getRootType(), REFACTORING_WEB);
			refactoringWebManager.saveUpdatedArticle(cachedArticle);
			return cachedArticle;
		}
	}
	
	public void replaceKDOMNode(String articleName, String nodeID, String text) {
		replaceKDOMNodeWithoutSave(articleName, nodeID, text);
		saveUpdatedArticle(this.getCachedArticle(articleName));
	}

	// ändert Artikelknoten, vorsicht: danach ist der KDOM inkonsistent, das Attribut dirty im Artikel müsste auf true stehen
	public void replaceKDOMNodeWithoutSave(String articleName, String nodeID, String text) {
		KnowWEArticle art = this.getCachedArticle(articleName);
		if (art == null) {
			throw new RuntimeException("RefactoringArticleManager.replaceKDOMNodeWithoutSave: Article " + articleName + " could not be found.");
		}
		Section<KnowWEArticle> root = art.getSection();
		root.setOriginalTextSetLeaf(nodeID, text);
	}
	
	// saves the article to a consistent state
	public void saveUpdatedArticle(KnowWEArticle art) {
		String articleName = art.getTitle();
		StringBuilder newArticleText = new StringBuilder();
		
		KnowWEArticle newArticle = art;
		if (refactoringWebManager.getArticle(articleName) != null) {
			refactoringWebManager.getArticle(articleName).getSection().collectTextsFromLeaves(newArticleText, false);
			newArticle = new KnowWEArticle(newArticleText.toString(), articleName, art.getRootType(), REFACTORING_WEB);
		}
		refactoringWebManager.saveUpdatedArticle(newArticle);
	}
	
	public Section<? extends KnowWEObjectType> findCachedQuestionToAnswer(Section<? extends KnowWEObjectType> answer) {
		return findNode(answer
		.findAncestorOfExactType(SubTree.class).findAncestorOfExactType(SubTree.class)
		.findChildOfType(DashTreeElement.class).findSuccessor(QuestionID.class).getId());
	}
}
