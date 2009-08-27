package de.d3web.KnOfficeParser.table;


import java.util.ResourceBundle;

import de.d3web.KnOfficeParser.IDObjectManagement;
import de.d3web.report.Message;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.psMethods.xclPattern.XCLRelationType;
/**
 * Erstellt XCL Relationen aus einer Tabellenzelle
 * @author Markus Friedrich
 *
 */
public class XCLRelationBuilder implements CellKnowledgeBuilder {

	private ResourceBundle properties;
	private boolean createUncompleteFindings = true;
	
	public boolean isCreateUncompleteFindings() {
		return createUncompleteFindings;
	}

	public void setCreateUncompleteFindings(boolean createUncompleteFindings) {
		this.createUncompleteFindings = createUncompleteFindings;
	}

	public XCLRelationBuilder (String file) {
		properties = ResourceBundle.getBundle(file);
	}
	
	@Override
	public Message add(IDObjectManagement idom, int line, int column,
			String file, AbstractCondition cond, String text, Diagnosis diag, boolean errorOccured) {
		if (!createUncompleteFindings) {
			if (errorOccured) {
				System.out.println(text);
				return null;
			}
		}
		String s;
		try {
			s = properties.getString(text);
		} catch (Exception e1) {
			s=text;
		}
		if (s.equals("--")) {
			XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, diag, XCLRelationType.contradicted);
		} else if (s.equals("!")) {
			XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, diag, XCLRelationType.requires);
		} else if (s.equals("++")) {
			XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, diag, XCLRelationType.sufficiently);
		} else {
			Double value;
			try {
				value = Double.parseDouble(s);
			} catch (NumberFormatException e) {
				return MessageKnOfficeGenerator.createNoValidWeightException(file, line, column, "", text);
			}
			XCLModel.insertXCLRelation(idom.getKnowledgeBase(), cond, diag, XCLRelationType.explains, value);
			
		}
		return null;
	}

}
