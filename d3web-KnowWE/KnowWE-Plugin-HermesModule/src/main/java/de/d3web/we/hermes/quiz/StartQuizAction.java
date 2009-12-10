package de.d3web.we.hermes.quiz;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.KnowWEParameterMap;

public class StartQuizAction implements KnowWEAction{

	@Override
	public boolean isAdminAction() {
		return false;
	}

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String user = parameterMap.getUser();
		QuizSessionManager.getInstance().createSession(user);
		return QuizHandler.renderQuizPanel(parameterMap.getUser(), QuizSessionManager.getInstance().getSession(user));
	}

}
