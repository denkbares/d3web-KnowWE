package de.d3web.we.hermes.action;

import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.hermes.quiz.QuizSessionManager;
import de.d3web.we.hermes.taghandler.QuizHandler;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public class StopQuizAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public boolean isAdminAction() {
		return false;
	}

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String kdomid = parameterMap.get("kdomid");
		Section<? extends KnowWEObjectType> sec = KnowWEEnvironment.getInstance().getArticleManager(
				KnowWEEnvironment.DEFAULT_WEB).findNode(kdomid);
		QuizSessionManager.getInstance().stopSession(parameterMap.getUser());
		return QuizHandler.renderQuizPanel(parameterMap.getUser(),
				QuizSessionManager.getInstance().getSession(parameterMap.getUser()),
				kdomid);

	}

}
