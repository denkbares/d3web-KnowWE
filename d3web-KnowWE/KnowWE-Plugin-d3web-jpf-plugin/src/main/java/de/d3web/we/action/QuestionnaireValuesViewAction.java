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

import java.io.IOException;
import java.util.List;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.utils.D3webUtils;

/**
 * This action generates the result for the QuestionnaireValuesViewHandler
 * {@Link QuestionnaireValuesViewHandler}
 * 
 * @author Sebastian Furth
 * @created 06.06.2010
 */
public class QuestionnaireValuesViewAction extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {

		String questionnaireName = context.getParameter("questionnaire");
		String web = context.getParameter(KnowWEAttributes.WEB);
		StringBuilder result = new StringBuilder();

		for (KnowWEArticle article : KnowWEEnvironment.getInstance().getArticleManager(web).getArticles()) {
			Session session = D3webUtils.getSession(article.getTitle(), context.getWikiContext(),
					web);
			if (session != null) {
				IDObject io = session.getKnowledgeBase().searchObjectForName(questionnaireName);
				if (io instanceof QContainer) {
					QContainer questionnaire = (QContainer) io;
					for (TerminologyObject no : questionnaire.getChildren()) {
						if (no instanceof Question) {
							renderQuestion((Question) no, session, result);
						}
					}
			        context.setContentType("text/html; charset=UTF-8");
					context.getWriter().write(result.toString());
					return;
				}
			}
		}

		context.getWriter().write("Unknown Questionnaire: " + questionnaireName);

	}

	private void renderQuestion(Question question, Session session,
			StringBuilder result) {

		Value v = null;

		if (session.getBlackboard().getAnsweredQuestions().contains(question)) v = session.getBlackboard().getValue(
				question);

		result.append("<p>");
		result.append(question.getName());

		if (v instanceof ChoiceValue) {
			result.append(": ");
			result.append(v);
		}
		else if (v instanceof MultipleChoiceValue) {
			result.append(": ");
			List<ChoiceValue> cvs = (List<ChoiceValue>) ((MultipleChoiceValue) v.getValue()).getValue();
			for (ChoiceValue cv : cvs) {
				result.append(cv);
				result.append(", ");
			}
			result.delete(result.length() - 2, result.length());
		}
		else if (v instanceof NumValue) {
			result.append(": ");
			result.append(v.getValue());
		}
		result.append("</p>");
	}

}
