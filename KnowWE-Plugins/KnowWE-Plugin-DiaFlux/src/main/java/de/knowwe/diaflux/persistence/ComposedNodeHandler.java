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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.diaFlux.flow.ComposedNode;
import de.d3web.diaFlux.flow.Node;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.diaflux.type.ActionType;
import de.knowwe.diaflux.type.CallFlowActionType;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.NodeType;

/**
 * @author Reinhard Hatko
 * @created 10.08.10
 * 
 */
public class ComposedNodeHandler extends AbstractNodeHandler<ActionType> {

	public ComposedNodeHandler() {
		super(ActionType.getInstance(), "KnOffice");
	}

	@Override
	public boolean canCreateNode(D3webCompiler compiler, KnowledgeBase kb, Section<NodeType> nodeSection) {
		return super.canCreateNode(compiler, kb, nodeSection)
				&& Sections.successor(nodeSection, CallFlowActionType.class) != null;
	}

	@Override
	public Node createNode(D3webCompiler compiler, KnowledgeBase kb, Section<NodeType> nodeSection,
			Section<FlowchartType> flowSection, String id) {

		Section<CallFlowActionType> section =
				Sections.successor(nodeSection, CallFlowActionType.class);

		String flowName = CallFlowActionType.getFlowName(section);
		String nodeName = CallFlowActionType.getStartNodeName(section);

		return new ComposedNode(id, flowName, nodeName);
	}

}
