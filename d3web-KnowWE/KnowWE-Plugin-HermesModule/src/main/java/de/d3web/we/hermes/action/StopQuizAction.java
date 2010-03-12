package de.d3web.we.hermes.action;

import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.hermes.quiz.QuizSessionManager;
import de.d3web.we.hermes.taghandler.QuizHandler;

public class StopQuizAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public boolean isAdminAction() {
		return false;
	}

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		QuizSessionManager.getInstance().stopSession(parameterMap.getUser());
		return QuizHandler.renderQuizPanel(parameterMap.getUser(), QuizSessionManager.getInstance().getSession(parameterMap.getUser()));
		
	}

}
