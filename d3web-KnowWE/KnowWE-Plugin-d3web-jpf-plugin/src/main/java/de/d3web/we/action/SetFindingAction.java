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

package de.d3web.we.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.Blackboard;
import de.d3web.core.session.blackboard.DefaultFact;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.indication.inference.PSMethodUserSelected;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.basic.DPSEnvironmentManager;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.event.EventManager;
import de.d3web.we.event.FindingSetEvent;
import de.d3web.we.utils.D3webUtils;

public class SetFindingAction extends DeprecatedAbstractKnowWEAction {

	// public void perform(Model model) throws Exception {
	// Broker broker = KnowWEUtils.getBroker(model);
	// String namespace = java.net.URLDecoder.decode((String)
	// BasicUtils.getModelAttribute(model, KnowWEAttributes.SEMANO_NAMESPACE,
	// String.class, true));
	// String objectid = (String) BasicUtils.getModelAttribute(model,
	// KnowWEAttributes.SEMANO_OBJECT_ID, String.class, true);
	// String valueid = (String) BasicUtils.getModelAttribute(model,
	// KnowWEAttributes.SEMANO_VALUE_ID, String.class, true);
	// String valuenum = (String) BasicUtils.getModelAttribute(model,
	// KnowWEAttributes.SEMANO_VALUE_NUM, String.class, true);
	// String valueids = (String) BasicUtils.getModelAttribute(model,
	// KnowWEAttributes.SEMANO_VALUE_IDS, String.class, true);
	// if(namespace == null || objectid == null) {
	// return;
	// }
	// Map<String,String> m = new HashMap<String,String>();
	// m.put(KnowWEAttributes.SEMANO_NAMESPACE, namespace);
	// m.put(KnowWEAttributes.SEMANO_OBJECT_ID, objectid);
	// m.put(KnowWEAttributes.SEMANO_VALUE_ID, valueid);
	// m.put(KnowWEAttributes.SEMANO_VALUE_NUM, valuenum);
	// m.put(KnowWEAttributes.SEMANO_VALUE_IDS, valueids);
	// perform(m);
	//
	// }

	@SuppressWarnings( {
			"unchecked", "deprecation" })
	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String namespace = java.net.URLDecoder.decode(parameterMap.get(KnowWEAttributes.SEMANO_NAMESPACE));
		String objectid = parameterMap.get(KnowWEAttributes.SEMANO_OBJECT_ID);
		String valueid = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_ID);
		String valuenum = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_NUM);
		String valueids = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_IDS);
		String topic = parameterMap.getTopic();
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);

		if (namespace == null || objectid == null) {
			return "null";
		}

		// if DPS is inactive
		if (!ResourceBundle.getBundle("KnowWE_config").getString("dps.active").contains("true")) {
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

				List<Value> values = new ArrayList<Value>();
				if (valueids != null) {
					String[] ids = valueids.split("\\,");
					for (String string : ids) {
						values.add(kbm.findValue(question, string.trim()));
					}
				}
				Value singleValue = null;
				if (valueid != null) {
					singleValue = kbm.findValue(question, valueid);
				}
				else if (valuenum != null) {
					singleValue = new NumValue(Double.parseDouble(valuenum));
				}
				if (singleValue != null) values.add(singleValue);

				if (!values.isEmpty()) {
					for (Value value : values) {
						if (question instanceof QuestionMC && !value.equals(Unknown.getInstance())) {
							Fact mcFact = blackboard.getValueFact(question);
							if (mcFact != null && !mcFact.getValue().equals(Unknown.getInstance())) {
								MultipleChoiceValue mcv = ((MultipleChoiceValue) mcFact.getValue());
								Collection<ChoiceValue> thisMcv = (Collection<ChoiceValue>) ((MultipleChoiceValue) value).getValue();
								for (ChoiceValue cv : (Collection<ChoiceValue>) mcv.getValue()) {
									if (!thisMcv.contains(cv)) {
										thisMcv.add(cv);
									}
								}
							}
						}

						blackboard.addValueFact(new DefaultFact(question,
								value, PSMethodUserSelected.getInstance(),
										PSMethodUserSelected.getInstance()));

						EventManager.getInstance().fireEvent(
								new FindingSetEvent(question, value, namespace, web, user));
					}
				}
			}
		}
		// with active DPS
		else {

			DPSEnvironment env = DPSEnvironmentManager.getInstance().getEnvironments(web);
			Broker broker = env.getBroker(user);

			Object value = null;
			List<Object> values = new ArrayList<Object>();
			if (valueid != null) {
				value = valueid;
			}
			else if (valuenum != null && !valuenum.equals("")) {
				value = Double.valueOf(valuenum);
			}
			if (value != null) {
				values.add(value);
			}
			if (valueids != null) {
				String[] ids = valueids.split("\\,");
				for (String string : ids) {
					values.add(string.trim());
				}
			}

			KnowledgeServiceSession kss = broker.getSession().getServiceSession(namespace);

			Information info = new Information(namespace, objectid, values,
					TerminologyType.symptom, InformationType.OriginalUserInformation);
			kss.inform(info);
			broker.update(info);

		}
		return "value set";
	}

}
