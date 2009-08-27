package de.d3web.textParser.decisionTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.report.Report;

public class IndicationTableValueChecker implements ValueChecker{

	KnowledgeBaseManagement kbm;
	
	public IndicationTableValueChecker(KnowledgeBaseManagement kbm) {
		this.kbm = kbm;
	}

	/* (non-Javadoc)
	 * @see de.d3web.textParser.decisionTable.ValueChecker#checkValues(de.d3web.textParser.decisionTable.DecisionTable)
	 */
	public Report checkValues(DecisionTable table) {
		Report report = new Report();
		
		report.addAll(checkDiagnoses(table));
		report.addAll(checkDiagnosisStates(table));
		report.addAll(checkQASets(table));
		report.addAll(checkData(table));
		
		return report;
	}

	private Report checkDiagnoses(DecisionTable table) {
		Report report = new Report();
		for (int j=1; j<table.columns(); j++) {
			if (!table.isEmptyCell(0,j)) {
				String diagnosisText = table.get(0,j);
				if (kbm.findDiagnosis(diagnosisText)==null)
					report.error(MessageGenerator.invalidDiagnosis(0,j,diagnosisText));
			}
		}
		
		return report;
	}
	
	private Report checkDiagnosisStates(DecisionTable table) {
		Report report = new Report();
		String[] states = new String[] {
				"suggested", "s", "verdächtig", "v",
				"established", "e", "bestätigt", "b",
				"excluded", "x", "ausgeschlossen", "a",
				"unclear", "u", "unklar" };
		List<String> statesList = Arrays.asList(states);
		for (int j=1; j<table.columns(); j++) {
			if (!table.isEmptyCell(1,j)) {
				String stateText = table.get(1,j);
				if (!statesList.contains(stateText))
					report.error(MessageGenerator.invalidDiagnosisState(0,j,stateText));
			}
		}
		return report;
	}
	
	private Report checkQASets(DecisionTable table) {
		Report report = new Report();
		for (int i=2; i<table.rows(); i++) {
			if (!table.isEmptyCell(i,0)) {
				String qText = table.get(i,0);
				QASet q = kbm.findQContainer(qText);
				if (q==null)
					q = kbm.findQuestion(qText);
				if (q==null)
					report.error(MessageGenerator.invalidQASet(i,0,qText));
			}
		}
		return report;
	}
	
	private Report checkData(DecisionTable table) {
		Report report = new Report();
		for (int j=1; j<table.columns(); j++) {
			List<Double> list = new ArrayList<Double>(1);
			for (int i=2; i<table.rows(); i++) {
				if (!table.isEmptyCell(i,j)) {
					String text = table.get(i,j);
					try {
						Double d = Double.parseDouble(text);
						if (list.contains(d))
							report.error(MessageGenerator.doubledValue(i,j,text));
						else
							list.add(d);
					}
					catch (NumberFormatException e) {
						report.error(MessageGenerator.invalidValue(i,j,text));
					}
				}
			}
		}
		return report;
	}
}
