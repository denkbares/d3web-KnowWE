package de.d3web.we.hermes.quiz;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.KnowWEParameterMap;

public class SetAnswerAction implements KnowWEAction {

	@Override
	public boolean isAdminAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String user = parameterMap.getUser();
		String answerString = parameterMap.get("answer");
		int answer = -1;
		
		try {
		answer = Integer.parseInt(answerString);
		} catch (Exception e) {
			
		}
		
		QuizSessionManager.getInstance().setAnswer(parameterMap.getUser(),answer);
		return QuizHandler.renderQuizPanel(user, QuizSessionManager.getInstance().getSession(user));
	}

}
