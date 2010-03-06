package session;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.refactoring.dialog.RefactoringSession;

public class RefactoringSessionTestImpl extends RefactoringSession {
	
	public RefactoringSessionTestImpl(KnowWEParameterMap params) {
		this.parameters = params;
	}

	@Override
	public void warning(String message) {
		System.out.println(message);

	}

	@Override
	protected void error(String message) {
		System.out.println(message);

	}
	
	@Override
	protected void saveAndFinish() {
		for (KnowWEArticle changedArticle: refManager.getChangedArticles()) {
			String changedArticleID = changedArticle.getTitle();
			KnowWEArticleManager knowWEManager = KnowWEEnvironment.getInstance().getArticleManager(parameters.getWeb());

			//der Artikel wird sicherheitshalber in einen konsistenten Zustand gebracht
			refManager.saveUpdatedArticle(changedArticle);
			KnowWEArticle consinstentChangedArticle = refManager.getArticle(changedArticleID);
			knowWEManager.replaceKDOMNode(parameters, changedArticleID, changedArticleID, consinstentChangedArticle.getSection().getOriginalText());
			
			//TODO wenn man einen neuen Artikel im knowWEManager anlegen möchte
//			//der Artikel wird sicherheitshalber in einen konsistenten Zustand gebracht
//			refManager.saveUpdatedArticle(changedArticle);
//			KnowWEArticle consinstentChangedArticle = refManager.getArticle(changedArticleID);
//			if (knowWEManager.getArticle(changedArticleID) != null) {
//				knowWEManager.replaceKDOMNode(parameters, changedArticleID, changedArticleID, consinstentChangedArticle
//						.getSection().getOriginalText());
//			} else {
//				knowWEManager.saveUpdatedArticle(changedArticle);
//			}

		}
	}
}
