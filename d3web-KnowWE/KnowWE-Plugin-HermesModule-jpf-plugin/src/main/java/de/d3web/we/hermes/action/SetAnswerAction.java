package de.d3web.we.hermes.action;

import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.hermes.quiz.QuizSessionManager;
import de.d3web.we.hermes.taghandler.QuizHandler;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public class SetAnswerAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public boolean isAdminAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		String kdomid = parameterMap.get("kdomid");
		Section<? extends KnowWEObjectType> sec = KnowWEEnvironment.getInstance().getArticleManager(
				KnowWEEnvironment.DEFAULT_WEB).findNode(kdomid);
		String user = parameterMap.getUser();
		String answerString = parameterMap.get("answer");
		int answer = -1;

		try {
			answer = Integer.parseInt(answerString);
		}
		catch (Exception e) {

		}

		QuizSessionManager.getInstance().setAnswer(parameterMap.getUser(), answer);
		return QuizHandler.renderQuizPanel(user,
				QuizSessionManager.getInstance().getSession(user), kdomid);
	}

}
