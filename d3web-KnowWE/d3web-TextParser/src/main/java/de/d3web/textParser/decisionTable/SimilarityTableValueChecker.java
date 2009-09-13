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

package de.d3web.textParser.decisionTable;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionMC;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionOC;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.report.Report;

/**
 * Creation date: (31.10.2005)
 * 
 * @author Andreas
 * 
 */
public class SimilarityTableValueChecker extends DecisionTableValueChecker {

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

	private static String FUZZY = rb.getString("similarity.comparator.fuzzy");

	private List comparators;

	public SimilarityTableValueChecker(KnowledgeBaseManagement kbm) {
		super(null, kbm);
		comparators = getComparators();
	}

	@Override
	public Report checkValues(DecisionTable table) {
		Report report = new Report();
		report.addAll(checkQuestions(table, 0, table.rows()));
		report.addAll(checkQuestionComparators(table));
		report.addAll(checkComparatorSpecificValues(table));
		return report;
	}

	private List<String> getComparators() {
		List<String> ret = new ArrayList<String>();
		ret.add(GROUPED_SYMMETRIC.toLowerCase());
		ret.add(GROUPED_SYMMETRIC_ABBR.toLowerCase());
		ret.add(GROUPED_ASYMMETRIC.toLowerCase());
		ret.add(GROUPED_ASYMMETRIC_ABBR.toLowerCase());
		ret.add(INDIVIDUAL.toLowerCase());
		ret.add(SCALED.toLowerCase());
		ret.add(SECTION.toLowerCase());
		ret.add(SECTION_INTERPOLATE.toLowerCase());
		ret.add(DIVISION.toLowerCase());
		ret.add(DIVISION_DENOMINATOR.toLowerCase());
		ret.add(YN.toLowerCase());
		// Change
		ret.add(FUZZY.toLowerCase());
		return ret;
	}

	/**
	 * checks if comparators are valid
	 * 
	 * @param table
	 * @return
	 */
	private Report checkQuestionComparators(DecisionTable table) {
		Report report = new Report();
		for (int i = 0; i < table.rows(); i++) {
			if (!table.isEmptyCell(i, 0)) {
				String comp = table.get(i, 1).toLowerCase();
				if (comp.equals(""))
					report.error(MessageGenerator.missingQuestionComparator(i,
							1));
				else if (!comparators.contains(comp))
					report.error(MessageGenerator.invalidQuestionComparator(i,
							1, comp));

			}
		}

		return report;
	}

	private Report checkComparatorSpecificValues(DecisionTable table) {
		Report report = new Report();
		for (int i = 0; i < table.rows(); i++) {
			if (!table.isEmptyCell(i, 0)) {
				String comp = table.get(i, 1);
				// symmetrische ï¿½hnlichkeitsmatrix
				if (comp.equalsIgnoreCase(GROUPED_SYMMETRIC)
						|| comp.equalsIgnoreCase(GROUPED_SYMMETRIC_ABBR))
					report.addAll(checkQCGroupedSymmetricValues(table, i));
				// asymmetrische ï¿½hnlichkeitsmatrix
				if (comp.equalsIgnoreCase(GROUPED_ASYMMETRIC)
						|| comp.equalsIgnoreCase(GROUPED_ASYMMETRIC_ABBR))
					report.addAll(checkQCGroupedAsymmetricValues(table, i));
				// individuell
				else if (comp.equalsIgnoreCase(INDIVIDUAL)) {
					report.addAll(checkQCIndividualValues(table, i));
				}
				// intervall-schema
				else if (comp.equalsIgnoreCase(SCALED)) {
					report.addAll(checkQCScaledValues(table, i));
				}
				// abschnitt normal
				else if (comp.equalsIgnoreCase(SECTION)) {
					report.addAll(checkQCSectionValues(table, i));
				}
				// abschnittsweise interpoliert (wie abschnitt normal)
				else if (comp.equalsIgnoreCase(SECTION_INTERPOLATE)) {
					report.addAll(checkQCSectionInterpolateValues(table, i));
				}
				// division normal
				else if (comp.equalsIgnoreCase(DIVISION)) {
					report.addAll(checkQCDivisionValues(table, i));
				}
				// division/subtraktion
				else if (comp.equalsIgnoreCase(DIVISION_DENOMINATOR)) {
					report.addAll(checkQCDivisionDenominatorValues(table, i));
				}
				// ja/nein
				else if (comp.equalsIgnoreCase(YN)) {
					report.addAll(checkQCYNValues(table, i));
				} 
				else if (comp.equalsIgnoreCase(FUZZY)) {
					report.addAll(checkQCFuzzyValues(table, i));
				}
			}
		}
		return report;
	}

	private int getNextQuestionRowIndex(DecisionTable table, int rowIx) {
		for (int i = rowIx; i < table.rows(); i++) {
			if (!table.isEmptyCell(i, 0))
				return i;
		}
		return table.rows() - 1;
	}

	private Report checkQCGroupedSymmetricValues(DecisionTable table, int rowIx) {
		Report report = new Report();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		// TODO franz: q == null?
		if (q != null && !(q instanceof QuestionOC || q instanceof QuestionMC)) {
			report.error(MessageGenerator.invalidQuestionTypeForQC(rowIx, 0,
					GROUPED_SYMMETRIC, q.getClass().getSimpleName()));
			return report;
		}
		// TODO: check matrix
		return report;
	}

	private Report checkQCGroupedAsymmetricValues(DecisionTable table, int rowIx) {
		Report report = new Report();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		// TODO franz: q == null?
		if (q != null && !(q instanceof QuestionOC || q instanceof QuestionMC)) {
			report.error(MessageGenerator.invalidQuestionTypeForQC(rowIx, 0,
					GROUPED_ASYMMETRIC, q.getClass().getSimpleName()));
			return report;
		}
		// TODO: check matrix
		return report;
	}

	private Report checkQCIndividualValues(DecisionTable table, int rowIx) {
		Report report = new Report();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		// TODO franz: was soll wirklich passieren, falls q == null?
		if (q != null
				&& !(q instanceof QuestionOC || q instanceof QuestionMC || q instanceof QuestionNum)) {
			report.error(MessageGenerator.invalidQuestionTypeForQC(rowIx, 0,
					INDIVIDUAL, q.getClass().getSimpleName()));
			return report;
		}
		report.addAll(checkAllEmpty(table, rowIx));
		return report;
	}

	private Report checkQCScaledValues(DecisionTable table, int rowIx) {
		Report report = new Report();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		// TODO franz: q == null?
		if (q != null && !(q instanceof QuestionOC)) {
			report.error(MessageGenerator.invalidQuestionTypeForQC(rowIx, 0,
					SCALED, q.getClass().getSimpleName()));
			return report;
		}
		// check constant
		if (table.isEmptyCell(rowIx, 2)) {
			report.error(MessageGenerator.missingConstant(rowIx, 2));
		} else {
			try {
				Double.parseDouble(table.get(rowIx, 2));
			} catch (NumberFormatException e) {
				report.error(MessageGenerator.invalidConstant(rowIx, 2, table
						.get(rowIx, 2)));
			}
		}
		// check valuePairs
		List alternatives = ((QuestionOC) q).getAllAlternatives();
		int answerCount = 0;
		for (int i = rowIx + 1; i < table.rows() && table.isEmptyCell(i, 0); i++) {
			if (table.isEmptyRow(i))
				continue;
			String answerText = table.get(i, 1);
			if (answerText.equals(""))
				report.error(MessageGenerator.missingAnswer(i, 1));
			else {
				AnswerChoice a = kbm.findAnswerChoice((QuestionOC) q,
						answerText);
				answerCount++;
				// check if answer exists
				if (a == null) {
					report.error(MessageGenerator.invalidAnswer(i, 1, q
							.getText(), answerText));
					continue;
				}
				// check if answer is at right position
				int expectedIndex = alternatives.indexOf(a);
				int index = i - (rowIx + 1);
				if (expectedIndex != index) {
					report.error(MessageGenerator.invalidAnswerOrder(i, 1,
							answerText));
				}
			}
			// check value
			if (table.isEmptyCell(i, 2))
				report.error(MessageGenerator.missingValue(i, 2));
			else {
				try {
					Double value = Double.parseDouble(table.get(i, 2)
							.replaceAll(",", "."));
				} catch (NumberFormatException e) {
					report.error(MessageGenerator.invalidValue(i, 2, table.get(
							i, 2)));
				}
			}
		}
		// check if number of answers is correct
		if (alternatives.size() != answerCount)
			report.error(MessageGenerator.invalidNumberOfAnswers(rowIx, 0,
					answerCount, alternatives.size()));
		return report;
	}

	private Report checkQCSectionValues(DecisionTable table, int rowIx) {
		Report report = new Report();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		// TODO franz: q == null?
		if (q != null && !(q instanceof QuestionNum)) {
			report.error(MessageGenerator.invalidQuestionTypeForQC(rowIx, 0,
					SECTION, q.getClass().getSimpleName()));
			return report;
		}
		// TODO: values
		return report;
	}

	private Report checkQCSectionInterpolateValues(DecisionTable table,
			int rowIx) {
		Report report = new Report();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		// TODO franz: q == null?
		if (q != null && !(q instanceof QuestionNum)) {
			report.error(MessageGenerator.invalidQuestionTypeForQC(rowIx, 0,
					SECTION_INTERPOLATE, q.getClass().getSimpleName()));
			return report;
		}
		// TODO: check values
		return report;
	}

	private Report checkQCDivisionValues(DecisionTable table, int rowIx) {
		Report report = new Report();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		// TODO franz: q == null?
		if (q != null && !(q instanceof QuestionNum)) {
			report.error(MessageGenerator.invalidQuestionTypeForQC(rowIx, 0,
					DIVISION, q.getClass().getSimpleName()));
			return report;
		}
		report.addAll(checkAllEmpty(table, rowIx));
		return report;
	}

	private Report checkQCDivisionDenominatorValues(DecisionTable table,
			int rowIx) {
		Report report = new Report();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		// TODO franz: q == null?
		if (q != null && !(q instanceof QuestionNum)) {
			report.error(MessageGenerator.invalidQuestionTypeForQC(rowIx, 0,
					DIVISION_DENOMINATOR, q.getClass().getSimpleName()));
			return report;
		}
		// check denominator
		if (table.isEmptyCell(rowIx, 2)) {
			report.error(MessageGenerator.missingDenominator(rowIx, 2));
		} else {
			try {
				Double.parseDouble(table.get(rowIx, 2));
			} catch (NumberFormatException e) {
				report.error(MessageGenerator.invalidDenominator(rowIx, 2,
						table.get(rowIx, 2)));
			}
		}
		// check if all other fields are empty
		for (int j = 3; j < table.columns(); j++) {
			if (!table.isEmptyCell(rowIx, j))
				report.error(MessageGenerator.fieldNotEmpty(rowIx, j, table
						.get(rowIx, j)));
		}
		for (int i = rowIx + 1; i < table.rows(); i++) {
			if (!table.isEmptyCell(i, 0))
				break;
			for (int j = 1; j < table.columns(); j++) {
				if (!table.isEmptyCell(i, j))
					report.error(MessageGenerator.fieldNotEmpty(i, j, table
							.get(i, j)));
			}
		}
		return report;
	}

	private Report checkQCYNValues(DecisionTable table, int rowIx) {
		Report report = new Report();
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		// TODO franz: q == null?
		if (q != null && !(q instanceof QuestionYN)) {
			report.error(MessageGenerator.invalidQuestionTypeForQC(rowIx, 0,
					YN, q.getClass().getSimpleName()));
			return report;
		}
		report.addAll(checkAllEmpty(table, rowIx));
		return report;
	}

	private Report checkAllEmpty(DecisionTable table, int rowIx) {
		Report report = new Report();
		for (int j = 2; j < table.columns(); j++) {
			if (!table.isEmptyCell(rowIx, j))
				report.error(MessageGenerator.fieldNotEmpty(rowIx, j, table
						.get(rowIx, j)));
		}
		for (int i = rowIx + 1; i < table.rows(); i++) {
			if (!table.isEmptyCell(i, 0))
				break;
			for (int j = 1; j < table.columns(); j++) {
				if (!table.isEmptyCell(i, j))
					report.error(MessageGenerator.fieldNotEmpty(i, j, table
							.get(i, j)));
			}
		}
		return report;
	}

	private Report checkQCFuzzyValues(DecisionTable table, int rowIx) {
		Question q = kbm.findQuestion(table.get(rowIx, 0));
		Report report = new Report();
		if (q instanceof QuestionNum) {
			String increasingLeft = table.get(rowIx, 3);
			String constLeft = table.get(rowIx, 4);
			String constRight = table.get(rowIx, 5);
			String decreasingRight = table.get(rowIx, 6);
			report.addAll(this.isDouble(increasingLeft, 3, rowIx));
			report.addAll(this.isDouble(constLeft, 4, rowIx));
			report.addAll(this.isDouble(constRight, 5, rowIx));
			report.addAll(this.isDouble(decreasingRight, 6, rowIx));
			try {
				double increasingLeftDouble = Double
						.parseDouble(increasingLeft);
				double constLeftDouble = getValue(constLeft);
				double constRightDouble = getValue(constRight);
				double decreasingRightDouble = Double
						.parseDouble(decreasingRight);
				/**if (increasingLeftDouble <= constLeftDouble
						&& constLeftDouble <= constRightDouble
						&& constRightDouble <= decreasingRightDouble) {
				} else {
					report.error(MessageGenerator.invalidValue(rowIx, 0,
					"values are not valid : (in ascending order?)"));
				}**/
			} catch (Exception ex) {
				// wird schon bei isDouble behandelt!
			}
		} else {
			report.add(MessageGenerator.createMessage(rowIx, 0,
					"noQuestionNum", q.getText()));
		}
		return report;
	}

	private Report isDouble(String str, int column, int row) {
		//Begin Change
		/*Report report = new Report();
		if (str.equals("") && column == 4) {
			report.error(MessageGenerator.invalidValue(row, column,
					" constLeft (end increasing; staying at 1)  = \"\"" + str));
			return report;
		} else if (str.equals("") && column == 5) {
			report.error(MessageGenerator.invalidValue(row, column,
					" constRight (start decreasing to 0) = \"\"" + str));
			return report;
		} else if (str.equals("")) {
			return report;
		}
		try {
			Double.parseDouble(str);
		} catch (Exception ex) {
			report.error(MessageGenerator.invalidValue(row, column, str));
		}
		return report;
	}*/
	//End Change
		Report report = new Report();
		if (str.equals("")) {
			report.error(MessageGenerator.invalidValue(row, column,
					"double value is missing!"));
			return report;
		} else if ((str.equals("+") || str.equals("-")) && (column == 5 || column == 4)) {
			return report; 
		}
		try {
			Double.parseDouble(str);
		} catch (Exception ex) {
			report.error(MessageGenerator.invalidValue(row, column, str));
		}
		return report;
	}

	private Double getValue(String doubleValue){
		if(doubleValue.equals("+")){
			return Double.POSITIVE_INFINITY;
		}
		else if (doubleValue.equals("-")){
			return Double.NEGATIVE_INFINITY;
		}
		else{
			return Double.parseDouble(doubleValue);
		}
	}
}
