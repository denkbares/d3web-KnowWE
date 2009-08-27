package de.d3web.knowledgeExporter.testutils;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Report;
import de.d3web.textParser.decisionTable.DecisionTable;
import de.d3web.textParser.decisionTable.DecisionTableConfigReader;
import de.d3web.textParser.decisionTable.DecisionTableValueChecker;

public class SetCoveringTableValueChecker extends DecisionTableValueChecker {

	
	public SetCoveringTableValueChecker(DecisionTableConfigReader cReader,
			KnowledgeBaseManagement kbm) {
		super(cReader, kbm);
	}
	
	public Report checkValues(DecisionTable table) {
		return null;
	}

}
