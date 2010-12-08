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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.Blackboard;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.blackboard.FactFactory;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.TextValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.event.EventManager;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.d3web.event.FindingSetEvent;

public class SetSingleFindingAction extends DeprecatedAbstractKnowWEAction {

	@SuppressWarnings({ "deprecation" })
	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String namespace = java.net.URLDecoder.decode(parameterMap
				.get(KnowWEAttributes.SEMANO_NAMESPACE));

		String objectid = parameterMap.get(KnowWEAttributes.SEMANO_OBJECT_ID);
		String valueid = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_ID);
		String valuenum = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_NUM);
		String valuedate = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_DATE);
		String valueText = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_TEXT);
		String topic = parameterMap.getTopic();
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);

		if (namespace == null || objectid == null) {
			return "null";
		}

		KnowledgeBaseManagement kbm = D3webModule.getKnowledgeRepresentationHandler(web).getKBM(
					topic);
		Session session = D3webUtils.getSession(topic, user, web);
		Blackboard blackboard = session.getBlackboard();

		// Necessary for FindingSetEvent
		Question question = kbm.findQuestion(objectid);
		if (question != null) {

			// update: resetting the value is unnecessary --rh@100903
			// reset choices in case the selection changed
			// (user removed choices)
			// blackboard.addValueFact(new DefaultFact(question,
			// Unknown.getInstance(), PSMethodUserSelected.getInstance(),
			// PSMethodUserSelected.getInstance()));
			//

			Value value = null;
			if (valueid != null) {
				value = kbm.findValue(question, valueid);
			}
			else if (valuenum != null) {
				value = new NumValue(Double.parseDouble(valuenum));

				// TODO set valuedate in Attributes
			}
			else if (valuedate != null) {
				final DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				try {
					Date date = format.parse(valuedate);
					value = new DateValue(date);
				}
				catch (ParseException e) {
					e.printStackTrace();
				}
			}
			else if (valueText != null) {
				value = new TextValue(valueText);
			}
			if (value != null) {
				if (question instanceof QuestionMC && !value.equals(Unknown.getInstance())) {

					/*
					 * if (mcFact != null &&
					 * !mcFact.getValue().equals(Unknown.getInstance())) {
					 * MultipleChoiceValue mcv = ((MultipleChoiceValue)
					 * mcFact.getValue()); Collection<ChoiceValue> thisMcv =
					 * (Collection<ChoiceValue>) ((MultipleChoiceValue)
					 * value).getValue(); for (ChoiceValue cv :
					 * (Collection<ChoiceValue>) mcv.getValue()) { if
					 * (!thisMcv.contains(cv)) { thisMcv.add(cv); } } }
					 */

					Fact mcFact = blackboard.getValueFact(question);

					// remove old mc value if any

					// vb: should be comment out, see below:
					// that is wrong and even not possible
					// because this may be a merged fact.
					// old facts are automatically overwrtitten
					// by setting new ones from the same source!
					// if (mcFact != null) {
					// blackboard.removeValueFact(mcFact);
					//
					// }
				}

				// synchronize to session as suggested for multi-threaded
				// kernel access applications
				synchronized (session) {
					Fact fact = FactFactory.createUserEnteredFact(question, value);
					blackboard.addValueFact(fact);
				}
				EventManager.getInstance().fireEvent(
						new FindingSetEvent(question, value, namespace, web, user));
			}
		}
		return null;
	}

}
