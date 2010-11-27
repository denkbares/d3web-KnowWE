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

import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.flow.NOOPAction;
import de.d3web.we.flow.type.ActionType;
import de.d3web.we.flow.type.CallFlowActionType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.rules.action.D3webRuleAction;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * @author Reinhard Hatko
 * @created 10.08.10
 * 
 */
public class ActionNodeHandler extends AbstractNodeHandler {

	public ActionNodeHandler() {
		super(ActionType.getInstance());
	}

	public boolean canCreateNode(KnowWEArticle article,
			KnowledgeBaseManagement kbm, Section nodeSection) {

		Section<D3webRuleAction> actionSection = nodeSection.findSuccessor(D3webRuleAction.class);

		return actionSection != null
				&& actionSection.getObjectType().getClass() != CallFlowActionType.class;
	}

	public INode createNode(KnowWEArticle article, KnowledgeBaseManagement kbm,
			Section nodeSection, Section flowSection, String id, List<KDOMReportMessage> errors) {

		Section<AbstractXMLObjectType> actionSection = getNodeInfo(nodeSection);
		
		String markup = getMarkup(actionSection);
		
		// avoids the creation of an ActionIndication by the 
		// D3WebRuleActionParser
		if (markup.equalsIgnoreCase("NOP")) {
			// TODO problem because no Forward knowledge is present?
			return FlowFactory.getInstance().createActionNode(id, NOOPAction.INSTANCE);
		}

		Section<D3webRuleAction> ruleAction = nodeSection.findSuccessor(D3webRuleAction.class);


		PSAction action = ruleAction.get().getAction(article, ruleAction);

		if (action == null) {
			return null;
		}

		return FlowFactory.getInstance().createActionNode(id, action);

	}

}
