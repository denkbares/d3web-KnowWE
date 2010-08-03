/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.flow.testcase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import de.d3web.core.session.values.Unknown;
import de.d3web.indication.inference.PSMethodUserSelected;
import de.d3web.we.action.AbstractAction;
import de.d3web.we.action.ActionContext;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.event.EventManager;
import de.d3web.we.event.FindingSetEvent;
import de.d3web.we.utils.D3webUtils;

/**
 * @author Florian Ziegler
 * @created 31.07.2010
 */
public class RunTestcaseAction extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {
		KnowWEParameterMap map = context.getKnowWEParameterMap();
		String web = map.getWeb();
		String topic = map.getTopic();
		String headerLine = map.get("headerLine");
		String currentLine = map.get("currentLine");
		
		String[] headerElements = headerLine.split(",.,");
		String[] currentElements = currentLine.split(",.,");

		String timestamp = currentElements[0];

		Map<String, String> testcaseMap = new HashMap<String, String>();
		for (int i = 0; i < headerElements.length; i++) {
			testcaseMap.put(headerElements[i], currentElements[i + 1]);
		}

		
		
		for (String questionName : testcaseMap.keySet()) {
			
			KnowledgeBaseManagement kbm = D3webModule.getKnowledgeRepresentationHandler(web).getKBM(topic);
			String user = context.getWikiContext().getUsername();
			Session session = D3webUtils.getSession(topic, user, web);
			Blackboard blackboard = session.getBlackboard();

			String valueString = testcaseMap.get(questionName);
			
			// Necessary for FindingSetEvent
			Question question = kbm.findQuestion(questionName);
			if (question == null) {
				System.out.println("Could not find Question '" + questionName +"'.");
				continue;
			}
			
			//TODO
			String namespace = "TODO";
			Value value = kbm.findValue(question, valueString);
			
			
			value = kbm.findValue(question, valueString);
			
			
			
			if (value == null) {
				System.out.println("Could not set value '" + valueString +"' on Question '" + questionName +"'.");
			}
			
			if (question instanceof QuestionMC && !value.equals(Unknown.getInstance())) {
				Fact mcFact = blackboard.getValueFact(question);
				if (mcFact != null && !mcFact.getValue().equals(Unknown.getInstance())) {
					MultipleChoiceValue mcv = ((MultipleChoiceValue) mcFact.getValue());
					List<ChoiceValue> thisMcv = (List<ChoiceValue>) ((MultipleChoiceValue) value).getValue();
					for (ChoiceValue cv : (List<ChoiceValue>) mcv.getValue()) {
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
					new FindingSetEvent(question, value, namespace),
					web, user, null);
			
			
		}
		
		
	}

}
