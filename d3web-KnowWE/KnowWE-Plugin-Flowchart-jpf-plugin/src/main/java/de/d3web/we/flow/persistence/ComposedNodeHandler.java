/**
 * 
 */
package de.d3web.we.flow.persistence;

import java.util.LinkedList;
import java.util.List;

import de.d3web.abstraction.ActionSetValue;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.diaFlux.NoopAction;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.INode;
import de.d3web.report.Message;
import de.d3web.we.flow.FlowchartSubTreeHandler;
import de.d3web.we.flow.FlowchartTerminologySubTreeHandler;
import de.d3web.we.flow.type.ActionType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * @author Reinhard Hatko
 * @created 10.08.10
 *
 */
public class ComposedNodeHandler extends AbstractNodeHandler {


	public ComposedNodeHandler() {
		super(ActionType.getInstance(), "KnOffice");
	}


	public boolean canCreateNode(KnowWEArticle article,
			KnowledgeBaseManagement kbm, Section nodeSection) {
		
		Section<AbstractXMLObjectType> nodeInfo = getNodeInfo(nodeSection);
		
		if (nodeInfo == null)
			return false;
		
		String actionString = FlowchartSubTreeHandler.getXMLContentText(nodeInfo);
		
		return actionString.startsWith("CALL[");
	}


	public INode createNode(KnowWEArticle article, KnowledgeBaseManagement kbm,
			Section nodeSection, Section flowSection, String id, List<Message> errors) {
		
		Section<AbstractXMLObjectType> nodeInfo = getNodeInfo(nodeSection);
		String actionString = FlowchartSubTreeHandler.getXMLContentText(nodeInfo);
		
		if (!actionString.startsWith("CALL[")) 
			return null;

		int nodenameStart = actionString.indexOf('(');
		int nodenameEnd = actionString.indexOf(')');

		String flowName = actionString.substring(5, nodenameStart);
		String nodeName = actionString.substring(nodenameStart + 1, nodenameEnd);

		return FlowFactory.getInstance().createComposedNode(id, flowName, nodeName);
		
		//old version using Terminology

//		QContainer container = kbm.findQContainer(flowName + "_Questionnaire");
//
//		if (container == null) {
//			errors.add(new Message("Terminology not found for flow'" + flowName + "'"));
//			return null;
//		}
//
//		QuestionMC question = null;
//
//		for (TerminologyObject child : container.getChildren()) {
//
//			if (child.getName().equals(
//					flowName + "_" + FlowchartTerminologySubTreeHandler.STARTNODES_QUESTION_NAME)) question = (QuestionMC) child;
//		}
//
//		if (question == null) {
//			errors.add(new Message("No startnode question found for flow '" + flowName + "'."));
//			return null;
//		}
//
//		ActionSetValue action = FlowFactory.getInstance().createSetValueAction();
//
//		action.setQuestion(question);
//
//		Choice answer = null;
//		for (Choice child : question.getAllAlternatives()) {
//			if (child.getName().equals(nodeName)) answer = child;
//
//		}
//
//		if (answer == null) {
//			errors.add(new Message("Startnode  '" + nodeName
//					+ "' not found in terminology of flow '" + flowName + "'."));
//			return null;
//		}
//
//	//TODO: HOTFIX	
//			List<ChoiceValue> values = new LinkedList<ChoiceValue>();
//			values.add(new ChoiceValue(answer));
//		
//			action.setValue(new MultipleChoiceValue(values));
////
//		return FlowFactory.getInstance().createActionNode(id, action);

	
	
	}

}
