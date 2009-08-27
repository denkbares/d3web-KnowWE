package de.d3web.KnOfficeParser.table;

import de.d3web.KnOfficeParser.IDObjectManagement;
import de.d3web.report.Message;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
/**
 * Interface für Builder, die Wissen aus einer Tabellenzelle inklusive derren Kontext generieren
 * @author Markus Friedrich
 *
 */
public interface CellKnowledgeBuilder {
	Message add(IDObjectManagement idom, int line, int column, String file, AbstractCondition cond, String text, Diagnosis diag, boolean errorOccured);
}
