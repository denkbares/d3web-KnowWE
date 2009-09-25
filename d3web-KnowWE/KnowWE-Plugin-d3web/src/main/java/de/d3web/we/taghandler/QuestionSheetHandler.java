/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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

package de.d3web.we.taghandler;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.supportknowledge.DCElement;
import de.d3web.kernel.supportknowledge.DCMarkup;
import de.d3web.kernel.supportknowledge.MMInfoObject;
import de.d3web.kernel.supportknowledge.MMInfoStorage;
import de.d3web.kernel.supportknowledge.MMInfoSubject;
import de.d3web.kernel.supportknowledge.Property;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.module.DefaultTextType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class QuestionSheetHandler  extends AbstractTagHandler{

	public QuestionSheetHandler() {
		super("QuestionSheet");
	}
	
	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.QuestionSheet.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
		D3webKnowledgeService service = D3webModule.getInstance().getAD3webKnowledgeServiceInTopic(web, topic);
		
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(user);
		
		StringBuffer html = new StringBuffer();
		html.append("<div id=\"questionsheet-panel\" class=\"panel\"><h3>" + rb.getString("KnowWE.QuestionSheet.header") + "</h3>");
			
		if(service != null) {
			KnowledgeBase kb = service.getBase();
			List<Question> questions = kb.getQuestions();
			
			html.append("<ul>");
			for (Question question : questions) {
				if(question.getProperties().getProperty(Property.ABSTRACTION_QUESTION) != null &&  question.getProperties().getProperty(Property.ABSTRACTION_QUESTION) instanceof Boolean && ((Boolean)question.getProperties().getProperty(Property.ABSTRACTION_QUESTION)).booleanValue()) {
					//dont show abstract questions
					continue;
				}
				MMInfoStorage storage = (MMInfoStorage)question.getProperties().getProperty(Property.MMINFO);
				DCMarkup markup = new DCMarkup();
		        markup.setContent(DCElement.SOURCE, question.getId());
		        markup.setContent(DCElement.SUBJECT, MMInfoSubject.PROMPT.getName());      
		        if(storage != null) {
		        	Set<MMInfoObject> o = storage.getMMInfo(markup);
		        }
				String rendered = DefaultTextType.getRenderedInput(question.getId(), question.getText(), service.getId(), user.getUsername(), "Question", question.getText(),"");
				html.append("<li><img src=\"KnowWEExtension/images/arrow_right.png\" border=\"0\"/>" + rendered + "</li> \n"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
			}
			html.append("</ul>");
		} else {
			html.append("<p class=\"box error\">" + rb.getString("KnowWE.QuestionSheet.error") + "</p>");
		}
		html.append("</div>");
		return html.toString();
	}
}
