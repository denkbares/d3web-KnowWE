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
package de.d3web.we.action;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.Blackboard;
import de.d3web.core.session.blackboard.DefaultFact;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.DateValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.indication.inference.PSMethodUserSelected;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.event.EventManager;
import de.d3web.we.event.FindingSetEvent;
import de.d3web.we.utils.D3webUtils;

/**
 * Instead of {@link SetSingleFindingAction} this is used. But i am sure this is
 * not the proper Code. If you know how to implement this right. Do it. This
 * works for MC-Questions just like {@link SetSingleFindingAction}. What is with
 * QuestionOC?
 * 
 * @author Johannes Dienst
 * @created 14.10.2010
 */
public class ImageQuestionSetAction extends AbstractAction {

	@SuppressWarnings("unchecked")
	@Override
	public void execute(ActionContext context) throws IOException {

		@SuppressWarnings("deprecation")
		String namespace = java.net.URLDecoder.decode(context
				.getParameter(KnowWEAttributes.SEMANO_NAMESPACE));

		String objectid = context.getParameter(KnowWEAttributes.SEMANO_OBJECT_ID);
		String valueid = context.getParameter(KnowWEAttributes.SEMANO_VALUE_ID);
		String valuenum = context.getParameter(KnowWEAttributes.SEMANO_VALUE_NUM);
		String valuedate = context.getParameter(KnowWEAttributes.SEMANO_VALUE_DATE);
		String topic = context.getWikiContext().getTopic();
		String user = context.getWikiContext().getUserName();
		String web = context.getWikiContext().getWeb();

		if (namespace == null || objectid == null) {
			return;
		}

		KnowledgeBaseManagement kbm = D3webModule.getKnowledgeRepresentationHandler(web).getKBM(
					topic);
		Session session = D3webUtils.getSession(topic, user, web);
		Blackboard blackboard = session.getBlackboard();

		// Necessary for FindingSetEvent
		Question question = kbm.findQuestion(objectid);
		if (question != null) {

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

			// Some tests of values here
			if (value == null) return;
			if (!(question instanceof QuestionMC)) return;
			if (value.equals(Unknown.getInstance())) return;

			// Initialisation of needed Variables
			Fact mcFact = blackboard.getValueFact(question);
			Value answer = session.getBlackboard().getValue(question);
			Collection<ChoiceValue> mcValcol = null;
			final ArrayList<ChoiceValue> toAddList = new ArrayList<ChoiceValue>();
			if (answer != null && !(answer.getValue() instanceof String)) {
				mcValcol = (Collection<ChoiceValue>) answer.getValue();
			}

			/*
			 * the ChoiceValue is in the MultipleChoiceValue Remove it by
			 * deleting it from the Collection of the old MultipleChoiceValue
			 * and create a new one
			 */
			if (valueContained(answer, value)) {
				mcValcol.removeAll((Collection<ChoiceValue>)
								value.getValue());

				for (ChoiceValue val : mcValcol)
					toAddList.add(val);

				Value toAdd = new MultipleChoiceValue(toAddList);
				blackboard.removeValueFact(mcFact);
				blackboard.addValueFact(new DefaultFact(question,
								toAdd, PSMethodUserSelected.getInstance(),
										PSMethodUserSelected.getInstance()));

				EventManager.getInstance().fireEvent(
								new FindingSetEvent(question, value, namespace, web, user));
				return;
			}

			/*
			 * The ChoiceValue is not contained in the MultipleChoiceValue 1. If
			 * mcFact is not null: Add the ChoiceValue to a new
			 * MultipleChoiceValue by creating a new one containing also the old
			 * ones 2. If nothing was added before mcFact is null: Add it to
			 * blackboard
			 */
			if (!valueContained(answer, value)) {
				if (mcFact != null) {
					mcValcol.addAll((Collection<ChoiceValue>) value.getValue());

					for (ChoiceValue val : mcValcol)
						toAddList.add(val);
					Value toAdd = new MultipleChoiceValue(toAddList);

					blackboard.addValueFact(new DefaultFact(question,
									toAdd, PSMethodUserSelected.getInstance(),
											PSMethodUserSelected.getInstance()));
				}
				else {
					blackboard.addValueFact(new DefaultFact(question,
									value, PSMethodUserSelected.getInstance(),
											PSMethodUserSelected.getInstance()));
				}

				EventManager.getInstance().fireEvent(
								new FindingSetEvent(question, value, namespace, web, user));
			}

		}

	}

	/**
	 * Checks if <b>value</b> is contained in MultipleChoiceValue from
	 * <b>answer</b>.
	 * 
	 * @created 15.10.2010
	 * @param answer
	 * @param value
	 * @return
	 */
	private boolean valueContained(Value answer, Value value) {
		if ((answer != null) && !(answer.getValue() instanceof String)) {
			Set<Value> values = (Set<Value>) answer.getValue();
			return values.containsAll((Collection<ChoiceValue>)
					((MultipleChoiceValue) value).getValue());
		}
		return false;
	}
}
