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
import java.util.List;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.ConditionTrue;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.diaFlux.flow.CommentNode;
import de.d3web.diaFlux.flow.ComposedNode;
import de.d3web.diaFlux.flow.Edge;
import de.d3web.diaFlux.flow.Flow;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.Node;
import de.d3web.diaFlux.io.DiaFluxPersistenceHandler;
import de.d3web.strings.Strings;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.diaflux.persistence.NodeHandler;
import de.knowwe.diaflux.persistence.NodeHandlerManager;
import de.knowwe.diaflux.type.EdgeType;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.FlowchartXMLHeadType;
import de.knowwe.diaflux.type.GuardType;
import de.knowwe.diaflux.type.NodeType;
import de.knowwe.diaflux.type.OriginType;
import de.knowwe.diaflux.type.TargetType;
import de.knowwe.kdom.xml.AbstractXMLType;
import de.knowwe.kdom.xml.XMLContent;

/**
 * @author Reinhard Hatko
 * @created on: 12.10.2009
 */
public class FlowchartSubTreeHandler implements D3webCompileScript<FlowchartType> {

	public static final String ORIGIN_KEY = "diafluxorigin";
	public static final String ICON_KEY = "diafluxicon";

	@Override
	public void compile(D3webCompiler article, Section<FlowchartType> s) {

		KnowledgeBase kb = D3webUtils.getKnowledgeBase(article);

		Section<XMLContent> flowContent = AbstractXMLType.getContentChild(s);

		Section<FlowchartXMLHeadType> head = Sections.successor(s, FlowchartXMLHeadType.class);
		if (kb == null || flowContent == null
				|| head == null || head.hasErrorInSubtree()) {
			return;
		}

		String name = FlowchartType.getFlowchartName(s);
		String icon = FlowchartType.getIcon(s);
		boolean autostart = FlowchartType.isAutoStart(s);

		if (name == null || name.equals("")) {
			name = "unnamed";
		}

		List<Node> nodes = createNodes(article, kb, s);
		List<Edge> edges = createEdges(article, s, nodes);
		Flow flow = FlowFactory.createFlow(kb, name, nodes, edges);
		flow.setAutostart(autostart);

		FlowchartUtils.storeFlowProperty(flow, ORIGIN_KEY, s.getID());
		if (!Strings.isBlank(icon)) {
			FlowchartUtils.storeFlowProperty(flow, ICON_KEY, icon);
		}

	}

	private static List<Edge> createEdges(D3webCompiler compiler, Section<FlowchartType> flowSection, List<Node> nodes) {
		List<Edge> result = new ArrayList<>();

		List<Section<EdgeType>> edgeSections = new ArrayList<>();
		Section<XMLContent> flowcontent = AbstractXMLType.getContentChild(flowSection);
		Sections.successors(flowcontent, EdgeType.class, edgeSections);

		for (Section<EdgeType> section : edgeSections) {

			String id = AbstractXMLType.getAttributes(section).get("fcid");

			Node origin = findNodeForEdge(compiler, section, OriginType.class, nodes);
			Node target = findNodeForEdge(compiler, section, TargetType.class, nodes);

			if (origin == null || target == null) {
				continue;
			}

			Condition condition;
			Section<GuardType> guardSection = Sections.successor(section, GuardType.class);
			List<Message> msgs = new ArrayList<>();
			if (guardSection != null) {

				Section<CompositeCondition> compositionConditionSection = Sections.successor(
						guardSection, CompositeCondition.class);
				condition = buildCondition(compiler, compositionConditionSection);

				if (condition == null) {
					msgs.add(Messages.error("Could not parse condition: "
							+ getXMLContentText(guardSection)));
				}
			}
			else {
				if (origin instanceof ComposedNode) {
					msgs.add(Messages.error("Outgoing condition missing for node " + origin.toString()));
				}
				condition = ConditionTrue.INSTANCE;
			}
			Messages.storeMessages(compiler, section, FlowchartSubTreeHandler.class, msgs);

			if (condition != null) {
				Edge edge = FlowFactory.createEdge(id, origin, target, condition);
				result.add(edge);
			}
		}

		return result;
	}

	private static <T extends AbstractXMLType> Node findNodeForEdge(PackageCompiler compiler, Section<EdgeType> edge, Class<T> childType, List<Node> nodes) {
		Section<T> targetSection = Sections.successor(edge, childType);

		if (targetSection == null) {
			String id = AbstractXMLType.getAttributes(edge).get("fcid");
			String messageText = "No node of type '" + childType.getSimpleName()
					+ "' specified in edge with id '" + id + "'.";
			Messages.storeMessage(compiler, edge, FlowchartSubTreeHandler.class,
					Messages.syntaxError(messageText));
			return null;
		}

		String nodeID = getXMLContentText(targetSection);

		Node target = DiaFluxPersistenceHandler.getNodeByID(nodeID, nodes);

		if (target == null) {
			String id = AbstractXMLType.getAttributes(edge).get("fcid");
			String messageText = "No node found with id '" + nodeID + "' (in edge '" + id + "').";
			Messages.storeMessage(compiler, edge, FlowchartSubTreeHandler.class,
					Messages.noSuchObjectError(messageText));
			return null;
		}
		return target;

	}

	private static Condition buildCondition(D3webCompiler compiler, Section<CompositeCondition> s) {
		return KDOMConditionFactory.createCondition(compiler, s);
	}

	public static String getXMLContentText(Section<? extends AbstractXMLType> s) {
		Section<XMLContent> contentChild = AbstractXMLType.getContentChild(s);
		if (contentChild == null) {
			return "";
		}
		else {
			return contentChild.getText().replaceAll("^<!\\[CDATA\\[", "").replaceAll("\\]\\]>$",
					"");
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Node> createNodes(D3webCompiler compiler, KnowledgeBase kb, Section<FlowchartType> flowSection) {

		List<Node> result = new ArrayList<>();
		ArrayList<Section<NodeType>> nodeSections = new ArrayList<>();
		Section<XMLContent> flowcontent = AbstractXMLType.getContentChild(flowSection);
		Sections.successors(flowcontent, NodeType.class, nodeSections);
		for (Section<NodeType> nodeSection : nodeSections) {

			NodeHandler handler = NodeHandlerManager.getInstance().findNodeHandler(compiler, kb,
					nodeSection);

			List<Message> msgs = new ArrayList<>();
			if (handler == null) {
				msgs.add(Messages.error("No NodeHandler found for: " + nodeSection.getText()));
			}
			else {// handler can in general handle NodeType
				String id = getNodeID(nodeSection);
				Node node = handler.createNode(compiler, kb, nodeSection, flowSection, id);

				// handler could not generate a node for the supplied section
				if (node == null) {
					Section<AbstractXMLType> nodeInfo = (Section<AbstractXMLType>) Sections.successor(
							nodeSection, handler.get().getClass());
					String text = getXMLContentText(nodeInfo);

					msgs.add(Messages.error("Could not create node for: " + text));

					node = new CommentNode(id, "Surrogate for node of type " + text);

				}

				result.add(node);
			}
			Messages.storeMessages(compiler, nodeSection, FlowchartSubTreeHandler.class, msgs);

		}

		return result;
	}

	public static String getNodeID(Section<NodeType> nodeSection) {
		return AbstractXMLType.getAttributes(nodeSection).get("fcid");
	}

}
