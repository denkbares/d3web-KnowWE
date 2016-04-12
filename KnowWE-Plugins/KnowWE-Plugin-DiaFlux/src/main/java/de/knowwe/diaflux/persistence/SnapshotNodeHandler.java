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
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.flow.SnapshotNode;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.diaflux.FlowchartSubTreeHandler;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.NodeType;
import de.knowwe.diaflux.type.SnapshotType;

/**
 * @author Reinhard Hatko
 * @created 10.08.10
 * 
 */
public class SnapshotNodeHandler extends AbstractNodeHandler<SnapshotType> {

	public SnapshotNodeHandler() {
		super(SnapshotType.getInstance(), null);
	}

	@Override
	public Node createNode(D3webCompiler compiler, KnowledgeBase kb, Section<NodeType> nodeSection,
			Section<FlowchartType> flowSection, String id) {

		Section<SnapshotType> nodeInfo = getNodeInfo(nodeSection);
		String content = FlowchartSubTreeHandler.getXMLContentText(nodeInfo);

		return new SnapshotNode(id, content);
	}

}
