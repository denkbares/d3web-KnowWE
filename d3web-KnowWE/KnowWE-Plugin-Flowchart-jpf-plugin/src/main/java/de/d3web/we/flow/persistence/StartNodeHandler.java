/**
 * 
 */
package de.d3web.we.flow.persistence;

import java.util.List;

import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.diaFlux.flow.FlowFactory;
import de.d3web.diaFlux.flow.INode;
import de.d3web.report.Message;
import de.d3web.we.flow.FlowchartSubTreeHandler;
import de.d3web.we.flow.FlowchartTagHandler;
import de.d3web.we.flow.type.StartType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 * @author Reinhard Hatko
 * @created 10.08.10
 * 
 */
public class StartNodeHandler extends AbstractNodeHandler {

	public StartNodeHandler() {
		super(StartType.getInstance(), null);
	}

	public boolean canCreateNode(KnowWEArticle article,
			KnowledgeBaseManagement kbm, Section nodeSection) {

		Section<AbstractXMLObjectType> nodeInfo = getNodeInfo(nodeSection);

		return nodeInfo != null;

	}

	public INode createNode(KnowWEArticle article, KnowledgeBaseManagement kbm, Section nodeSection, Section flowSection, String id, List<Message> errors) {

		Section<AbstractXMLObjectType> nodeInfo = getNodeInfo(nodeSection);

		String name = FlowchartSubTreeHandler.getXMLContentText(nodeInfo);

		return FlowFactory.getInstance().createStartNode(id, name);

	}

}
