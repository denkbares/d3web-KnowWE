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

package de.knowwe.d3web.action;

import java.io.IOException;
import java.util.Collection;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.ValueUtils;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.blackboard.FactFactory;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.strings.Strings;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

public class SetSingleFindingAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String result = setValue(context);
		if (result != null && context.getWriter() != null) {
			context.getWriter().write(result);
		}

	}

	private String setValue(UserActionContext context) {

		String objectid = context.getParameter(Attributes.SEMANO_OBJECT_ID);
		String valuenum = context.getParameter(Attributes.SEMANO_VALUE_NUM);
		String valuedate = context.getParameter(Attributes.SEMANO_VALUE_DATE);
		String valueText = context.getParameter(Attributes.SEMANO_VALUE_TEXT);

		String sectionId = context.getParameter(Attributes.SECTION_ID);

		String web = context.getWeb();
		String namespace;
		String term;
		String valueid = null;
		namespace = Strings.decodeURL(context.getParameter(Attributes.SEMANO_NAMESPACE));
		String tempValueID = context.getParameter(Attributes.SEMANO_VALUE_ID);
		if (tempValueID != null) valueid = Strings.decodeURL(tempValueID);
		term = Strings.decodeURL(context.getParameter(Attributes.SEMANO_TERM_NAME));

		if (term != null && !term.equalsIgnoreCase("undefined")) {
			objectid = term;
		}

		if (namespace == null || objectid == null) {
			return null;
		}
		Section<?> section = Sections.get(sectionId);
		if (section == null || !KnowWEUtils.canView(section, context)) {
			return null;
		}
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(section);
		Session session = SessionProvider.getSession(context, kb);
		// Added for KnowWE-Plugin-d3web-Debugger
		if (context.getParameters().containsKey("KBid")) {
			String kbID = context.getParameter("KBid");
			Collection<KnowledgeBase> knowledgeBases = D3webUtils.getKnowledgeBases(KnowWEUtils.getArticleManager(web));
			for (KnowledgeBase knowledgeBase : knowledgeBases) {
				kb = knowledgeBase;
				if (kb.getId() != null && kb.getId().equals(kbID)) {
					session = SessionProvider.getSession(context, kb);
					break;
				}
			}
		}

		// Necessary for FindingSetEvent
		Question question = kb.getManager().searchQuestion(objectid);
		if (question != null) {

			Value value = null;
			if (valueid != null) {
				value = KnowledgeBaseUtils.findValue(question, valueid);
			}
			else if (valuenum != null) {
				try {
					value = new NumValue(Double.parseDouble(valuenum));
				}
				catch (NumberFormatException e) {
					// nothing to do, value will be null, field will be empty
				}
			}
			else if (valuedate != null) {
				try {
					value = ValueUtils.createDateValue((QuestionDate) question, valuedate);
				}
				catch (IllegalArgumentException e) {
					// nothing to do, value will be null, field will be empty
				}
			}
			else if (valueText != null) {
				value = new TextValue(valueText);
			}
			if (value != null) {
				// synchronize to session as suggested for multi-threaded
				// kernel access applications
				Fact fact = FactFactory.createUserEnteredFact(question, value);
				D3webUtils.setFindingSynchronized(fact, session, context);
			}
		}
		return null;
	}

}
