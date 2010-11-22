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

import java.io.IOException;
import java.util.List;

import de.d3web.core.io.utilities.Util;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.INode;
import de.d3web.report.Message;
import de.d3web.scoring.ActionHeuristicPS;
import de.d3web.scoring.Score;
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
public class DiagnosisNodeHandler extends AbstractNodeHandler {

	public DiagnosisNodeHandler() {
		super(ActionType.getInstance(), "KnOffice");
	}

	public boolean canCreateNode(KnowWEArticle article,
			KnowledgeBaseManagement kbm, Section nodeSection) {

		Section<AbstractXMLObjectType> nodeInfo = getNodeInfo(nodeSection);

		if (nodeInfo == null) return false;

		String actionString = FlowchartSubTreeHandler.getXMLContentText(nodeInfo);

		// TODO check if LHS of actionstring is diagnosis

		return actionString.contains("=");
	}

	public INode createNode(KnowWEArticle article, KnowledgeBaseManagement kbm,
			Section nodeSection, Section flowSection, String id, List<Message> errors) {

		Section<AbstractXMLObjectType> nodeInfo = getNodeInfo(nodeSection);
		String actionString = FlowchartSubTreeHandler.getXMLContentText(nodeInfo);

		if (!actionString.contains("=")) return null;

		String[] split = actionString.split("=");
		String solutionString = split[0].trim();
		String scoreString = split[1].trim();

		ActionHeuristicPS action = new ActionHeuristicPS();

		if (solutionString.startsWith("\"")) // remove "
		solutionString = solutionString.substring(1, solutionString.length() - 1);

		if (scoreString.startsWith("\"")) // remove "
		scoreString = scoreString.substring(1, scoreString.length() - 1);

		Solution solution = kbm.findSolution(solutionString);

		if (solution == null) {
			errors.add(new Message("Solution not found: " + solutionString));
			return null;
		}

		action.setSolution(solution);


		Score score = null;
		try {
			score = Util.getScore(scoreString);
		}
		catch (IOException e) {
			// happens only for pp-rules
		}

		if (score == null) {
			errors.add(new Message("Score not found: " + scoreString));
			return null;
		}

		action.setScore(score);

		return FlowFactory.getInstance().createActionNode(id, action);

	}

}
