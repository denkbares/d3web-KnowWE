/**
 * 
 */
package de.d3web.textParser.decisionTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionMC;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionOC;
import de.d3web.kernel.psMethods.shared.PSMethodShared;
import de.d3web.kernel.psMethods.shared.comparators.GroupedComparator;
import de.d3web.kernel.psMethods.shared.comparators.IndividualComparator;
import de.d3web.kernel.psMethods.shared.comparators.QuestionComparator;
import de.d3web.kernel.psMethods.shared.comparators.mc.QuestionComparatorMCGroupedAsymmetric;
import de.d3web.kernel.psMethods.shared.comparators.mc.QuestionComparatorMCGroupedSymmetric;
import de.d3web.kernel.psMethods.shared.comparators.mc.QuestionComparatorMCIndividual;
import de.d3web.kernel.psMethods.shared.comparators.num.QuestionComparatorNumDivision;
import de.d3web.kernel.psMethods.shared.comparators.num.QuestionComparatorNumDivisionDenominator;
import de.d3web.kernel.psMethods.shared.comparators.num.QuestionComparatorNumFuzzy;
import de.d3web.kernel.psMethods.shared.comparators.num.QuestionComparatorNumIndividual;
import de.d3web.kernel.psMethods.shared.comparators.num.QuestionComparatorNumSection;
import de.d3web.kernel.psMethods.shared.comparators.num.QuestionComparatorNumSectionInterpolate;
import de.d3web.kernel.psMethods.shared.comparators.oc.QuestionComparatorOCGroupedAsymmetric;
import de.d3web.kernel.psMethods.shared.comparators.oc.QuestionComparatorOCGroupedSymmetric;
import de.d3web.kernel.psMethods.shared.comparators.oc.QuestionComparatorOCIndividual;
import de.d3web.kernel.psMethods.shared.comparators.oc.QuestionComparatorOCScaled;
import de.d3web.kernel.psMethods.shared.comparators.oc.QuestionComparatorYN;
import de.d3web.report.Report;

/**
 * Creation date: (31.10.2005)
 * 
 * @author Andreas Klar
 * 
 */
public class SimilarityTableKnowledgeGenerator extends KnowledgeGenerator {

	private static final ResourceBundle rb = ResourceBundle
			.getBundle("properties.textParser");

	private static String GROUPED_SYMMETRIC = rb
			.getString("similarity.comparator.grouped.symmetric");

	private static String GROUPED_SYMMETRIC_ABBR = rb
			.getString("similarity.comparator.grouped.symmetric.abbr");

	private static String GROUPED_ASYMMETRIC = rb
			.getString("similarity.comparator.grouped.asymmetric");

	private static String GROUPED_ASYMMETRIC_ABBR = rb
			.getString("similarity.comparator.grouped.asymmetric.abbr");

	private static String INDIVIDUAL = rb
			.getString("similarity.comparator.individual");

	private static String SCALED = rb.getString("similarity.comparator.scaled");

	private static String SECTION = rb
			.getString("similarity.comparator.section");

	private static String SECTION_INTERPOLATE = rb
			.getString("similarity.comparator.sectionInterpolate");

	private static String DIVISION = rb
			.getString("similarity.comparator.division");

	private static String DIVISION_DENOMINATOR = rb
			.getString("similarity.comparator.divisionDenomiator");

	private static String YN = rb.getString("similarity.comparator.YN");

	// Change
	private static String FUZZY = rb.getString("similarity.comparator.fuzzy");

	private static String ABSOLUTE = rb
			.getString("similarity.comparator.absolute");

	private static String PERCENTAGE = rb
			.getString("similarity.comparator.percentage");

	public SimilarityTableKnowledgeGenerator(KnowledgeBaseManagement kbm) {
		super(kbm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.textParser.decisionTable.KnowledgeGenerator#generateKnowledge(de.d3web.textParser.decisionTable.DecisionTable)
	 */
	@Override
	public Report generateKnowledge(DecisionTable table) {
		int count = 0;
		for (int i = 0; i < table.rows(); i++) {
			if (!table.isEmptyCell(i, 0)) {
				String comp = table.get(i, 1);
				Question q = kbm.findQuestion(table.get(i, 0));
				removeSimilarity(q);

				// symmetrische ï¿½hnlichkeitsmatrix
				if (comp.equalsIgnoreCase(GROUPED_SYMMETRIC)
						|| comp.equalsIgnoreCase(GROUPED_SYMMETRIC_ABBR)) {
					setQCGroupedSymmetric(table, i);
					count++;
				}
				// asymmetrische ï¿½hnlichkeitsmatrix
				// bug fixed
				else if (comp.equalsIgnoreCase(GROUPED_ASYMMETRIC)
						|| comp.equalsIgnoreCase(GROUPED_ASYMMETRIC_ABBR)) {
					setQCGroupedAsymmetric(table, i);
					count++;
				}
				// individuell
				else if (comp.equalsIgnoreCase(INDIVIDUAL)) {
					setQCIndividual(table, i);
					count++;
				}
				// intervall-schema
				else if (comp.equalsIgnoreCase(SCALED)) {
					setQCScaled(table, i);
					count++;
				}
				// abschnitt normal
				else if (comp.equalsIgnoreCase(SECTION)) {
					setQCSection(table, i);
					count++;
				}
				// abschnittsweise interpoliert (wie abschnitt normal)
				else if (comp.equalsIgnoreCase(SECTION_INTERPOLATE)) {
					setQCSectionInterpolate(table, i);
					count++;
				}
				// division normal
				else if (comp.equalsIgnoreCase(DIVISION)) {
					setQCDivision(table, i);
					count++;
				}
				// division/subtraktion
				else if (comp.equalsIgnoreCase(DIVISION_DENOMINATOR)) {
					setQCDivisionDenominator(table, i);
					count++;
				}
				// ja/nein
				else if (comp.equalsIgnoreCase(YN)) {
					setQCYN(table, i);
					count++;
				} else if (comp.equalsIgnoreCase(FUZZY)) {
					setQCFuzzy(table, i);
					count++;
				} else {
					Logger.getLogger(this.getClass().getName()).warning(
							"Could not find QuestionComparator: " + comp);
				}
			}
		}

		report.note(MessageGenerator.addedSimilarities(table.getSheetName(),
				count));
		return report;
	}

	private void removeSimilarity(Question theQuestion) {
		while (true) {
			List<KnowledgeSlice> knowledgeSlices = (List<KnowledgeSlice>) theQuestion
					.getKnowledge(PSMethodShared.class,
							PSMethodShared.SHARED_SIMILARITY);
			if (knowledgeSlices != null && !knowledgeSlices.isEmpty())
				theQuestion.removeKnowledge(PSMethodShared.class,
						knowledgeSlices.get(0),
						PSMethodShared.SHARED_SIMILARITY);
			else
				break;
		}
	}

	private void setQCGroupedSymmetric(DecisionTable table, int rowIx) {
		GroupedComparator comp;
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		if (q instanceof QuestionOC)
			comp = new QuestionComparatorOCGroupedSymmetric();
		else if (q instanceof QuestionMC)
			comp = new QuestionComparatorMCGroupedSymmetric();
		else
			return;
		((QuestionComparator) comp).setQuestion(q);

		// matrix must have correct order of answers !!
		List<AnswerChoice> answerCols = new ArrayList<AnswerChoice>();
		for (int j = 2; j < table.columns(); j++) {
			if (!table.isEmptyCell(rowIx, j))
				answerCols.add(kbm.findAnswerChoice((QuestionChoice) q, table
						.get(rowIx, j)));
			else
				break;
		}
		for (int i = rowIx + 2; i < table.rows(); i++) {
			if (!table.isEmptyCell(i, 0))
				return;
			else {
				AnswerChoice a1 = kbm.findAnswerChoice((QuestionChoice) q,
						table.get(i, 1));
				for (int j = 2; j < table.columns() && j <= (i - rowIx); j++) {
					AnswerChoice a2 = answerCols.get(j - 2);
					if (a1 != null && a2 != null && a1 != a2) {
						try {
							Double value = Double.parseDouble(table.get(i, j)
									.replaceAll(",", "."));
							comp.addPairRelation(a1, a2, value);
						} catch (NumberFormatException e) {
							Logger.getLogger(this.getClass().getName())
									.warning(
											"Invalid value in matrix: "
													+ table.get(i, j));
						}
					}
				}
			}
		}
	}

	// Wer diese und die letzte Methode zusammenfassen will sollte die Indizes
	// in den Schleifen beachten !!!
	private void setQCGroupedAsymmetric(DecisionTable table, int rowIx) {
		GroupedComparator comp;
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		if (q instanceof QuestionOC)
			comp = new QuestionComparatorOCGroupedAsymmetric(); // !!
		else if (q instanceof QuestionMC)
			comp = new QuestionComparatorMCGroupedAsymmetric(); // !!
		else
			return;
		((QuestionComparator) comp).setQuestion(q);

		// matrix must have correct order of answers !!
		List<AnswerChoice> answerCols = new ArrayList<AnswerChoice>();
		for (int j = 2; j < table.columns(); j++) {
			if (!table.isEmptyCell(rowIx, j))
				answerCols.add(kbm.findAnswerChoice((QuestionChoice) q, table
						.get(rowIx, j)));
			else
				break;
		}
		for (int i = rowIx + 1; i < table.rows(); i++) {
			if (!table.isEmptyCell(i, 0))
				return;
			else {
				AnswerChoice a1 = kbm.findAnswerChoice((QuestionChoice) q,
						table.get(i, 1));
				for (int j = 2; j < table.columns()
						&& j < answerCols.size() + 2; j++) {
					AnswerChoice a2 = answerCols.get(j - 2);
					if (a1 != null && a2 != null && a1 != a2) {
						try {
							Double value = Double.parseDouble(table.get(i, j)
									.replaceAll(",", "."));
							comp.addPairRelation(a1, a2, value);
						} catch (NumberFormatException e) {
							Logger.getLogger(this.getClass().getName())
									.warning(
											"Invalid value in matrix: "
													+ table.get(i, j));
						}
					}
				}
			}
		}
	}

	private void setQCIndividual(DecisionTable table, int rowIx) {
		IndividualComparator comp;
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		if (q instanceof QuestionOC)
			comp = new QuestionComparatorOCIndividual();
		else if (q instanceof QuestionMC)
			comp = new QuestionComparatorMCIndividual();
		else if (q instanceof QuestionNum)
			comp = new QuestionComparatorNumIndividual();
		else
			return;
		((QuestionComparator) comp).setQuestion(q);
	}

	private void setQCScaled(DecisionTable table, int rowIx) {
		QuestionComparatorOCScaled comp = new QuestionComparatorOCScaled();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		if (!(q instanceof QuestionOC))
			return;
		comp.setQuestion(q);
		try {
			double constant = Double.parseDouble(table.get(rowIx, 2)
					.replaceAll(",", "."));
			comp.setConstant(constant);
		} catch (NumberFormatException e) {
			Logger.getLogger(this.getClass().getName()).warning(
					"Could not set normed value constant: "
							+ table.get(rowIx, 2));
		}
		List alternatives = ((QuestionOC) q).getAllAlternatives();
		List values = new LinkedList();
		for (int i = rowIx + 1; i < table.rows() && table.isEmptyCell(i, 0); i++) {
			AnswerChoice a = kbm.findAnswerChoice((QuestionOC) q, table.get(i,
					1));
			if (a == null)
				continue;
			int index = alternatives.indexOf(a);
			try {
				Double value = Double.parseDouble(table.get(i, 2).replaceAll(
						",", "."));
				values.add(index, value);
			} catch (NumberFormatException e) {
				Logger.getLogger(this.getClass().getName()).warning(
						"Invalid value: " + table.get(i, 2));
			}
		}
		comp.setValues(values);
	}

	private void setQCSection(DecisionTable table, int rowIx) {
		QuestionComparatorNumSection comp = new QuestionComparatorNumSection();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		comp.setQuestion(q);
		addValuePairs(comp, table, rowIx + 1);
	}

	private void setQCSectionInterpolate(DecisionTable table, int rowIx) {
		QuestionComparatorNumSectionInterpolate comp = new QuestionComparatorNumSectionInterpolate();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		comp.setQuestion(q);
		addValuePairs(comp, table, rowIx + 1);
	}

	private void addValuePairs(QuestionComparatorNumSection comp,
			DecisionTable table, int rowIx) {
		for (int i = rowIx; i < table.rows(); i++) {
			if (!table.isEmptyCell(i, 0))
				return;
			else {
				// leere Zeilen am Ende werden noch nicht erlaubt
				try {
					double xValue = Double.parseDouble(table.get(i, 1)
							.replaceAll(",", "."));
					double yValue = Double.parseDouble(table.get(i, 2)
							.replaceAll(",", "."));
					comp.addValuePair(xValue, yValue);
				} catch (NumberFormatException e) {
					Logger.getLogger(this.getClass().getName()).warning(
							"Invalid value pair: " + table.get(i, 1) + " "
									+ table.get(i, 2));
				}
			}
		}
	}

	private void setQCDivision(DecisionTable table, int rowIx) {
		QuestionComparatorNumDivision comp = new QuestionComparatorNumDivision();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		comp.setQuestion(q);
	}

	private void setQCDivisionDenominator(DecisionTable table, int rowIx) {
		QuestionComparatorNumDivisionDenominator comp = new QuestionComparatorNumDivisionDenominator();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		comp.setQuestion(q);
		try {
			double denominator = Double.parseDouble(table.get(rowIx, 2)
					.replaceAll(",", "."));
			comp.setDenominator(denominator);
		} catch (NumberFormatException e) {
			Logger.getLogger(this.getClass().getName()).warning(
					"Could not set denominator: " + table.get(rowIx, 2));
		}
	}

	private void setQCYN(DecisionTable table, int rowIx) {
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		QuestionComparatorYN comp = new QuestionComparatorYN();
		comp.setQuestion(q);
	}

	// Begin Change
	private void setQCFuzzy(DecisionTable table, int rowIx) {
		QuestionComparatorNumFuzzy nf = new QuestionComparatorNumFuzzy();
		if (table.get(rowIx, 2).equalsIgnoreCase(ABSOLUTE)) {
			nf
					.setInterpretationMethod(QuestionComparatorNumFuzzy.INTERPRETATION_METHOD_ABSOLUTE);
		} else if (table.get(rowIx, 2).equalsIgnoreCase(PERCENTAGE)) {
			nf
					.setInterpretationMethod(QuestionComparatorNumFuzzy.INTERPRETATION_METHOD_PRECENTAGE);
		} else {
			Logger.getLogger(this.getClass().getName()).warning(
					"Can't set interpretation method : " + table.get(rowIx, 2));
		}
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		String increasingLeft = table.get(rowIx, 3);
		String constLeft = table.get(rowIx, 4);
		String constRight = table.get(rowIx, 5);
		String decreasingRight = table.get(rowIx, 6);
		nf = this.setIncreasingLeft(increasingLeft, nf);
		nf = this.setConstLeft(constLeft, nf);
		nf = this.setConstRight(constRight, nf);
		nf = this.setDecresingRight(decreasingRight, nf);
		nf.setQuestion(q);
		q.addKnowledge(PSMethodShared.class, nf, PSMethodShared.SHARED_SIMILARITY);
		List simList = q.getKnowledge(PSMethodShared.class,
				PSMethodShared.SHARED_SIMILARITY);
	}

	private QuestionComparatorNumFuzzy setIncreasingLeft(String increasingLeft,
			QuestionComparatorNumFuzzy nf) {
		if (!increasingLeft.equals("")) {
			Double temp = QuestionComparatorNumFuzzy.stringToDouble(increasingLeft);
			nf.setIncreasingLeft(temp);
		}
		return nf;
	}

	private QuestionComparatorNumFuzzy setConstLeft(String constLeft,
			QuestionComparatorNumFuzzy nf) {
		if(constLeft.equals("+")){
			nf.setConstLeft(Double.POSITIVE_INFINITY);
		}
		else if (constLeft.equals("-")){
			nf.setConstLeft(Double.NEGATIVE_INFINITY);
		}
		else{
			nf.setConstLeft(Double.parseDouble(constLeft));
		}
		return nf;
	}

	private QuestionComparatorNumFuzzy setConstRight(String constRight,
			QuestionComparatorNumFuzzy nf) {
		if(constRight.equals("+")){
			nf.setConstRight(Double.POSITIVE_INFINITY);
		}
		else if (constRight.equals("-")){
			nf.setConstRight(Double.NEGATIVE_INFINITY);
		}
		else{
			nf.setConstRight(Double.parseDouble(constRight));
		}
		return nf;
	}

	private QuestionComparatorNumFuzzy setDecresingRight(
			String decreasingRight, QuestionComparatorNumFuzzy nf) {
		if (!decreasingRight.equals("")) {
			Double temp = QuestionComparatorNumFuzzy.stringToDouble(decreasingRight);
			nf.setDecreasingRight(temp);
		}
		return nf;
	}

}
