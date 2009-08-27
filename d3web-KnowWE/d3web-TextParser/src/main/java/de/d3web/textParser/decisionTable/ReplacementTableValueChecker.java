/**
 * 
 */
package de.d3web.textParser.decisionTable;

import java.util.ArrayList;
import java.util.List;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.report.Report;
import de.d3web.textParser.Utils.KBUtils;

/**
 * @author Andreas
 */
public class ReplacementTableValueChecker implements ValueChecker {

	KnowledgeBaseManagement kbm;
	List<String> originals;
	List<String> replacements;
	
	public ReplacementTableValueChecker(KnowledgeBaseManagement kbm) {
		super();
		this.kbm = kbm;
	}

	/* (non-Javadoc)
	 * @see de.d3web.textParser.decisionTable.ValueChecker#checkValues(de.d3web.textParser.decisionTable.DecisionTable)
	 */
	public Report checkValues(DecisionTable table) {
		Report report = new Report();
        originals = new ArrayList<String>(1);
		replacements = new ArrayList<String>(1);
		
		readNames(table);
		report.addAll(checkObjectNames(table));
		report.addAll(checkForCircles(table));
		report.addAll(checkForDuplication(table));
		return report;
	}

	private void readNames(DecisionTable table) {
		for (int i=0; i<table.rows(); i++) {
//			if (!table.isEmptyRow(i) &&
//				!(KBUtils.findNamedObject(kbm, table.get(i,0))==null && table.get(i,2).equals(""))) {
				originals.add(table.get(i,0));
				replacements.add(table.get(i,1));
//			}
		}
	}
	
	private Report checkObjectNames(DecisionTable table) {
		Report report = new Report();
        for (int i=0; i<table.rows(); i++) {
			String name = table.get(i,0);
			String replace = table.get(i,1);
			
			QContainer qc = kbm.findQContainer(name);
			Question q = kbm.findQuestion(name);
			Diagnosis d = kbm.findDiagnosis(name);
			// name exists but doesn't represent a named object
			if (!name.equals("") && qc==null && q==null && d==null) {
				// check if object should be an answer
				if (!table.get(i,2).equals("")) {
					q = kbm.findQuestion(table.get(i,2));
					if (q==null)
						report.error(MessageGenerator.invalidQuestion(i,2,table.get(i,2)));
					else if (q instanceof QuestionChoice){
						Answer a = kbm.findAnswerChoice((QuestionChoice)q, name);
						if (a==null)
							report.error(MessageGenerator.invalidAnswer(i,2,q.getText(),name));
					}
					else {
						report.error(MessageGenerator.invalidQuestionChoice(i,2,table.get(i,2)));
					}
				}
				else
					report.error(MessageGenerator.invalidObjectName(i,0,name));
			}
			// more than one named object exists with the same name
			else if ((qc!=null || q!=null || d!=null) &&
					!(qc!=null ^ q!=null ^ d!=null)) {
				report.error(MessageGenerator.replace_NotUnique(i,0,name));
			}
			// unique named object found !!!
			else {
				// check if third column is empty
				if (!table.get(i,2).equals(""))
					report.error(MessageGenerator.fieldNotEmpty(i,1,table.get(i,2)));
			}
			
			// check replacement
			NamedObject obj = KBUtils.findNamedObject(kbm, replace);
			// TODO: search for AnswerChoice
			if (obj!=null)
				report.error(MessageGenerator.replace_AlreadyExists(i,1,replace));
			
		}
		return report;
	}
	
	private Report checkForCircles(DecisionTable table) {
		Report report = new Report();
		for (int i=0; i<table.rows(); i++) {
			if (!table.isEmptyRow(i)) {
				String original = table.get(i,0);
				if (!original.equals("") && replacements.contains(original)) {
					report.error(MessageGenerator.replace_Circle(originals.get(i)));
				}
			}
		}
		
		return report;
	}
	
	private Report checkForDuplication(DecisionTable table) {
		Report report = new Report();
		for (int i=0; i<table.rows(); i++) {
			if (!table.isEmptyRow(i)) {
				// check for duplicate original names
				String original = table.get(i,0);
				if (originals.indexOf(original)!=
					originals.lastIndexOf(original)) {
					report.error(MessageGenerator.replace_MultipleReplacements(i,0,original));
				}
				// check for duplicate replacement names
				String replacement = table.get(i,1);
				if (replacements.indexOf(replacement)!=
					replacements.lastIndexOf(replacement)) {
					report.error(MessageGenerator.replace_MultipleNames(i,1,replacement));
				}
			}
		}
		return report;
	}
}
