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
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.Node;
import de.d3web.we.flow.type.ActionType;
import de.d3web.we.flow.type.CallFlowActionType;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.flow.type.NodeType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.rules.action.D3webRuleAction;

/**
 * @author Reinhard Hatko
 * @created 10.08.10
 * 
 */
public class ActionNodeHandler extends AbstractNodeHandler {

	public ActionNodeHandler() {
		super(ActionType.getInstance());
	}

	@SuppressWarnings("unchecked")
	public boolean canCreateNode(KnowWEArticle article, KnowledgeBase kb, Section<NodeType> nodeSection) {

		Section<D3webRuleAction> actionSection = Sections.findSuccessor(nodeSection,
				D3webRuleAction.class);

		return actionSection != null
				&& actionSection.get().getClass() != CallFlowActionType.class;
	}

	@SuppressWarnings("unchecked")
	public Node createNode(KnowWEArticle article, KnowledgeBase kb, Section<NodeType> nodeSection,
			Section<FlowchartType> flowSection, String id, List<KDOMReportMessage> errors) {

		Section<D3webRuleAction> ruleAction = Sections.findSuccessor(nodeSection,
				D3webRuleAction.class);

		PSAction action = ruleAction.get().getAction(article, ruleAction);

		if (action == null) {
			return null;
		}

		return FlowFactory.getInstance().createActionNode(id, action);

	}

}
