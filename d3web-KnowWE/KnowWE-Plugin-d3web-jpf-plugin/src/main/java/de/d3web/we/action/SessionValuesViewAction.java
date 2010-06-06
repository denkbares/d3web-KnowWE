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

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.Session;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.utils.D3webUtils;

/**
 * This action generates the result for the SessionValuesViewHandler {@Link
 *  SessionValuesViewHandler}
 * 
 * @author Sebastian Furth
 * @created 06.06.2010
 */
public class SessionValuesViewAction extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {

		String topic = context.getParameter(KnowWEAttributes.TOPIC);
		String web = context.getParameter(KnowWEAttributes.WEB);
		String user = context.getParameter(KnowWEAttributes.USER);

		StringBuilder result = new StringBuilder();
		Session session = D3webUtils.getSession(topic, user, web);

		if (session != null) {
			result.append("<table><tr><td><b>Question</b></td><td><b>Value</b></td></tr>");
			for (Question q : session.getKnowledgeBase().getQuestions()) {
				result.append("<tr><td>");
				result.append(q.getName());
				result.append("</td><td>");
				result.append(session.getBlackboard().getValue(q));
				result.append("</td><tr>\n");
			}
			result.append("</table>");
		}
		else {
			result.append("This article's session is not accessible!");
		}

		context.getWriter().write(result.toString());
	}

}
