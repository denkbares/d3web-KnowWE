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

package de.knowwe.diaflux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.diaFlux.flow.CommentNode;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.inference.ConditionTrue;
import de.d3web.diaFlux.io.DiaFluxPersistenceHandler;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.compile.ConstraintModule;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.diaflux.persistence.NodeHandler;
import de.knowwe.diaflux.persistence.NodeHandlerManager;
import de.knowwe.diaflux.type.EdgeContentType;
import de.knowwe.diaflux.type.EdgeType;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.GuardType;
import de.knowwe.diaflux.type.NodeType;
import de.knowwe.diaflux.type.OriginType;
import de.knowwe.diaflux.type.PositionType;
import de.knowwe.diaflux.type.TargetType;
import de.knowwe.kdom.xml.AbstractXMLType;
import de.knowwe.kdom.xml.XMLContent;

/**
 * 
 * 
 * @author Reinhard Hatko
 * @created on: 12.10.2009
 */
public class FlowchartSubTreeHandler extends D3webSubtreeHandler<FlowchartType> {

	public static final String ORIGIN_KEY = "diafluxorigin";
	public static final String ICON_KEY = "diafluxicon";
	// public static final String ORIGIN = "diafluxorigin";
	// public static final String ICON = "diafluxicon";

	private final List<Class<? extends Type>> filteredTypes =
			new ArrayList<Class<? extends Type>>(0);

	public FlowchartSubTreeHandler() {
		this.registerConstraintModule(new FlowchartConstraintModule());
		filteredTypes.add(PositionType.class);
	}

	@Override
	public Collection<Message> create(Article article, Section<FlowchartType> s) {

		if (!article.isFullParse()) destroy(article, s);

		KnowledgeBase kb = getKB(article);
		Section<XMLContent> flowcontent = ((AbstractXMLType) s.get()).getContentChild(s);

		if (kb == null || flowcontent == null) {
			return null;
		}

		Map<String, String> attributeMap = AbstractXMLType.getAttributeMapFor(s);
		String name = attributeMap.get("name");
		boolean autostart = Boolean.parseBoolean(attributeMap.get("autostart"));

		if (name == null || name.equals("")) {
			name = "unnamed";
		}

		List<Message> errors = new ArrayList<Message>();

		List<Node> nodes = createNodes(article, name, s, errors);

		List<Edge> edges = createEdges(article, s, nodes, errors);

		Flow flow = FlowFactory.createFlow(kb, name, nodes, edges);
		flow.setAutostart(autostart);

		FlowchartUtils.storeFlowProperty(flow, ORIGIN_KEY, s.getID());
		// flow.getInfoStore().addValue(Property.getProperty(ORIGIN,
		// String.class), s.getID());

		String icon = attributeMap.get("icon");
		if (icon != null) {
			FlowchartUtils.storeFlowProperty(flow, ICON_KEY, icon);
			// flow.getInfoStore().addValue(Property.getProperty(ICON,
			// String.class), icon);
		}

		return errors;
	}

	@Override
	public void destroy(Article article, Section<FlowchartType> s) {
		KnowledgeBase kb = getKB(article);

		Map<String, String> attributeMap =
				AbstractXMLType.getAttributeMapFor(s);
		if (attributeMap != null) {
			String name = attributeMap.get("name");
			TerminologyObject oldFlow = kb.getManager().search(name);
			if (oldFlow != null) {
				oldFlow.destroy();
			}

		}

	}

	private class FlowchartConstraintModule extends ConstraintModule<FlowchartType> {

		@Override
		public boolean violatedConstraints(Article article, Section<FlowchartType> s) {
			return KnowWEUtils.getTerminologyManager(article).areTermDefinitionsModifiedFor(
					article)
					|| s.isOrHasChangedSuccessor(article.getTitle(), filteredTypes);
		}

	}

	@SuppressWarnings("unchecked")
	private List<Edge> createEdges(Article article, Section<FlowchartType> flowSection, List<Node> nodes, List<Message> errors) {
		List<Edge> result = new ArrayList<Edge>();

		List<Section<EdgeType>> edgeSections = new ArrayList<Section<EdgeType>>();
		Section<XMLContent> flowcontent = ((AbstractXMLType) flowSection.get()).getContentChild(flowSection);
		Sections.findSuccessorsOfType(flowcontent, EdgeType.class, edgeSections);

		for (Section<EdgeType> section : edgeSections) {

			String id = AbstractXMLType.getAttributeMapFor(section).get("fcid");
			Section<EdgeContentType> content = (Section<EdgeContentType>) section.getChildren().get(
					1);

			Section<OriginType> originSection = Sections.findChildOfType(content, OriginType.class);

			// TODO remove duplicate code
			if (originSection == null) {
				String messageText = "No origin node specified in edge with id '" + id + "'.";

				errors.add(Messages.syntaxError(messageText));
				continue;
			}
			String sourceID = getXMLContentText(originSection);

			Node source = DiaFluxPersistenceHandler.getNodeByID(sourceID, nodes);

			if (source == null) {
				String messageText = "No origin node found with id " + sourceID + " in edge " + id
						+ ".";

				errors.add(Messages.noSuchObjectError(messageText));
				continue;
			}

			Section<TargetType> targetSection = Sections.findChildOfType(content, TargetType.class);

			if (targetSection == null) {
				String messageText = "No target node specified in edge with id '" + id + "'.";

				errors.add(Messages.syntaxError(messageText));
				continue;
			}

			String targetID = getXMLContentText(targetSection);

			Node target = DiaFluxPersistenceHandler.getNodeByID(targetID, nodes);

			if (target == null) {
				String messageText = "No target node found with id " + targetID + " in edge " + id
						+ ".";
				errors.add(Messages.noSuchObjectError(messageText));
				continue;
			}

			Condition condition;

			Section<GuardType> guardSection = Sections.findChildOfType(content, GuardType.class);
			if (guardSection != null) {
				Section<CompositeCondition> compositionConditionSection = (Section<CompositeCondition>) guardSection.getChildren().get(
						1);
				condition = buildCondition(article, compositionConditionSection, errors);

				if (condition == null) {
					condition = ConditionTrue.INSTANCE;

					errors.add(Messages.objectCreationError("Could not parse condition: "
							+ getXMLContentText(guardSection)));

				}

			}
			else {
				condition = ConditionTrue.INSTANCE;
			}

			Edge edge = FlowFactory.createEdge(id, source, target, condition);

			result.add(edge);

		}

		return result;
	}

	private Condition buildCondition(Article article, Section<CompositeCondition> s, List<Message> errors) {

		return KDOMConditionFactory.createCondition(article, s);
	}

	@SuppressWarnings("unchecked")
	public static String getXMLContentText(Section<? extends AbstractXMLType> s) {
		String originalText = ((Section<XMLContent>) s.getChildren().get(1)).getText();
		// return StringEscapeUtils.unescapeXml(originalText);
		return originalText;
	}

	@SuppressWarnings("unchecked")
	private List<Node> createNodes(Article article, String flowName, Section<FlowchartType> flowSection, List<Message> errors) {

		List<Node> result = new ArrayList<Node>();
		ArrayList<Section<NodeType>> nodeSections = new ArrayList<Section<NodeType>>();
		Section<XMLContent> flowcontent = ((AbstractXMLType) flowSection.get()).getContentChild(flowSection);
		Sections.findSuccessorsOfType(flowcontent, NodeType.class, nodeSections);

		KnowledgeBase kb = getKB(article);

		for (Section<NodeType> nodeSection : nodeSections) {

			NodeHandler handler = NodeHandlerManager.getInstance().findNodeHandler(article, kb,
					nodeSection);

			if (handler == null) {
				errors.add(Messages.error("No NodeHandler found for: "
						+ nodeSection.getText()));
				continue;
			}

			else {// handler can in general handle NodeType
				String id = AbstractXMLType.getAttributeMapFor(nodeSection).get("fcid");

				Node node = handler.createNode(article, kb, nodeSection, flowSection, id, errors);

				if (node != null) {
					result.add(node);
				}
				else { // handler could not generate a node for the supplied
						// section
					Section<AbstractXMLType> nodeInfo = (Section<AbstractXMLType>) Sections.findSuccessor(
							nodeSection, handler.get().getClass());
					String text = getXMLContentText(nodeInfo);

					errors.add(Messages.objectCreationError("NodeHandler "
							+ handler.getClass().getSimpleName() + " could not create node for: "
							+ text));

					result.add(new CommentNode(id,
							"Surrogate for node of type " + text));
				}

			}

		}

		return result;
	}

}
