/*
 * Copyright (C) 2012 denkbares GmbH
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

package de.d3web.we.quicki;

import java.io.IOException;

import org.json.JSONObject;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.session.Session;
import de.d3web.interview.Interview;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author Benedikt Kaemmerer
 * @created 28.11.2012
 */

public class QuickInterviewGetChoiceAvailability extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String sectionId = context.getParameter(Attributes.SECTION_ID);
		String objectId = context.getParameter(Attributes.SEMANO_OBJECT_ID);
		Section<?> section = Sections.get(sectionId);
		if (KnowWEUtils.canView(section, context)) {
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(context, section);
			if (kb == null) return;

			Session session = SessionProvider.getSession(context, kb);
			if (session == null) return;

			QuestionChoice question = kb.getManager().search(objectId, QuestionChoice.class);
			if (question == null) return;

			JSONObject json = getChoiceAvailability(session, question);
			context.setContentType("application/json");
			context.getWriter().write(json.toString());
		}
	}

	private JSONObject getChoiceAvailability(Session session, QuestionChoice question) {
		Interview interview = Interview.get(session);
		JSONObject json = new JSONObject();
		for (Choice choice : question.getAllAlternatives()) {
			json.put(choice.getName(), interview.isAvailable(choice));
		}
		return json;
	}
}
