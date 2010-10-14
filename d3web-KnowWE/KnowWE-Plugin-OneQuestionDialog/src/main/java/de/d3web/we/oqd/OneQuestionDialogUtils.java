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

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.knowledge.InterviewObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.session.Session;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * 
 * @author Florian Ziegler
 * @created 16.08.2010
 */
public class OneQuestionDialogUtils {

	private static KnowWEUserContext kwuser;

	/**
	 * gets a session from a topic and a web
	 * 
	 * @created 31.08.2010
	 * @param topic
	 * @param web
	 * @return the session
	 */
	public static Session getSession(String topic, String web, KnowWEUserContext user) {

		kwuser = user;

		/*
		 * D3webKnowledgeService knowledgeService =
		 * D3webModule.getAD3webKnowledgeServiceInTopic( web, topic);
		 * 
		 * return SessionFactory.createSession(knowledgeService.getBase());
		 */

		D3webKnowledgeService knowledgeServiceInTopic = D3webModule.getAD3webKnowledgeServiceInTopic(
				web, topic);
		String kbid = knowledgeServiceInTopic.getId();

		Broker broker = D3webModule.getBroker(user.getUserName(), web);

		KnowledgeServiceSession serviceSession = broker.getSession()
				.getServiceSession(kbid);
		Session session = null;

		if (serviceSession instanceof D3webKnowledgeServiceSession) {

			session = ((D3webKnowledgeServiceSession) serviceSession).getSession();
			return session;
		}

		if (serviceSession == null) {
			kbid = KnowWEEnvironment.WIKI_FINDINGS + ".."
					+ KnowWEEnvironment.generateDefaultID(KnowWEEnvironment.WIKI_FINDINGS);
			serviceSession = broker.getSession().getServiceSession(kbid);
			if (serviceSession instanceof D3webKnowledgeServiceSession) {
				session = ((D3webKnowledgeServiceSession) serviceSession).getSession();
				return session;
			}
		}

		return null;
	}

	/**
	 * Returns all alternatives of an InterviewObject
	 * 
	 * @created 31.08.2010
	 * @param o
	 * @return the alternatives
	 */
	public static List<Choice> getAllAlternatives(InterviewObject object) {
		List<Choice> answers = new ArrayList<Choice>();

		if (object instanceof QuestionChoice) {
			answers = ((QuestionChoice) object).getAllAlternatives();
		}
		else if (object instanceof QuestionYN) {
			answers = ((QuestionYN) object).getAllAlternatives();
		}

		return answers;
	}

	/**
	 * Creates a new Form with question and answers from an InterviewObject
	 * 
	 * @created 31.08.2010
	 * @param o
	 * @return the new form
	 */
	public static String createNewForm(InterviewObject o) {
		String type = "";
		if (o instanceof QuestionOC) {
			type = "radio";
		}
		else if (o instanceof QuestionMC) {
			type = "checkbox";
		}
		else if (o instanceof QuestionNum) {
			type = "text";
		}

		List<Choice> answers = OneQuestionDialogUtils.getAllAlternatives(o);

		StringBuilder html = new StringBuilder();

		html.append("<div class='oqdheadline'>");

		html.append("<div class=\"oqdquestion\" id='oqdquestion'>");
		html.append(o.getName());
		html.append("<input type=\"hidden\" name=\"" + o.getId() + "\" value=\"" + o.getId()
				+ "\">");
		html.append("</div>");

		// MF: moved send button to head
		html.append("<div id=\"oqdbutton\" class=\"oqdbutton\" onclick=\"return OneQuestionDialog.sendQuestion(this)\" "
				+ "title='"
				+ D3webModule.getKwikiBundle_d3web(kwuser).getString("KnowWE.OQD.send")
				+ "'>");
		html.append("</div>");

		html.append("</div>");
		html.append("<table>");

		if (!type.equals("text")) {
			for (Choice c : answers) {
				html.append("<tr>");
				html.append("<td class=\"oqdanswer\">");
				html.append("<input type=\"" + type + "\" name=\"" + o.getName() + "\" value=\""
						+ c.getName() + "\">"
						+ c.getName());
				html.append("<input type=\"hidden\" name=\"" + c.getId() + "\" value=\""
						+ c.getId() + "\">");
				html.append("</td>");
				html.append("</tr>");
			}
		}
		else {
			html.append("<tr>");
			html.append("<td>");
			html.append("<input onkeypress=\"return OneQuestionDialog.submitOnEnter(this, event)\" type=\""
					+ type + "\">");
			html.append("</td>");
			html.append("</tr>");
		}
		/*
		 * html.append("<tr>"); html.append("<td>"); html.append(
		 * "<div id=\"oqdbutton\" class=\"oqdbutton\" onclick=\"return OneQuestionDialog.sendQuestion(this)\" "
		 * + "title='" +
		 * D3webModule.getKwikiBundle_d3web(kwuser).getString("KnowWE.OQD.send")
		 * + "'>"); html.append("</div>"); // html.append(
		 * "<div class=\"oqdprevious\" onclick=\"return OneQuestionDialog.getPrevious(this)\">"
		 * ); // html.append("</div>"); html.append("</td>");
		 * html.append("</tr>");
		 */
		html.append("</table>");

		return html.toString();
	}

	/**
	 * returns the previous InterviewObject from the history. Tries to match
	 * them via name and id.
	 * 
	 * @created 06.09.2010
	 * @param question name of the InterviewObject
	 * @param questionId id of the InterviewObject
	 * @return
	 */
	public static InterviewObject getPrevious(String question, String questionId) {
		List<InterviewObject> interviewObjects = OneQuestionDialogHistory.getInstance().getInterviewObjects();
		int pos = 0;

		for (InterviewObject o : interviewObjects) {
			if (o.getName().equals(question) && o.getId().equals(questionId)) {
				pos = interviewObjects.indexOf(o);
			}
		}
		if (pos > 0) {
			return interviewObjects.get(pos - 1);
		}
		else {
			return null;
		}
	}
}
