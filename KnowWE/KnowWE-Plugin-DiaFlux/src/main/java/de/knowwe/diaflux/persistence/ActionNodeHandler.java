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
package de.knowwe.diaflux.persistence;

import de.d3web.core.inference.PSAction;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.diaFlux.flow.ActionNode;
import de.d3web.diaFlux.flow.Node;
import de.d3web.we.kdom.action.D3webRuleAction;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.diaflux.type.ActionType;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.NodeType;

/**
 * @author Reinhard Hatko
 * @created 10.08.10
 * 
 */
public class ActionNodeHandler extends AbstractNodeHandler<ActionType> {

	public ActionNodeHandler() {
		super(ActionType.getInstance());
	}

	@Override
	public boolean canCreateNode(D3webCompiler compiler, KnowledgeBase kb, Section<NodeType> nodeSection) {

		@SuppressWarnings("rawtypes")
		Section<D3webRuleAction> actionSection = Sections.successor(nodeSection,
				D3webRuleAction.class);

		return actionSection != null;
	}

	@Override
	public Node createNode(D3webCompiler compiler, KnowledgeBase kb, Section<NodeType> nodeSection,
			Section<FlowchartType> flowSection, String id) {

		@SuppressWarnings("rawtypes")
		Section<D3webRuleAction> ruleAction = Sections.successor(nodeSection,
				D3webRuleAction.class);

		@SuppressWarnings("unchecked")
		PSAction action = ruleAction.get().getAction(compiler, ruleAction);

		if (action == null) {
			return null;
		}

		return new ActionNode(id, action);
	}

}
