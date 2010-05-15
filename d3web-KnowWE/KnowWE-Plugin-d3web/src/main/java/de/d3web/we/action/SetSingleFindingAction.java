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

package de.d3web.we.action;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import common.Logger;

import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.session.values.AnswerNum;
import de.d3web.core.session.values.Choice;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEFacade;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.d3webModule.DPSEnvironmentManager;
import de.d3web.we.event.EventManager;
import de.d3web.we.event.FindingSetEvent;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.utils.D3webUtils;

public class SetSingleFindingAction extends DeprecatedAbstractKnowWEAction {

	@SuppressWarnings("deprecation")
	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String namespace = java.net.URLDecoder.decode(parameterMap
				.get(KnowWEAttributes.SEMANO_NAMESPACE));
		String objectid = parameterMap.get(KnowWEAttributes.SEMANO_OBJECT_ID);
		String termName = URLDecoder.decode(parameterMap
				.get(KnowWEAttributes.SEMANO_TERM_NAME));
		String valueid = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_ID);
		String valuenum = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_NUM);
		String valueids = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_IDS);
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);

		
		
		DPSEnvironment env = DPSEnvironmentManager.getInstance()
				.getEnvironments(web);
		Broker broker = env.getBroker(user);

		if (namespace == null || objectid == null) {
			return "null";
		}
		// Object value = null;
		// List<Object> values = new ArrayList<Object>();
		// if(valueid != null) {
		// value = valueid;
		// } else if(valuenum != null) {
		// value = Double.valueOf(valuenum);
		// }
		// if(value != null) {
		// values.add(value);
		// }
		// if(valueids != null) {
		// String[] ids = valueids.split("\\,");
		// for (String string : ids) {
		// values.add(string.trim());
		// }
		// }
		
		
		
		
		Term term = null;
		term = SemanticAnnotationAction.getTerm(env, termName);
		
		//workaround TODO refactor
		if(term == null) {
			KnowledgeServiceSession kss = broker.getSession()
			.getServiceSession(namespace);
			if (kss instanceof D3webKnowledgeServiceSession) {
				Question q = ((D3webKnowledgeServiceSession)kss).getBaseManagement().getKnowledgeBase().searchQuestion(objectid);
				if(q != null) {
					termName = q.getName();
					term = SemanticAnnotationAction.getTerm(env, termName);
				}
			}
		}

		IdentifiableInstance ii = null;
		if (term != null) {
			ii = SemanticAnnotationAction.getII(env, namespace, term);
		}
		if (ii == null) {
			return "Question not found in KB: " + termName;
		}

		List<String> answers = new ArrayList<String>();

		String qid = null;

		KnowledgeServiceSession kss = broker.getSession().getServiceSession(
				namespace);
		
		List<Information> userInfos = broker.getSession().getBlackboard()
				.getOriginalUserInformation();
		for (Information information : userInfos) {
			IdentifiableInstance iio = information
					.getIdentifiableObjectInstance();
			if (!iio.equals(ii))
				continue;
			qid = iio.getObjectId();
			Collection<IdentifiableInstance> iivs = information
					.getIdentifiableValueInstances();
			if (iivs.isEmpty())
				break;
			Iterator<IdentifiableInstance> iter = iivs.iterator();

			while (iter.hasNext()) {
				IdentifiableInstance iiv = iter.next();
				Object val = iiv.getValue();
				if (val instanceof String) {
					answers.add((String) val);
				}
			}
		}
		
		//HOTFIX for answer not set in mc-question.
		// Occurred just once, dont know why exactly.
		if (qid == null) 
			qid = objectid;

		// Necessary for FindingSetEvent
		Question question = D3webUtils.getQuestion(kss, qid);
		Answer answer = null;
		
		// We need the Answer (Choice) Object for the FindingSetEvent
		if (question instanceof QuestionChoice) {
			for (Choice choice : ((QuestionChoice) question).getAllAlternatives()) {
				if (choice.getId().equals(valueid)) {
					answer = choice;
					break;
				}
			}
		}
		
		boolean contains = false;
		boolean mc = (question instanceof QuestionMC);
		for (String a : answers) {
			if (a.equals(valueid)) {
				contains = true;
			}
		}
		List<Object> valuesAfterClick = new ArrayList<Object>();
		if (mc) {
			if (!contains) {
				for (String a : answers) {
					valuesAfterClick.add(a);
				}
				valuesAfterClick.add(valueid.trim());
			} else {
				for (String a : answers) {
					if (!a.equals(valueid))
						valuesAfterClick.add(a);
				}
			}

		} else {
			if (valuenum != null) {
				try {
					Double doubleValue = Double.valueOf(valuenum);
					valuesAfterClick.add(doubleValue);
					// Necessary for FindingSetEvent
					answer = new AnswerNum();
					((AnswerNum) answer).setValue(doubleValue);
					
				} catch (NumberFormatException e) {
				}
				
			} else {
				valuesAfterClick.add(valueid.trim());
			}
		}
		
		EventManager.getInstance().fireEvent(user, null, new FindingSetEvent(question, answer));

		Information info = new Information(namespace, objectid,
				valuesAfterClick, TerminologyType.symptom,
				InformationType.OriginalUserInformation);
		kss.inform(info);
		broker.update(info);

		try {
			KnowWEFacade.getInstance().performAction("RefreshHTMLDialogAction", parameterMap);
		} catch (IOException e) {
			Logger.getLogger(this.getClass()).error("Error while performing RefreshHTMLDialogAction" + e.getMessage());
		}
		
		return null;
	}

}
