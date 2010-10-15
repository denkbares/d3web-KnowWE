/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.action;

import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.we.basic.WikiEnvironment;
import de.d3web.we.basic.WikiEnvironmentManager;
import de.d3web.we.basic.SessionBroker;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;

public class QuestionStateReportAction extends DeprecatedAbstractKnowWEAction {

	/**
	 * Used by GuidelineModul edit in GuidelineRenderer: Method:
	 * d3webVariablesScript Don´t change output syntax
	 */
	@SuppressWarnings("deprecation")
	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String namespace = java.net.URLDecoder.decode(parameterMap.get(KnowWEAttributes.SEMANO_NAMESPACE));
		String questionID = parameterMap.get(KnowWEAttributes.SEMANO_OBJECT_ID);
		String questionName = parameterMap.get(KnowWEAttributes.TERM);
		if (questionName == null) {
			questionName = parameterMap.get(KnowWEAttributes.SEMANO_TERM_NAME);
		}
		// String valueid = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_ID);
		// String valuenum =
		// parameterMap.get(KnowWEAttributes.SEMANO_VALUE_NUM);
		// String valueids =
		// parameterMap.get(KnowWEAttributes.SEMANO_VALUE_IDS);
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);

		String result = "Error on finding question state -wrong id/name ?";

		WikiEnvironment env = WikiEnvironmentManager.getInstance().getEnvironments(web);
		SessionBroker broker = env.getBroker(user);

		Session session = broker.getServiceSession(namespace);
		KnowledgeBase base = env.getService(namespace);
		Question q = null;

		if (questionID != null) {
			QASet set = base.searchQASet(questionID);
			if (set instanceof Question) {

				q = ((Question) set);
			}
		}

		if (questionName != null) {
			List<Question> questionList = base.getQuestions();
			for (Question question : questionList) {
				if (question.getName().equals(questionName)) {

					q = question;
				}
			}
		}

		// if(kss instanceof
		// de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession){
		//
		// }

		if (q != null) {

			List<? extends Question> answeredQuestions = session.getBlackboard().getAnsweredQuestions();
			if (answeredQuestions.contains(q)) {
				Value theanswer = session.getBlackboard().getValue(q);
				result = "#" + q.getName() + ":";
				if (theanswer != null) {
					result += theanswer.getValue().toString() + ";";
				}
				else {
					result += "no answer object";
				}
			}
			else {
				result = "undefined";
			}
		}

		return result;
	}

}
