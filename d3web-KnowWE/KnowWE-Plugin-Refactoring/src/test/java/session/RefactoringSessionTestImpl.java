package session;

import java.util.List;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.refactoring.session.RefactoringSession;

public class RefactoringSessionTestImpl extends RefactoringSession {
	
	private KnowWEParameterMap parameters;

	public RefactoringSessionTestImpl(KnowWEParameterMap params) {
		this.parameters = params;
	}

	@Override
	public String findNewName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends KnowWEObjectType> String findObjectID(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends KnowWEObjectType> String[] findObjectIDs(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends KnowWEObjectType> findRenamingType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void printExistingElements(List<Section<? extends KnowWEObjectType>> existingElements) {
		// TODO Auto-generated method stub

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
	protected String findRefactoringSourceCode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Section<?> findXCList() {
		// TODO Auto-generated method stub
		return null;
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
