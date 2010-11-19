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
import de.d3web.diaFlux.flow.IEdge;
import de.d3web.diaFlux.flow.INode;
import de.d3web.diaFlux.inference.ConditionTrue;
import de.d3web.diaFlux.inference.DiaFluxUtils;
import de.d3web.diaFlux.io.DiaFluxPersistenceHandler;
import de.d3web.report.Message;
import de.d3web.we.flow.persistence.NodeHandler;
import de.d3web.we.flow.persistence.NodeHandlerManager;
import de.d3web.we.flow.type.EdgeType;
import de.d3web.we.flow.type.GuardType;
import de.d3web.we.flow.type.NodeType;
import de.d3web.we.flow.type.OriginType;
import de.d3web.we.flow.type.TargetType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;

/**
 *
 *
 * @author Reinhard Hatko
 * @created on: 12.10.2009
 */
public class FlowchartSubTreeHandler extends D3webSubtreeHandler {



	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {

		KnowledgeBaseManagement kbm = getKBM(article);
		Section flowcontent = ((AbstractXMLObjectType) s.getObjectType()).getContentChild(s);

		if (kbm == null || flowcontent == null) {
			return null;
		}

		Map<String, String> attributeMap = AbstractXMLObjectType.getAttributeMapFor(s);
		String name = attributeMap.get("name");
		String id = attributeMap.get("fcid");

		if (name == null || name.equals("")) name = "unnamed";

		List<Message> errors = new ArrayList<Message>();

		List<INode> nodes = createNodes(article, name, s, errors);

		List<IEdge> edges = createEdges(article, s, nodes, errors);

		Flow flow = FlowFactory.getInstance().createFlow(id, name, nodes, edges);

		DiaFluxUtils.addFlow(flow, kbm.getKnowledgeBase());

		List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();

		for (final Message message : errors) {
			msgs.add(new ObjectCreationError(message.getMessageText(), getClass()));
		}

		return msgs;
	}

	private List<IEdge> createEdges(KnowWEArticle article, Section flowSection, List<INode> nodes, List<Message> errors) {
		List<IEdge> result = new ArrayList<IEdge>();

		List<Section> edgeSections = new ArrayList<Section>();
		Section flowcontent = ((AbstractXMLObjectType) flowSection.getObjectType()).getContentChild(flowSection);
		flowcontent.findSuccessorsOfType(EdgeType.class, edgeSections);

		for (Section section : edgeSections) {

			String id = AbstractXMLObjectType.getAttributeMapFor(section).get("fcid");
			Section content = (Section) section.getChildren().get(1);

			Section originSection = content.findChildOfType(OriginType.class);

			// TODO remove duplicate code
			if (originSection == null) {
				String messageText = "No origin node specified in edge with id '" + id + "'.";

				errors.add(new Message(messageText));
				continue;
			}
			String sourceID = getXMLContentText(originSection);

			INode source = DiaFluxPersistenceHandler.getNodeByID(sourceID, nodes);

			if (source == null) {
				String messageText = "No origin node found with id " + sourceID + " in edge " + id
						+ ".";

				errors.add(new Message(messageText));
				continue;
			}

			Section targetSection = content.findChildOfType(TargetType.class);

			if (targetSection == null) {
				String messageText = "No target node specified in edge with id '" + id + "'.";

				errors.add(new Message(messageText));
				continue;
			}

			String targetID = getXMLContentText(targetSection);

			INode target = DiaFluxPersistenceHandler.getNodeByID(targetID, nodes);

			if (target == null) {
				String messageText = "No target node found with id " + targetID + " in edge " + id
						+ ".";
				errors.add(new Message(messageText));
				continue;
			}

			Condition condition;

			Section guardSection = content.findChildOfType(GuardType.class);
			if (guardSection != null) {
				Section compositionConditionSection = (Section) guardSection.getChildren().get(1);
				condition = buildCondition(article, compositionConditionSection, errors);

				if (condition == null) {
					condition = ConditionTrue.INSTANCE;

					errors.add(new Message("Could not parse condition: "
							+ getXMLContentText(guardSection)));

				}

			}
			else {
				condition = ConditionTrue.INSTANCE;
			}

			IEdge edge = FlowFactory.getInstance().createEdge(id, source,
					target, condition);

			result.add(edge);


		}

		return result;
	}



	private Condition buildCondition(KnowWEArticle article, Section s, List<Message> errors) {

		return KDOMConditionFactory.createCondition(article, s);
	}



	public static String getXMLContentText(Section s) {
		String originalText = ((Section) s.getChildren().get(1)).getOriginalText();
//		return StringEscapeUtils.unescapeXml(originalText);
		return originalText;
	}


	private List<INode> createNodes(KnowWEArticle article, String flowName, Section flowSection, List<Message> errors) {

		List<INode> result = new ArrayList<INode>();
		List<Section> nodeSections = new ArrayList<Section>();
		Section flowcontent = ((AbstractXMLObjectType) flowSection.getObjectType()).getContentChild(flowSection);
		flowcontent.findSuccessorsOfType(NodeType.class, nodeSections);

		KnowledgeBaseManagement kbm = getKBM(article);

		for (Section nodeSection : nodeSections) {


			NodeHandler handler = NodeHandlerManager.getInstance().findNodeHandler(article, kbm,
					nodeSection);

			if (handler == null) {
				errors.add(new Message("No NodeHandler found for: " + nodeSection.getOriginalText()));
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
					Section<KnowWEObjectType> nodeInfo = nodeSection.findSuccessor(handler.getObjectType().getClass());
					String text = getXMLContentText(nodeInfo);

					errors.add(new Message("NodeHandler " + handler.getClass().getSimpleName()
							+ " could not create node for: " + text));

					result.add(FlowFactory.getInstance().createCommentNode(id,
							"Surrogate for node of type " + text));
				}

			}

		}

		return result;
	}



	// vv old ANTLR Condition building

	// private Condition buildCondition(KnowWEArticle article, Section s,
	// List<Message> errors) {
	// String originalText = getXMLContentText(s);
	//
	// if (originalText.startsWith("PROCESSED[")) {
	//
	// String flowName = originalText.substring(10, originalText.length() -
	// 1);
	// return new FlowchartProcessedCondition(flowName);
	// }
	// else if (originalText.startsWith("IS_ACTIVE[")) {
	// int nodenameStart = originalText.indexOf('(');
	// int nodenameEnd = originalText.indexOf(')');
	//
	// String flowName = originalText.substring(10, nodenameStart);
	// String nodeName = originalText.substring(nodenameStart + 1,
	// nodenameEnd);
	// return new NodeActiveCondition(flowName, nodeName);
	// }
	// else {
	//
	// // for other Conditions use ANTLR parser
	// InputStream stream = new
	// ByteArrayInputStream(originalText.getBytes());
	// ANTLRInputStream input = null;
	// try {
	// input = new ANTLRInputStream(stream);
	// }
	// catch (IOException e) {
	// e.printStackTrace();
	// }
	// DefaultLexer lexer = new DefaultLexer(input);
	// CommonTokenStream tokens = new CommonTokenStream(lexer);
	// ComplexConditionSOLO parser = new ComplexConditionSOLO(tokens);
	//
	// RestrictedIDObjectManager idom = new
	// RestrictedIDObjectManager(getKBM(article));
	//
	// D3webConditionBuilder builder = new
	// D3webConditionBuilder("Parsed from article",
	// errors,
	// idom);
	//
	// parser.setBuilder(builder);
	// try {
	// parser.complexcondition();
	// }
	// catch (RecognitionException e) {
	// e.printStackTrace();
	// }
	// Condition condition = builder.pop();
	//
	// return condition;
	// }
	// }


}
