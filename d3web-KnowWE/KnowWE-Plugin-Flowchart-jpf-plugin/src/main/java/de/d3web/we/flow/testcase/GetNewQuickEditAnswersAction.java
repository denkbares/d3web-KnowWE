/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.flow.testcase;

import java.io.IOException;
import java.util.List;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;

/**
 * @author Florian Ziegler
 * @created 04.07.2010
 */
public class GetNewQuickEditAnswersAction extends AbstractAction {

	@Override
	/**
	 * returns the new answers (separated by [:;:]) for a given headerElement if
	 * the question is mc/oc/yn or [:]EMPTY[:] if the question
	 * is of a different type
	 */
	public void execute(ActionContext context) throws IOException {
		KnowWEParameterMap map = context.getKnowWEParameterMap();
		String web = map.getWeb();
		String topic = map.getTopic();
		String element = context.getParameter("element");

		D3webKnowledgeService knowledgeService = D3webModule.getAD3webKnowledgeServiceInTopic(
				web, topic);
		List<Question> questions = knowledgeService.getBase().getQuestions();
		List<Solution> solutions = knowledgeService.getBase().getSolutions();

		boolean question = false;

		for (Question q : questions) {
			if (q.getName().equals(element)) {
				if (q instanceof QuestionYN) {
					context.getWriter().write("Yes[:;:]No[:;:]Unknown");
					question = true;
					break;
				}
				else if (q instanceof QuestionChoice) {
					StringBuffer buffy = new StringBuffer();
					for (Choice c : ((QuestionChoice) q)
							.getAllAlternatives()) {
						buffy.append(c.getName() + "[:;:]");
					}
					buffy.append("Unknown");
					context.getWriter().write(buffy.toString());
					question = true;
					break;
				}
				else {
					context.getWriter().write("[:]EMPTY[:]");
					question = true;
					break;
				}
			}
		}

		if (!question) {
			for (Solution s : solutions) {
				if (s.getName().equals(element)) {
					context.getWriter().write("established[:;:]suggested[:;:]excluded");
				}
			}
		}

	}

}
