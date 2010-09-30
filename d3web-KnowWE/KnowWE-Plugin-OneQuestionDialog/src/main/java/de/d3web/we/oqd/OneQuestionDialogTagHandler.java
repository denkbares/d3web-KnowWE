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

import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.core.knowledge.InterviewObject;
import de.d3web.core.session.Session;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 *
 * @author Florian Ziegler
 * @created 16.08.2010
 */
public class OneQuestionDialogTagHandler extends AbstractTagHandler {

	public OneQuestionDialogTagHandler() {
		super("onequestiondialog");
		KnowWERessourceLoader.getInstance().add("onequestiondialog.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);
		KnowWERessourceLoader.getInstance().add("onequestiondialog.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {
		// D3webKnowledgeService knowledgeService =
		// D3webModule.getAD3webKnowledgeServiceInTopic(
		// web, topic);
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(user.getHttpRequest());

		// if the OQDialog is not in the main article (e.g. LeftMenu),
		// then no KB is found. Set to article.
		String articleName = user.getUrlParameterMap().get("page");
		if (!topic.equalsIgnoreCase(articleName)) {
			topic = articleName;
		}

		D3webKnowledgeService knowledgeServiceInTopic = D3webModule.getAD3webKnowledgeServiceInTopic(
				web, topic);
		if (knowledgeServiceInTopic == null) return rb.getString("KnowWE.quicki.error");

		Session current = OneQuestionDialogUtils.getSession(topic, web, user);

		InterviewObject o = current.getInterview().nextForm().getInterviewObject();

		OneQuestionDialogHistory.getInstance().addInterviewObject(o);

		String html = "<h3 class=\"oneQuestionDialog\">Dialog</h3>";

		if (o == null) {
			return html
					+ "<div class=\"oneQuestionDialog\">Keine weiteren Fragen vorhanden</div>";
		}

		return html + "<div class=\"oneQuestionDialog\">" + OneQuestionDialogUtils.createNewForm(o)
				+ "</div>";

	}

}
