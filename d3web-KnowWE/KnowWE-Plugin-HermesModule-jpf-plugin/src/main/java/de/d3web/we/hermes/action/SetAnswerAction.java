/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

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
