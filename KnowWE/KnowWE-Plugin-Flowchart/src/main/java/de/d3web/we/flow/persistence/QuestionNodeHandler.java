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

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.INode;
import de.d3web.indication.ActionIndication;
import de.d3web.indication.ActionInstantIndication;
import de.d3web.indication.ActionNextQASet;
import de.d3web.report.Message;
import de.d3web.we.flow.FlowchartSubTreeHandler;
import de.d3web.we.flow.type.ActionType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * @author Reinhard Hatko
 * @created 10.08.10
 * 
 */
public class QuestionNodeHandler extends AbstractNodeHandler {

	public QuestionNodeHandler() {
		super(ActionType.getInstance(), "KnOffice");
	}

	public boolean canCreateNode(KnowWEArticle article,
			KnowledgeBaseManagement kbm, Section nodeSection) {

		Section<AbstractXMLObjectType> nodeInfo = getNodeInfo(nodeSection);

		if (nodeInfo == null) return false;

		String actionString = FlowchartSubTreeHandler.getXMLContentText(nodeInfo);

		return actionString.startsWith("ERFRAGE") || actionString.startsWith("INSTANT");

	}

	public INode createNode(KnowWEArticle article, KnowledgeBaseManagement kbm, Section nodeSection, Section flowSection, String id, List<Message> errors) {

		Section<AbstractXMLObjectType> nodeInfo = getNodeInfo(nodeSection);
		String actionString = FlowchartSubTreeHandler.getXMLContentText(nodeInfo);

		String name = actionString.substring(8, actionString.length() - 1);
		QASet findQuestion = kbm.findQuestion(name);

		if (findQuestion == null) {
			findQuestion = kbm.findQContainer(name);
		}

		if (findQuestion == null) {
			errors.add(new Message("Question not found: " + name));
			return null;
		}

		List<QASet> qasets = new ArrayList<QASet>();

		qasets.add(findQuestion);

		ActionNextQASet action;
		if (actionString.startsWith("ERFRAGE")) action = new ActionIndication();
		else action = new ActionInstantIndication();

		action.setQASets(qasets);

		return FlowFactory.getInstance().createActionNode(id, action);

	}

}
