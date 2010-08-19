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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.d3web.core.knowledge.InterviewObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.session.Session;
import de.d3web.core.session.interviewmanager.Form;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;


/**
 * 
 * @author Florian Ziegler
 * @created 16.08.2010 
 */
public class OneQuestionDialogTagHandler extends AbstractTagHandler{

	/**
	 * @param name
	 */
	public OneQuestionDialogTagHandler() {
		super("onequestiondialog");
		KnowWERessourceLoader.getInstance().add("onequestiondialog.js", KnowWERessourceLoader.RESOURCE_SCRIPT);
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {
		D3webKnowledgeService knowledgeService = D3webModule.getAD3webKnowledgeServiceInTopic(
				web, topic);
		
		Session current = OneQuestionDialogUtils.getSession(topic, web);
		
		InterviewObject o = current.getInterview().nextForm().getInterviewObject();
		
		List<Choice> answers = OneQuestionDialogUtils.getAllAlternatives(o);
		
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"oneQuestionDialog\">");
		html.append("<form>");
		html.append("<p>");
		html.append(o.getName());
		html.append("<input type=\"hidden\" name=\"" + o.getId() + "\" value=\"" + o.getId() + "\">");
		html.append("</p>");
		html.append("<table>");
		for (Choice c : answers) {
			html.append("<tr>");
			html.append("<td>");
			html.append("<input type=\"checkbox\" value=\"" + c.getName() + "\">" + c.getName());
			html.append("<input type=\"hidden\" name=\"" + c.getId() + "\" value=\"" + c.getId() + "\">");
			html.append("</td>");
			html.append("</tr>");
		}
		html.append("<tr><td><button onclick=\"return OneQuestionDialog.sendQuestion(this)\">(+)</button></td></tr>");
		html.append("</table>");
		html.append("</form></div>");
		
		return html.toString();
	}
	


}
