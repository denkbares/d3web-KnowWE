package de.d3web.we.hermes.quiz;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.KnowWEParameterMap;

public class StopQuizAction implements KnowWEAction{

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
