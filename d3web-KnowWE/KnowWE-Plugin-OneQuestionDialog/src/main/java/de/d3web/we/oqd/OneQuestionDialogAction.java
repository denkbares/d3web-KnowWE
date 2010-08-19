/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.oqd;

import java.io.IOException;

import de.d3web.core.knowledge.InterviewObject;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.core.session.blackboard.Blackboard;
import de.d3web.core.session.interviewmanager.NextUnansweredQuestionFormStrategy;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;


/**
 * 
 * @author Florian Ziegler
 * @created 17.08.2010 
 */
public class OneQuestionDialogAction extends AbstractAction{

	@Override
	public void execute(ActionContext context) throws IOException {
		
		KnowWEParameterMap map = context.getKnowWEParameterMap();
		String web = map.getWeb();
		String topic = map.getTopic();
		String question = map.get("question");
		String answers = map.get("answers");
		
		String[] answerArray = answers.split(",.,");
		
		D3webKnowledgeService knowledgeService = D3webModule.getAD3webKnowledgeServiceInTopic(
				web, topic);
		Session current = OneQuestionDialogUtils.getSession(topic, web);
		
		InterviewObject o = current.getInterview().nextForm().getInterviewObject();
		
		Blackboard blackboard = current.getBlackboard();
		
		context.getWriter().write("");
		
	}

}
