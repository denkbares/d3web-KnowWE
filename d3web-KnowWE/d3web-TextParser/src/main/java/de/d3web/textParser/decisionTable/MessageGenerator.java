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

import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.report.Message;
import de.d3web.textParser.Utils.*;
import java.util.*;
import java.text.*;

public class MessageGenerator {

	private static final ResourceBundle rb = ResourceBundle
			.getBundle("properties.DecisionTableMessages");

	public static Message noXLSFile() {
		return createMessage("noXLSFile");
	}

	public static Message errorReadingXLS(String error) {
		return createMessage("errorReadingXLS", error);
	}

	public static Message missingSyntaxChecker() {
		return createMessage("missing.SyntaxChecker");
	}

	public static Message missingValueChecker() {
		return createMessage("missing.ValueChecker");
	}

	public static Message missingKnowledgeGenerator() {
		return createMessage("missing.KnowledgeGenerator");
	}

	public static Message foundTables(int i) {
		if (i <= 0)
			return createMessage("noTables");
		return createMessage("foundTables", i);
	}
	
	public static Message usingDefault(int row, int column, String value) {
		String text = rb.getString("default.used");
		return new Message(Message.NOTE, text+": \""+value+"\"", null,row, column,null);
	}

	public static Message diagnosisHasRules(String diagnosisName) {
		return createMessage("diagnosisHasRules", diagnosisName);
	}

	public static Message removedRules(String diagnosisName, int count) {
		return createMessage("knowledge.removedRules", diagnosisName, count);
	}

	public static Message addedRules(String sheetName, int count) {
		return createMessage("knowledge.createdRules", sheetName, count);
	}

	public static Message addedSCRelations(String sheetName, int count) {
		return createMessage("knowledge.createdSCRelations", sheetName, count);
	}

	public static Message removedAttributes(String objectName, int count) {
		return createMessage("knowledge.removedAttributes", objectName, count);
	}

	public static Message addedAttributes(String sheetName, int count) {
		return createMessage("knowledge.createdAttributes", sheetName, count);
	}

	public static Message addedSimilarities(String sheetName, int count) {
		return createMessage("knowledge.addedSimilarities", sheetName, count);
	}

	public static Message renamedObjects(String sheetName, int qContainers,
			int questions, int answers, int diagnoses) {
		int overallCount = qContainers + diagnoses + questions + answers;
		return createMessage("knowledge.renamedObjects", sheetName,
				overallCount, qContainers, questions, answers, diagnoses);
	}

	public static Message fieldNotEmpty(int row, int column, String value) {
		return createMessage(row, column, "fieldNotEmpty", value);
	}

	public static Message fieldEmpty(int row, int column) {
		return createMessage(row, column, "fieldEmpty");
	}

	public static Message notEnoughRows(int minRows) {
		return createMessage("notEnoughRows", minRows);
	}

	public static Message notEnoughColumns(int minCols) {
		return createMessage("notEnoughColumns", minCols);
	}

	public static Message doubledValue(int row, int column, String value) {
		return createMessage(row, column, "doubledValue", value);
	}

	public static Message replace_NotUnique(int row, int column, String value) {
		return createMessage(row, column, "replace.notUnique", value);
	}

	public static Message replace_AlreadyExists(int row, int column,
			String value) {
		return createMessage(row, column, "replace.alreadyExists", value);
	}

	public static Message replace_Circle(String value) {
		return createMessage("replace.circle", value);
	}

	public static Message replace_MultipleReplacements(int row, int column,
			String value) {
		return createMessage(row, column, "replace.multipleReplacements", value);
	}

	public static Message replace_MultipleNames(int row, int column,
			String value) {
		return createMessage(row, column, "replace.multipleNames", value);
	}

	// **************
	// missing Values

	public static Message missingAttributeDescriptor(int row, int column) {
		return createMessage(row, column, "missing.AttributeDescriptor");
	}

	public static Message missingLinkDescriptor(int row, int column) {
		return createMessage(row, column, "missing.LinkDescriptor");
	}

	public static Message missingImageDescriptor(int row, int column) {
		return createMessage(row, column, "missing.ImageDescriptor");
	}

	public static Message missingAnswer(int row, int column) {
		return createMessage(row, column, "missing.Answer");
	}

	public static Message missingDiagnosis(int row, int column) {
		return createMessage(row, column, "missing.Diagnosis");
	}

	public static Message missingDiagnosisState(int row, int column) {
		return createMessage(row, column, "missing.RuleDiagnoses");
	}

	public static Message missingAnswerForQuestion(int row, int column, String q) {
		return createMessage(row, column, "missing.AnswerForQuestion", q);
	}

	public static Message missingRuleDiagnoses() {
		return createMessage("missing.RuleDiagnoses");
	}

	public static Message missingScore(int row, int column) {
		return createMessage(row, column, "missing.Score");
	}

	public static Message missingLogicalOperator(int row, int column) {
		return createMessage(row, column, "missing.LogicalOperator");
	}

	public static Message missingLogicalOperatorColumn(int row, int column) {
		return createMessage(row, column, "missing.LogicalOperatorColumn");
	}

	public static Message missingCondition(int row, int column) {
		return createMessage(row, column, "missing.Condition");
	}

	public static Message missingSign(int row, int column) {
		return createMessage(row, column, "missing.Sign");
	}
	

	public static Message missingObjectName(int row, int column) {
		return createMessage(row, column, "missing.objectName");
	}

	public static Message missingReplaceName(int row, int column) {
		return createMessage(row, column, "missing.replaceName");
	}

	public static Message missingQuestionComparator(int row, int column) {
		return createMessage(row, column, "missing.questionComparator");
	}

	public static Message missingConstant(int row, int column) {
		return createMessage(row, column,
				"missing.questionComparator.scaled.constant");
	}

	public static Message missingDenominator(int row, int column) {
		return createMessage(row, column,
				"missing.questionComparator.division.denominator");
	}

	public static Message missingValue(int row, int column) {
		return createMessage(row, column, "missing.value");
	}

	// **************
	// invalid values

	public static Message invalidValue(int row, int column, String value) {
		return createMessage(row, column, "invalid.Value", value);
	}

	public static Message invalidObjectName(int row, int column, String value) {
		return createMessage(row, column, "invalid.objectName", value);
	}

	public static Message missingDiagnosisForSharedLocalWeight(int row,
			int column, String sharedLocalWeight, String value) {
		return createMessage(row, column,
				"missing.Diagnosis.SharedLocalWeight", sharedLocalWeight, value);
	}

	public static Message invalidDiagnosisForSharedLocalWeight(int row,
			int column, String sharedLocalWeight, String diagnosis) {
		return createMessage(row, column,
				"invalid.Diagnosis.SharedLocalWeight", sharedLocalWeight,
				diagnosis);
	}

	public static final String KEY_INVALID_DIAGNOSIS = "invalid.Diagnosis";

	public static Message invalidDiagnosis(int row, int column, String value) {
		return createMessage(row, column, KEY_INVALID_DIAGNOSIS, value);
	}

	public static Message invalidDiagnosisState(int row, int column,
			String value) {
		return createMessage(row, column, "invalid.DiagnosisState", value);
	}

	public static Message invalidAttribute(int row, int column, String value) {
		return createMessage(row, column, "invalid.Attribute", value);
	}

	public static Message invalidAttributeValue(int row, int column,
			String attributeName, String value) {
		return createMessage(row, column, "invalid.AttributeValue",
				attributeName, value);
	}

	public static Message invalidScore(int row, int column, String value) {
		return createMessage(row, column, "invalid.Score", value);
	}

	public static Message invalidQASet(int row, int column, String value) {
		return createMessage(row, column, "invalid.QASet", value);
	}

	public static Message invalidQContainer(int row, int column, String value) {
		return createMessage(row, column, "invalid.QContainer", value);
	}

	public static Message invalidNotQASetOrDiagnosis(int row, int column,
			String value) {
		return createMessage(row, column, "invalid.NotQASetOrDiagnosis", value);
	}

	public static final String KEY_INVALID_QUESTION = "invalid.Question";

	public static Message invalidQuestion(int row, int column, String value) {
		return createMessage(row, column, KEY_INVALID_QUESTION, value);

	}

	public static Message invalidQuestion(int row, int column, String value,
			int type) {
		return createMessage(row, column, KEY_INVALID_QUESTION, new Object[] {
				value, new Integer(type) });

	}

	public static Message invalidQuestionChoice(int row, int column,
			String value) {
		return createMessage(row, column, "invalid.QuestionChoice", value);
	}

	public static Message invalidAnswer(int row, int column, String qName,
			String value) {
		return createMessage(row, column, KEY_INVALID_ANSWER, qName, value);
	}

	public static Message invalidAnswer(int row, int column, String qName,
			String value, Question q) {
		return createMessage(row, column, KEY_INVALID_ANSWER, qName, value, q);
	}

	public static final String KEY_INVALID_ANSWER = "invalid.Answer";

	public static Message invalidAnswerOrder(int row, int column, String value) {
		return createMessage(row, column, "invalid.AnswerOrder", value);
	}

	public static Message invalidAnswerNum(int row, int column, String value) {
		return createMessage(row, column, "invalid.", value);
	}

	public static Message invalidAnswerNumInOperator(int row, int column,
			String value) {
		return createMessage(row, column, "invalid.AnswerNum.inOperator", value);
	}

	public static Message invalidAnswerNumNumberOrOperator(int row, int column,
			String value) {
		return createMessage(row, column, "invalid.AnswerNum.numberOrOperator",
				value);
	}

	public static Message invalidLogicalOperator(int row, int column,
			String value) {
		return createMessage(row, column, "invalid.LogicalOperator", value);
	}

	public static Message invalidMinMax(int row, int column, String value) {
		return createMessage(row, column, "invalid.minmax", value);
	}

	public static Message invalidNumberOfAnswers(int row, int column,
			int number, int expected) {
		return createMessage(row, column, "invalid.numberOfAnswers", number,
				expected);
	}

	public static Message invalidSign(int row, int column, String value) {
		return createMessage(row, column, "invalid.Sign", value);
	}

	public static Message invalidQuestionComparator(int row, int column,
			String value) {
		return createMessage(row, column, "invalid.questionComparator", value);
	}

	public static Message invalidQuestionTypeForQC(int row, int column,
			String qc, String qType) {
		return createMessage(row, column,
				"invalid.questionComparator.questionType", qc, qType);
	}

	public static Message invalidConstant(int row, int column, String value) {
		return createMessage(row, column,
				"invalid.questionComparator.scaled.constant", value);
	}

	public static Message invalidDenominator(int row, int column, String value) {
		return createMessage(row, column,
				"invalid.questionComparator.division.denominator", value);
	}

	public static Message multiplicityNotAllowed(int row, int column,
			String value) {
		return createMessage(row, column, "notAllowed.multiplicity", value);
	}

	public static Message AttributeNotAllowedForObject(int row, int column,
			String attributeName, String className) {
		return createMessage(row, column, "notAllowed.AttributeForObject",
				attributeName, className);
	}

	public static Message abnormalityValueNotAllowedForQuestion(int row,
			int column) {
		return createMessage(row, column,
				"notAllowed.AbnormalityValueForQuestion");
	}

	public static Message localWeightValueNotAllowedForQuestion(int row,
			int column) {
		return createMessage(row, column,
				"notAllowed.LocalWeightValueForQuestion");
	}

	public static Message num2ChoiceNotAllowedForQuestion(int row, int column) {
		return createMessage(row, column, "notAllowed.Num2ChoiceForQuestion");
	}

	public static Message createMessage(String key, Object... values) {
		return createMessage(-1, -1, key, values);
	}

	public static Message createMessage(int row, int column, String key,
			Object... values) {
		String result = rb.getString("unknownError") + ": " + key;
		try {
			result = MessageFormat.format(rb.getString(key), values);
		} catch (MissingResourceException e) {
		}
		Message m = new Message(result);
		if (values != null && values.length > 0) {
			ConceptNotInKBError error = null;
			String objectName = null;
			if (key.equals(KEY_INVALID_DIAGNOSIS)) {
				error = new ConceptNotInKBError(result);
				objectName = (String)values[0];
			}
			if (key.equals(KEY_INVALID_QUESTION)) {
				objectName = (String)values[0];
				if (values.length > 1 && values[1] != null && values[1] instanceof Integer) {
					error = new QuestionNotInKBError(result,
							(Integer) values[1]);
				} else {
					error = new QuestionNotInKBError(result);
				}
			}
			if (key.equals(KEY_INVALID_ANSWER)) {
				if (values.length >= 3) {
					AnswerNotInKBError answerError = new AnswerNotInKBError(
							result);
					if (values[2] != null && values[2] instanceof Question) {
						answerError.setQuestion((Question) values[2]);
						objectName = (String)values[1];
					}
					error = answerError;
				}
			}

			if (error != null) {
				error.setObjectName(objectName);
				error.setKey(key);
				m = error;
			}
		}
		if(row != 0) {
			m.setLineNo(row);
		}
		if (row >= 0 && column >= 0)
			m.setLocation(DecisionTable.columnDescriptor(column) + (row + 1));
		return m;
	}

}
