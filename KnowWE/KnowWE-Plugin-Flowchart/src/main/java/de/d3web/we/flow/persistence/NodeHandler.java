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

package de.d3web.we.flow.persistence;

import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.diaFlux.flow.Node;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.flow.type.NodeType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;

/**
 * 
 * @author Reinhard Hatko
 * 
 */
public interface NodeHandler {

	/**
	 * Checks if this NodeHandler can create a node from the supplied section.
	 * 
	 * @param article the article which contains the section
	 * @param kb the KBM of the article
	 * @param nodeSection the section of the node
	 * @return true, if this nodehandler can create a node
	 */
	boolean canCreateNode(KnowWEArticle article, KnowledgeBase kb, Section<NodeType> nodeSection);

	/**
	 * Creates a node from the supplied section.
	 * 
	 * @param article
	 * @param kb
	 * @param nodeSection
	 * @param flowSection
	 * @param id id of the node to create
	 * @param errors
	 * @return
	 */
	Node createNode(KnowWEArticle article, KnowledgeBase kb, Section<NodeType> nodeSection, Section<FlowchartType> flowSection, String id, List<KDOMReportMessage> errors);

	/**
	 * Returns the ObjectType of the NodeModel this handler handles.
	 * 
	 * @return
	 */
	Type get();

}
