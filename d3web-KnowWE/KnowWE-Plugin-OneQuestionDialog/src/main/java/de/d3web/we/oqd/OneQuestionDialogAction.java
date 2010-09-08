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
package de.d3web.we.oqd;

import java.io.IOException;

import de.d3web.core.knowledge.InterviewObject;
import de.d3web.core.session.Session;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.KnowWEParameterMap;

/**
 *
 * @author Florian Ziegler
 * @created 17.08.2010
 */
public class OneQuestionDialogAction extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {

		KnowWEParameterMap map = context.getKnowWEParameterMap();
		String web = map.getWeb();
		String topic = map.getTopic();
		String type = map.get("type");
		String question = map.get("question");
		String questionId = map.get("questionId");

		Session current = OneQuestionDialogUtils.getSession(topic, web, map.getWikiContext());

		context.setContentType("text/html; charset=UTF-8");

		// sometimes a request is sent with type, question and questionId ==
		// null - dont know why :/
		if (type == null) {
			return;
		}

		// if the normal button was hit, the next question will be shown
		if (type.equals("next")) {
			InterviewObject o = current.getInterview().nextForm().getInterviewObject();

			if (o == null) {
				// TODO move to bundle
				context.getWriter().write("Keine weiteren Fragen vorhanden");
			}
			else {
				OneQuestionDialogHistory.getInstance().addInterviewObject(o);
				context.getWriter().write(OneQuestionDialogUtils.createNewForm(o));
			}
		}
		// if the back button was hit, the previous question will be shown
		else if (type.equals("previous")) {
			InterviewObject o = OneQuestionDialogUtils.getPrevious(question, questionId);

			if (o == null) {
				return;
			}

			OneQuestionDialogHistory.getInstance().removeLast();
			context.getWriter().write(OneQuestionDialogUtils.createNewForm(o));
		}

	}

}
