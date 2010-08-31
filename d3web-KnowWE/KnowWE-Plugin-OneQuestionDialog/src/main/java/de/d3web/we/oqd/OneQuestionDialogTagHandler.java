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

import de.d3web.core.knowledge.InterviewObject;
import de.d3web.core.session.Session;
import de.d3web.we.core.KnowWERessourceLoader;
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
	}

	@Override
	public String render(String topic, KnowWEUserContext user, Map<String, String> values, String web) {
		// D3webKnowledgeService knowledgeService =
		// D3webModule.getAD3webKnowledgeServiceInTopic(
		// web, topic);

		Session current = OneQuestionDialogUtils.getSession(topic, web);

		InterviewObject o = current.getInterview().nextForm().getInterviewObject();

		return "<div class=\"oneQuestionDialog\">" + OneQuestionDialogUtils.createNewForm(o)
				+ "</div>";

	}

}
