/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

/**
 * 
 */
package de.d3web.we.flow.persistence;

import java.util.LinkedList;
import java.util.List;

import de.d3web.abstraction.ActionSetValue;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.INode;
import de.d3web.report.Message;
import de.d3web.we.flow.FlowchartSubTreeHandler;
import de.d3web.we.flow.FlowchartTerminologySubTreeHandler;
import de.d3web.we.flow.type.ExitType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * @author Reinhard Hatko
 * @created 10.08.10
 * 
 */
public class ExitNodeHandler extends AbstractNodeHandler {

	public ExitNodeHandler() {
		super(ExitType.getInstance(), null);
	}

	@Override
	public boolean canCreateNode(KnowWEArticle article,
			KnowledgeBaseManagement kbm, Section nodeSection) {

		Section<AbstractXMLObjectType> nodeInfo = getNodeInfo(nodeSection);

		return nodeInfo != null;
	}

	@Override
	public INode createNode(KnowWEArticle article, KnowledgeBaseManagement kbm,
			Section nodeSection, Section flowSection, String id, List<Message> errors) {

		String flowName = AbstractXMLObjectType.getAttributeMapFor(flowSection).get("name");

		Section<AbstractXMLObjectType> nodeInfo = getNodeInfo(nodeSection);

		String endNodeName = FlowchartSubTreeHandler.getXMLContentText(nodeInfo);

		ActionSetValue action = FlowFactory.getInstance().createSetValueAction();

		QuestionMC question = (QuestionMC) kbm.findQuestion(
				flowName + "_" + FlowchartTerminologySubTreeHandler.EXITNODES_QUESTION_NAME);

		action.setQuestion(question);

		Choice answer = null;
		for (Choice child : question.getAllAlternatives()) {
			if (child.getName().equals(endNodeName)) {
				answer = child;
				break;
			}

		}

		if (answer == null) {
			errors.add(new Message("No startnode  '" + flowName +
					"' not found in terminology of flow '" + flowName + "'."));
			return null;
		}

		// HOTFIX
		List<Choice> values = new LinkedList<Choice>();
		values.add(answer);

		action.setValue(MultipleChoiceValue.fromChoices(values));
		//

		return FlowFactory.getInstance().createEndNode(id, endNodeName, action);

	}

}
