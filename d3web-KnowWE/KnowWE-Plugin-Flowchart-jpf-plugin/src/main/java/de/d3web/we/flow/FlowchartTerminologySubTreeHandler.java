/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.diaFlux.IndicateFlowAction;
import de.d3web.diaFlux.NoopAction;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.IEdge;
import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.inference.FluxSolver;
import de.d3web.report.Message;
import de.d3web.we.flow.type.ExitType;
import de.d3web.we.flow.type.NodeType;
import de.d3web.we.flow.type.PositionType;
import de.d3web.we.flow.type.StartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.filter.SectionFilter;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.KDOMWarning;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.terminology.D3webSubtreeHandler;

/**
 * 
 * @author Reinhard Hatko Created on: 12.10.2009
 */
public class FlowchartTerminologySubTreeHandler extends D3webSubtreeHandler {

	private static final String FLOWCHART_QUESTIONNAIRE_NAME = "Questionnaire";
	public static final String STARTNODES_QUESTION_NAME = "Start";
	public static final String EXITNODES_QUESTION_NAME = "Exit";

	// TODO clean up: Just quick&dirty copied from original subtreehandler

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {

		KnowledgeBaseManagement kbm = getKBM(article);

		if (kbm == null) return null;

		Section content = ((AbstractXMLObjectType) s.getObjectType()).getContentChild(s);

		if (content == null) return null;

		List<Message> errors = new ArrayList<Message>();

		List<Section> sections = new ArrayList<Section>();

		content.findSuccessorsOfType(NodeType.class, sections);
		List<INode> nodes = createNodes(article, sections, errors);

		Map<String, String> attributeMap = AbstractXMLObjectType.getAttributeMapFor(s);

		String name = attributeMap.get("name");

		if (name == null || name.equals("")) name = "unnamed";

		String id = attributeMap.get("fcid");

		Flow flow = FlowFactory.getInstance().createFlow(id, name, nodes, new ArrayList<IEdge>());

		createTerminology(flow, kbm);

		List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();

		for (final Message message : errors) {
			msgs.add(new KDOMWarning() {

				@Override
				public String getVerbalization() {

					return message.getLineNo() + ": "
							+ message.getMessageText();
				}
			});
		}

		return msgs;

	}

	private void createTerminology(Flow flow, KnowledgeBaseManagement kbm) {

		QContainer flowQC;
		String name = flow.getName() + "_" + FLOWCHART_QUESTIONNAIRE_NAME;

		// reuse QContainer if already defined
		flowQC = kbm.findQContainer(name);

		if (flowQC == null) flowQC = kbm.createQContainer(name);

		QuestionMC startQ = createQuestion(flowQC, flow.getName() + "_" + STARTNODES_QUESTION_NAME,
				flow.getStartNodes(), kbm);
		createRules(flow, startQ, kbm);

		createQuestion(flowQC, flow.getName() + "_" + EXITNODES_QUESTION_NAME, flow.getExitNodes(),
				kbm);

		// flow.setTerminology(flowQC);

	}

	private void createRules(Flow flow, QuestionMC startQ, KnowledgeBaseManagement kbm) {

		for (Choice answer : startQ.getAlternatives()) {

			Rule rule = new Rule("FCIndication_" + startQ + "_" + answer.getName(),
					FluxSolver.class);

			rule.setAction(new IndicateFlowAction(flow.getName(), answer.getName()));
			rule.setCondition(new CondEqual(startQ, new ChoiceValue(answer)));

		}

	}

	private QuestionMC createQuestion(QContainer flowQC, String name,
			List<? extends Node> nodes, KnowledgeBaseManagement kbm) {

		String[] answers = new String[nodes.size()];

		for (int i = 0; i < answers.length; i++) {
			answers[i] = nodes.get(i).getName();
		}

		return kbm.createQuestionMC(name, flowQC, answers);

	}

	private List<INode> createNodes(KnowWEArticle article, List<Section> nodeSections, List<Message> errors) {

		List<INode> result = new ArrayList<INode>();

		for (Section section : nodeSections) {

			Section nodeContent = (Section) section.getChildren().get(1); // Section
																			// of
																			// NodeContentType

			// get the important info
			List<Section> children = nodeContent.getChildren(new SectionFilter() {

				public boolean accept(Section section) {
					return section.getObjectType() != PositionType.getInstance()
							&& section.getObjectType() != PlainText.getInstance();
				}
			});

			// Assert.assertEquals(section +
			// " has the wrong number of children.",1, children.size());

			if (children.size() != 1) continue;

			Section nodeinfo = children.get(0);

			String id = AbstractXMLObjectType.getAttributeMapFor(section).get("id");

			if (nodeinfo.getObjectType() == StartType.getInstance()) result.add(createStartNode(id,
					nodeinfo));
			else if (nodeinfo.getObjectType() == ExitType.getInstance()) result.add(createEndNode(
					id, nodeinfo));

		}

		return result;
	}

	private INode createEndNode(String id, Section section) {
		String name = ((Section) section.getChildren().get(1)).getOriginalText();

		return FlowFactory.getInstance().createEndNode(id, name, NoopAction.INSTANCE);
	}

	private INode createStartNode(String id, Section section) {
		String name = ((Section) section.getChildren().get(1)).getOriginalText();

		return FlowFactory.getInstance().createStartNode(id, name);

	}

}
