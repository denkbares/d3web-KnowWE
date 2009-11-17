/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.flow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import de.d3web.KnOfficeParser.D3webConditionBuilder;
import de.d3web.KnOfficeParser.DefaultLexer;
import de.d3web.KnOfficeParser.IDObjectManagement;
import de.d3web.KnOfficeParser.RestrictedIDObjectManager;
import de.d3web.KnOfficeParser.complexcondition.ComplexConditionSOLO;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.RuleFactory;
import de.d3web.kernel.domainModel.Score;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondAnd;
import de.d3web.kernel.psMethods.diaFlux.ConditionTrue;
import de.d3web.kernel.psMethods.diaFlux.FluxSolver;
import de.d3web.kernel.psMethods.diaFlux.actions.NoopAction;
import de.d3web.kernel.psMethods.diaFlux.flow.Flow;
import de.d3web.kernel.psMethods.diaFlux.flow.FlowFactory;
import de.d3web.kernel.psMethods.diaFlux.flow.IEdge;
import de.d3web.kernel.psMethods.diaFlux.flow.INode;
import de.d3web.kernel.psMethods.heuristic.ActionHeuristicPS;
import de.d3web.kernel.psMethods.nextQASet.ActionIndication;
import de.d3web.report.Message;
import de.d3web.we.flow.type.ActionType;
import de.d3web.we.flow.type.EdgeType;
import de.d3web.we.flow.type.ExitType;
import de.d3web.we.flow.type.GuardType;
import de.d3web.we.flow.type.NodeType;
import de.d3web.we.flow.type.PositionType;
import de.d3web.we.flow.type.SourceType;
import de.d3web.we.flow.type.StartType;
import de.d3web.we.flow.type.TargetType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.filter.SectionFilter;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.logging.Logging;
import de.d3web.we.terminology.D3webReviseSubTreeHandler;

/**
 * 
 *
 * @author Reinhard Hatko
 * Created on: 12.10.2009
 */
public class FlowchartSubTreeHandler extends D3webReviseSubTreeHandler {
	
	
	
	@Override
	public void reviseSubtree(KnowWEArticle article, Section s) {
		
		KnowledgeBaseManagement kbm = getKBM(article, s);
		
		if (kbm == null)
			return;

		Section content = ((AbstractXMLObjectType) s.getObjectType()).getContentChild(s);

		if (content == null) 
			return;
		
		
		
		List<Section> sections = new ArrayList<Section>();
		
		content.findSuccessorsOfType(NodeType.class, sections);
		List<INode> nodes = createNodes(article, sections);
		
		sections.clear();
		
		content.findSuccessorsOfType(EdgeType.class, sections);
		List<IEdge> edges = createEdges(article, sections, nodes);
		
		
		Map<String, String> attributeMap = AbstractXMLObjectType.getAttributeMapFor(s);
		
		String name = attributeMap.get("name");
		
		if (name == null || name.equals(""))
			name = "unnamed";
		
		
		String id = attributeMap.get("id");
		
		Flow flow = FlowFactory.getInstance().createFlow(id, name, nodes, edges);
		
		System.out.println(flow);
		
		KnowledgeBase knowledgeBase = getKBM(article, s).getKnowledgeBase();

		knowledgeBase.addKnowledge(FluxSolver.class, flow, FluxSolver.DIAFLUX);
		
		
		
//		FluxSolver fluxSolver = new FluxSolver();
//		XPSCase theCase = CaseFactory.createXPSCase(knowledgeBase);
//		fluxSolver.init(theCase);
//		
//		List<PropagationEntry> changes = new ArrayList<PropagationEntry>();
		
//		changes.add(new PropagationEntry(object, oldValue, newValue));
//		
//		fluxSolver.propagate(theCase, changes);
		
		
		
		
	}

	private List<IEdge> createEdges(KnowWEArticle article, List<Section> edgeSections, List<INode> nodes) {
		List<IEdge> result = new ArrayList<IEdge>();
		
		
		for (Section section : edgeSections) {
			
			Section content = section.getChildren().get(1); //get edgecontent-section
			
			String sourceID = getXMLContentText(content.findChildOfType(SourceType.class));
			
			INode source = getNodeByID(sourceID, nodes);
			
			if (source == null) {
				System.out.println("No node found with id: " + sourceID);
				continue;
			}
			
			
			String targetID = getXMLContentText(content.findChildOfType(TargetType.class));
			
			INode target = getNodeByID(targetID, nodes);
			
			if (target == null) {
				System.out.println("No node found with id: " + targetID);
				continue;
			}
			
			String id = AbstractXMLObjectType.getAttributeMapFor(section).get("id");
			
			
			
			AbstractCondition condition;
			
			List<Message> conditionErrors = new ArrayList<Message>();
			Section guardSection = content.findChildOfType(GuardType.class);
			
			if (guardSection != null) {
				condition = buildCondition(article, guardSection, conditionErrors);
				
				if (condition == null) {
					condition = ConditionTrue.INSTANCE;
					
					System.out.println("Could not parse condition: " + guardSection.getOriginalText());
					
				}
				
			}
			else
				condition = ConditionTrue.INSTANCE;
			
			System.out.println(conditionErrors);
			
			
			result.add(FlowFactory.getInstance().createEdge(id, source, target, condition));
			
			
			
		}
		
		
		
		return result;
	}

	private AbstractCondition buildCondition(KnowWEArticle article, Section s, List<Message> errors) {
		
		String originalText = getXMLContentText(s);
		
		InputStream stream = new ByteArrayInputStream(originalText.getBytes());
		ANTLRInputStream input  = null;
		try {
			input = new ANTLRInputStream(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		DefaultLexer lexer = new DefaultLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ComplexConditionSOLO parser = new ComplexConditionSOLO(tokens);
		
		IDObjectManagement idom = new RestrictedIDObjectManager(getKBM(article, s));
		
		
		D3webConditionBuilder builder = new D3webConditionBuilder("Parsed from article", errors, idom);
		
		
		parser.setBuilder(builder);
		try {
			parser.complexcondition();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
		AbstractCondition condition = builder.pop();
		
		return condition;
	}

	private String getXMLContentText(Section s) {
		return s.getChildren().get(1).getOriginalText();
	}

	private INode getNodeByID(String nodeID, List<INode> nodes) {

		for (INode node : nodes) {
			if (node.getID().equals(nodeID))
				return node;
		}
		
		return null;
	}

	private List<INode> createNodes(KnowWEArticle article, List<Section> nodeSections) {
		
		List<INode> result = new ArrayList<INode>();
		
		
		for (Section section : nodeSections) {
			
			Section nodeContent = section.getChildren().get(1); //Section of NodeContentType
			
			// get the important info 
			List<Section> children = nodeContent.getChildren(new SectionFilter() { 
				
				public boolean accept(Section section) {
					return section.getObjectType() != PositionType.getInstance() && section.getObjectType() != PlainText.getInstance();
				}
			});
			
//			Assert.assertEquals(section + " has the wrong number of children.",1, children.size());
			
			if (children.size() != 1)
				continue;
			
			Section nodeinfo = children.get(0);
			
			String id = AbstractXMLObjectType.getAttributeMapFor(section).get("id");
			
			if (nodeinfo.getObjectType() == StartType.getInstance()) 
				result.add(createStartNode(id, nodeinfo));
			else if (nodeinfo.getObjectType() == ExitType.getInstance())
				result.add(createEndNode(id, nodeinfo));
			else if(nodeinfo.getObjectType() == ActionType.getInstance())
				result.add(createActionNode(article, id, nodeinfo));
			else
				throw new UnsupportedOperationException("Unknown node type: " + nodeinfo.getObjectType());
			
			
		}
		
		
		return result;
	}

	private INode createActionNode(KnowWEArticle article, String id, Section section) {
		
		RuleAction action = NoopAction.INSTANCE;
		String string = getXMLContentText(section);
		if (string.startsWith("ERFRAGE")) {
			action = createQAindicationAction(article, section, string);
		} else if (string.contains("=")) {
			action = createHeuristicScoreAction(article, section, string);
		} else
			action = NoopAction.INSTANCE;
		
//		D3WebAction action = new ChangeDiagnosisStateAction(diagnosis, state);
		return FlowFactory.getInstance().createNode(id, action);
		
	}

	private RuleAction createHeuristicScoreAction(KnowWEArticle article, Section section,
			String string) {
		
		String[] split = string.split("=");
		String solution = split[0].trim();
		String score = split[1].trim();
		
		RuleComplex rule = new RuleComplex();
		
		ActionHeuristicPS action = new ActionHeuristicPS(rule);
		rule.setAction(action);
		rule.setCondition(new CondAnd(new ArrayList()));
		
		if (solution.startsWith("\""))// remove "
			solution = solution.substring(1, solution.length()-1);
		
		
		Diagnosis diagnosis = getKBM(article, section).findDiagnosis(solution); 
		
		if (diagnosis == null)
			Logging.getInstance().log(Level.INFO, "Diagnosis not found: " + solution);
		
		action.setDiagnosis(diagnosis);
		
		if (score.contains("P7"))
			action.setScore(Score.P7);
		else 
			action.setScore(Score.N7);
		
		
		return action;
	}

	private RuleAction createQAindicationAction(KnowWEArticle article, Section section, String string) {
		String name = string.substring(8, string.length() - 1);
		QASet findQuestion = getKBM(article, section).findQuestion(name);
		
		if (findQuestion == null)
			findQuestion = getKBM(article, section).findQContainer(name);
			
		if (findQuestion == null) {
			Logging.getInstance().log(Level.WARNING, "could not find question: " + name);
			return NoopAction.INSTANCE;
		}
		
		List qasets = new ArrayList();
		
		qasets.add(findQuestion);
		
		RuleComplex rule = RuleFactory.createRule("0000");
		rule.setProblemsolverContext(FluxSolver.class);
		rule.setCondition(new CondAnd(new ArrayList()));

		RuleAction action = new ActionIndication(rule);
		((ActionIndication) action).setQASets(qasets);
//		((RuleAction)action).setCorrespondingRule(rule);
		
		return action;
	}

	private INode createEndNode(String id, Section section) {
		String name = section.getChildren().get(0).getOriginalText();
		
		return FlowFactory.getInstance().createEndNode(id, name);
	}

	private INode createStartNode(String id, Section section) {
		String name = section.getChildren().get(0).getOriginalText();
		
		return FlowFactory.getInstance().createStartNode(id, name);
		
	}

}
