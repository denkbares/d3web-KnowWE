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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.Node;
import de.d3web.we.flow.FlowchartSubTreeHandler;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.flow.type.NodeType;
import de.d3web.we.flow.type.SnapshotType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.xml.AbstractXMLType;

/**
 * @author Reinhard Hatko
 * @created 10.08.10
 * 
 */
public class SnapshotNodeHandler extends AbstractNodeHandler {

	public SnapshotNodeHandler() {
		super(SnapshotType.getInstance(), null);
	}

	public boolean canCreateNode(KnowWEArticle article, KnowledgeBase kb,
			Section<NodeType> nodeSection) {

		Section<AbstractXMLType> nodeInfo = getNodeInfo(nodeSection);

		return nodeInfo != null;

	}

	public Node createNode(KnowWEArticle article, KnowledgeBase kb, Section<NodeType> nodeSection,
			Section<FlowchartType> flowSection, String id, List<KDOMReportMessage> errors) {

		Section<AbstractXMLType> nodeInfo = getNodeInfo(nodeSection);
		String content = FlowchartSubTreeHandler.getXMLContentText(nodeInfo);

		if (content.length() > 10) content = content.substring(0, 10) + "...";

		return FlowFactory.getInstance().createSnapshotNode(id, content);

	}

}
