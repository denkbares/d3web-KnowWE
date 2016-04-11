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
package de.knowwe.diaflux.persistence;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.diaFlux.flow.ActionNode;
import de.d3web.diaFlux.flow.NOOPAction;
import de.d3web.diaFlux.flow.Node;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.NamedObjectReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.diaflux.type.DecisionType;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.NodeType;

/**
 * 
 * @author Reinhard Hatko
 * @created 28.11.2010
 */
public class DecisionNodeHandler extends AbstractNodeHandler<DecisionType> {

	public DecisionNodeHandler() {
		super(DecisionType.getInstance());

	}

	@Override
	public Node createNode(D3webCompiler compiler, KnowledgeBase kb, Section<NodeType> nodeSection,
			Section<FlowchartType> flowSection, String id) {

		Section<NamedObjectReference> objectRef = Sections.successor(nodeSection,
				NamedObjectReference.class);
		NamedObject object = NamedObjectReference.getObject(compiler, objectRef);
		NOOPAction action;
		if (object instanceof TerminologyObject) {
			// only references to Solutions and Questions can be modelled
			action = new NOOPAction((TerminologyObject) object);
		}
		else {
			action = new NOOPAction();
		}
		return new ActionNode(id, action);

	}

}
