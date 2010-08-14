/**
 * 
 */
package de.d3web.we.flow.persistence;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.d3web.abstraction.ActionSetValue;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.INode;
import de.d3web.report.Message;
import de.d3web.we.flow.FlowchartSubTreeHandler;
import de.d3web.we.flow.FlowchartTerminologySubTreeHandler;
import de.d3web.we.flow.type.ExitType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * @author Reinhard Hatko
 * @created 10.08.10
 * 
 */
public class ExitNodeHandler extends AbstractNodeHandler {

	public ExitNodeHandler() {
		super(ExitType.getInstance(), null);
	}

	public boolean canCreateNode(KnowWEArticle article,
			KnowledgeBaseManagement kbm, Section nodeSection) {

		Section<AbstractXMLObjectType> nodeInfo = getNodeInfo(nodeSection);

		return nodeInfo != null;
	}

	public INode createNode(KnowWEArticle article, KnowledgeBaseManagement kbm,
			Section nodeSection, Section flowSection, String id, List<Message> errors) {

		String flowName = AbstractXMLObjectType.getAttributeMapFor(flowSection).get("name");

		Section<AbstractXMLObjectType> nodeInfo = getNodeInfo(nodeSection);

		String endNodeName = FlowchartSubTreeHandler.getXMLContentText(nodeInfo);

		ActionSetValue action = FlowFactory.getInstance().createSetValueAction();

		QuestionMC question = (QuestionMC) kbm.findQuestion(
				flowName + "_" + FlowchartTerminologySubTreeHandler.EXITNODES_QUESTION_NAME);

		action.setQuestion(question);

		Choice answer = null;
		for (Choice child : question.getAllAlternatives()) {
			if (child.getName().equals(endNodeName)) {
				answer = child;
				break;
			}

		}

		if (answer == null) {
			errors.add(new Message("No startnode  '" + flowName +
					"' not found in terminology of flow '" + flowName + "'."));
			return null;
		}

		// HOTFIX
		List<ChoiceValue> values = new LinkedList<ChoiceValue>();
		values.add(new ChoiceValue(answer));

		action.setValue(new MultipleChoiceValue(values));
		//

		return FlowFactory.getInstance().createEndNode(id, endNodeName, action);

	}

}
