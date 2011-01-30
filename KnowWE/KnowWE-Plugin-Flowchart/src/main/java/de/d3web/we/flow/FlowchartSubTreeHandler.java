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

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.FlowSet;
import de.d3web.diaFlux.flow.IEdge;
import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.inference.ConditionTrue;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.diaFlux.io.DiaFluxPersistenceHandler;
import de.d3web.we.flow.persistence.NodeHandler;
import de.d3web.we.flow.persistence.NodeHandlerManager;
import de.d3web.we.flow.type.EdgeContentType;
import de.d3web.we.flow.type.EdgeType;
import de.d3web.we.flow.type.FlowchartType;
import de.d3web.we.flow.type.GuardType;
import de.d3web.we.flow.type.NodeType;
import de.d3web.we.flow.type.OriginType;
import de.d3web.we.flow.type.PositionType;
import de.d3web.we.flow.type.TargetType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.report.SyntaxError;
import de.d3web.we.kdom.report.message.NoSuchObjectError;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.subtreeHandler.ConstraintModule;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;

/**
 * 
 * 
 * @author Reinhard Hatko
 * @created on: 12.10.2009
 */
public class FlowchartSubTreeHandler extends D3webSubtreeHandler<FlowchartType> {

	private final List<Class<? extends KnowWEObjectType>> filteredTypes =
			new ArrayList<Class<? extends KnowWEObjectType>>(0);

	public FlowchartSubTreeHandler() {
		this.registerConstraintModule(new FlowchartConstraintModule());
		filteredTypes.add(PositionType.class);
	}

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<FlowchartType> s) {

		if (!article.isFullParse()) destroy(article, s);

		KnowledgeBaseManagement kbm = getKBM(article);
		Section<XMLContent> flowcontent = ((AbstractXMLObjectType) s.getObjectType()).getContentChild(s);

		if (kbm == null || flowcontent == null) {
			return null;
		}

		Map<String, String> attributeMap = AbstractXMLObjectType.getAttributeMapFor(s);
		String name = attributeMap.get("name");
		String id = attributeMap.get("fcid");
		boolean autostart = Boolean.parseBoolean(attributeMap.get("autostart"));

		if (name == null || name.equals("")) {
			name = "unnamed";
		}

		List<KDOMReportMessage> errors = new ArrayList<KDOMReportMessage>();

		List<INode> nodes = createNodes(article, name, s, errors);

		List<IEdge> edges = createEdges(article, s, nodes, errors);

		Flow flow = FlowFactory.getInstance().createFlow(id, name, nodes, edges);
		flow.setAutostart(autostart);
		flow.setOrigin(s.getID());

		DiaFluxUtils.addFlow(flow, kbm.getKnowledgeBase());

		return errors;
	}

	@Override
	public void destroy(KnowWEArticle article, Section<FlowchartType> s) {
		FlowSet flowSet = DiaFluxUtils.getFlowSet(getKBM(article).getKnowledgeBase());
		Map<String, String> attributeMap = AbstractXMLObjectType.getLastAttributeMapFor(s);
		if (flowSet != null && attributeMap != null) {
			flowSet.remove(attributeMap.get("fcid"));
		}
	}

	private class FlowchartConstraintModule extends ConstraintModule<FlowchartType> {

		@Override
		public boolean violatedConstraints(KnowWEArticle article, Section<FlowchartType> s) {
			return s.isOrHasChangedSuccessor(article.getTitle(), filteredTypes);
		}

	}

	@SuppressWarnings("unchecked")
	private List<IEdge> createEdges(KnowWEArticle article, Section<FlowchartType> flowSection, List<INode> nodes, List<KDOMReportMessage> errors) {
		List<IEdge> result = new ArrayList<IEdge>();

		List<Section<EdgeType>> edgeSections = new ArrayList<Section<EdgeType>>();
		Section<XMLContent> flowcontent = ((AbstractXMLObjectType) flowSection.getObjectType()).getContentChild(flowSection);
		flowcontent.findSuccessorsOfType(EdgeType.class, edgeSections);

		for (Section<EdgeType> section : edgeSections) {

			String id = AbstractXMLObjectType.getAttributeMapFor(section).get("fcid");
			Section<EdgeContentType> content = (Section<EdgeContentType>) section.getChildren().get(
					1);

			Section<OriginType> originSection = content.findChildOfType(OriginType.class);

			// TODO remove duplicate code
			if (originSection == null) {
				String messageText = "No origin node specified in edge with id '" + id + "'.";

				errors.add(new SyntaxError(messageText));
				continue;
			}
			String sourceID = getXMLContentText(originSection);

			INode source = DiaFluxPersistenceHandler.getNodeByID(sourceID, nodes);

			if (source == null) {
				String messageText = "No origin node found with id " + sourceID + " in edge " + id
						+ ".";

				errors.add(new NoSuchObjectError(messageText));
				continue;
			}

			Section<TargetType> targetSection = content.findChildOfType(TargetType.class);

			if (targetSection == null) {
				String messageText = "No target node specified in edge with id '" + id + "'.";

				errors.add(new SyntaxError(messageText));
				continue;
			}

			String targetID = getXMLContentText(targetSection);

			INode target = DiaFluxPersistenceHandler.getNodeByID(targetID, nodes);

			if (target == null) {
				String messageText = "No target node found with id " + targetID + " in edge " + id
						+ ".";
				errors.add(new NoSuchObjectError(messageText));
				continue;
			}

			Condition condition;

			Section<GuardType> guardSection = content.findChildOfType(GuardType.class);
			if (guardSection != null) {
				Section<CompositeCondition> compositionConditionSection = (Section<CompositeCondition>) guardSection.getChildren().get(
						1);
				condition = buildCondition(article, compositionConditionSection, errors);

				if (condition == null) {
					condition = ConditionTrue.INSTANCE;

					errors.add(new ObjectCreationError("Could not parse condition: "
							+ getXMLContentText(guardSection), getClass()));

				}

			}
			else {
				condition = ConditionTrue.INSTANCE;
			}

			IEdge edge = FlowFactory.getInstance().createEdge(id, source, target, condition);

			result.add(edge);

		}

		return result;
	}

	private Condition buildCondition(KnowWEArticle article, Section<CompositeCondition> s, List<KDOMReportMessage> errors) {

		return KDOMConditionFactory.createCondition(article, s);
	}

	@SuppressWarnings("unchecked")
	public static String getXMLContentText(Section<? extends AbstractXMLObjectType> s) {
		String originalText = ((Section<XMLContent>) s.getChildren().get(1)).getOriginalText();
		// return StringEscapeUtils.unescapeXml(originalText);
		return originalText;
	}

	@SuppressWarnings("unchecked")
	private List<INode> createNodes(KnowWEArticle article, String flowName, Section<FlowchartType> flowSection, List<KDOMReportMessage> errors) {

		List<INode> result = new ArrayList<INode>();
		ArrayList<Section<NodeType>> nodeSections = new ArrayList<Section<NodeType>>();
		Section<XMLContent> flowcontent = ((AbstractXMLObjectType) flowSection.getObjectType()).getContentChild(flowSection);
		flowcontent.findSuccessorsOfType(NodeType.class, nodeSections);

		KnowledgeBaseManagement kbm = getKBM(article);

		for (Section<NodeType> nodeSection : nodeSections) {

			NodeHandler handler = NodeHandlerManager.getInstance().findNodeHandler(article, kbm,
					nodeSection);

			if (handler == null) {
				errors.add(new SimpleMessageError("No NodeHandler found for: "
						+ nodeSection.getOriginalText()));
				continue;
			}

			else {// handler can in general handle NodeType
				String id = AbstractXMLObjectType.getAttributeMapFor(nodeSection).get("fcid");

				INode node = handler.createNode(article, kbm, nodeSection, flowSection, id, errors);

				if (node != null) {
					result.add(node);
				}
				else { // handler could not generate a node for the supplied
						// section
					Section<AbstractXMLObjectType> nodeInfo = (Section<AbstractXMLObjectType>) nodeSection.findSuccessor(handler.getObjectType().getClass());
					String text = getXMLContentText(nodeInfo);

					errors.add(new ObjectCreationError("NodeHandler "
							+ handler.getClass().getSimpleName() + " could not create node for: "
							+ text, getClass()));

					result.add(FlowFactory.getInstance().createCommentNode(id,
							"Surrogate for node of type " + text));
				}

			}

		}

		return result;
	}

}
