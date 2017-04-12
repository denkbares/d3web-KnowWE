/*
 * Copyright (C) 2010 denkbares GmbH, Wuerzburg
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
package cc.knowwe.dialog.action;

import java.io.IOException;
import java.util.Map;

import cc.knowwe.dialog.KeepAlive;
import cc.knowwe.dialog.SessionConstants;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.ValueUtils;
import de.d3web.core.session.blackboard.Blackboard;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.blackboard.FactFactory;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.we.basic.SessionProvider;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * This class serves as Action for retracting values of questions.
 *
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 09.12.2010
 */
public class RetractAnswer extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		for (Map.Entry<String, String> entry : context.getParameters().entrySet()) {
			String id = entry.getKey();
			String valueString = entry.getValue();
			// before answering we start a heart-beat on the
			// http response stream, to avoid timeouts
			KeepAlive heartbeat = new KeepAlive(context.getOutputStream());
			heartbeat.start();
			try {
				retractAnswer(context, id, valueString);
			}
			finally {
				heartbeat.terminate();
			}
		}
	}

	public void retractAnswer(UserActionContext context, String id, String valueString) {

		KnowledgeBase base = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);
		Session session = SessionProvider.getSession(context, base);
		Blackboard blackboard = session.getBlackboard();
		Question question = base.getManager().searchQuestion(id);
		Fact fact = blackboard.getValueFact(question);

		if (question instanceof QuestionChoice) {
			Choice choice = KnowledgeBaseUtils.findChoice(
					(QuestionChoice) question, valueString);
			Value value = null;
			if (choice == null) {
				if (Unknown.UNKNOWN_ID.equals(valueString)) {
					value = Unknown.getInstance();
				}
			}
			else {
				value = new ChoiceValue(choice);
			}
			if (fact.getValue().equals(value)) {
				blackboard.removeValueFact(fact);
			} else if (question instanceof QuestionMC) {
				blackboard.removeValueFact(fact);
				MultipleChoiceValue mcValue = ValueUtils.mergeChoiceValuesXOR((QuestionMC) question, fact.getValue(), value);
				fact = FactFactory.createUserEnteredFact(question, mcValue);
				blackboard.addValueFact(fact);
			}
		}
		session.touch();
	}
}
