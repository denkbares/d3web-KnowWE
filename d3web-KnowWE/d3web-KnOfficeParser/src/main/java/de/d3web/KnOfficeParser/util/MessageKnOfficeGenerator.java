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

package de.d3web.KnOfficeParser.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.RecognitionException;

import de.d3web.kernel.domainModel.NumericalInterval;
import de.d3web.report.Message;
/**
 * Generieungsklasse für Fehlermeldung
 * @author Markus Friedrich
 *
 */
public class MessageKnOfficeGenerator {
	private static ResourceBundle rb = ResourceBundle.getBundle("errors");
	
	public static ResourceBundle getResourceBundle() {
		return rb;
	}
	
	public static Message createLexerNVAE(String file, RecognitionException re) {
		return createErrorMSG("lexernvae", file, re.line, "", ErrorMsg.getCharString(re.c));
	}
	
	public static Message createLexerMTE(String file, MismatchedTokenException mte) {
		return createErrorMSG("lexermte", file, mte.line, "", ErrorMsg.getCharString(mte.c), ErrorMsg.getCharString(mte.expecting));
	}
	
	public static Message createUnknownLexerError(String file, RecognitionException re) {
		return createErrorMSG("unknownReadError", file, re.line, "", "Lexer");
	}
	
	public static Message createPropertieError(String file, String propertiefile) {
		return createErrorMSG("propertyfile", file, 0, "", propertiefile);
	}
	
	public static Message createParserNVAE(String file, RecognitionException re) {
		return createErrorMSG("parsernvae", file, re.line, "", ErrorMsg.getTokenString(re.token));
	}
	
	public static Message createEmptyLineEndingException(String file, RecognitionException re) {
		return createErrorMSG("emptylineending", file, re.line, "");
	}
	
	public static Message createParserMTE(String file, MismatchedTokenException mte, String expected) {
		return createErrorMSG("parsermte", file, mte.line, "", expected, ErrorMsg.getTokenString(mte.token));
	}
	
	public static Message createUnknownParserError(String file, RecognitionException re) {
		return createErrorMSG("unknownReadError", file, re.line, "", "Parser");
	}
	
	public static Message createQuestionNotFoundException(String file, int line, String linetext, String question) {
		return createErrorMSG("qnf", file, line, linetext, question);
	}
	
	public static Message createQuestionNotFoundException(String file, int line, int column, String linetext, String question) {
		return createErrorMSG("qnf", file, line, column, linetext, question);
	}
	
	public static Message createTypeRecognitionError(String file, int line, String linetext, String question, String type) {
		return createErrorMSG("typerecognitionerror", file, line, linetext, question, type);
	}
	
	public static Message createTypeMismatchWarning(String file, int line, String linetext, String question, String type) {
		return createWarningMSG("typemismatch", file, line, linetext, question, type);
	}
	
	public static Message createTypeMismatchWarning(String file, int line, int column, String linetext, String question, String type) {
		return createWarningMSG("typemismatch", file, line, column, linetext, question, type);
	}
	
	public static Message createNaNException(String file, int line, String linetext, String number) {
		return createErrorMSG("nan", file, line, linetext, number);
	}
	
	public static Message createUnknownOpException(String file, int line, String linetext, String op) {
		return createErrorMSG("unknownOp", file, line, linetext, op);
	}
	
	public static Message createWrongYNAnswer(String file, int line, String linetext, String question) {
		return createErrorMSG("ynQuestion", file, line, linetext, question);
	}
	
	public static Message createAnswerNotFoundException(String file, int line, String linetext, String answer, String question) {
		return createErrorMSG("anf", file, line, linetext, answer, question);
	}
	
	public static Message createAnswerNotFoundException(String file, int line, int column, String linetext, String answer, String question) {
		return createErrorMSG("anf", file, line, column, linetext, answer, question);
	}
	
	public static Message createNoAnswerAllowedException(String file, int line, String linetext) {
		return createErrorMSG("naa", file, line, linetext);
	}
	
	public static Message createIntervallRangeError(String file, int line, String linetext) {
		return createErrorMSG("intervallRangeError", file, line, linetext);
	}
	
	public static Message createIntervallQuestionError(String file, int line, String linetext) {
		return createErrorMSG("intervallQuestionError", file, line, linetext);
	}
	
	public static Message createNoValidCondsException(String file, int line, String linetext) {
		return createErrorMSG("noConds", file, line, linetext);
	}
	
	public static Message createQuestionClassNotFoundException(String file, int line, String linetext, String qc) {
		return createErrorMSG("qcnf", file, line, linetext, qc);
	}
	
	public static Message createQuestionClassNotFoundException(String file, int line, int column, String linetext, String qc) {
		return createErrorMSG("qcnf", file, line, column, linetext, qc);
	}
	
	public static Message createQuestionClassorQuestionNotFoundException(String file, int line, String linetext, String qc) {
		return createErrorMSG("qcnf", file, line, linetext, qc);
	}
	
	public static Message createNoValidQuestionsException(String file, int line, String linetext) {
		return createErrorMSG("noQuestion", file, line, linetext);
	}
	
	public static Message createNoValidAnswerException(String file, int line, String linetext) {
		return createErrorMSG("noAnswer", file, line, linetext);
	}
	
	public static Message createSupressError(String file, int line, String linetext) {
		return createErrorMSG("supress", file, line, linetext);
	}
	
	public static Message createWrongOperatorInAbstractionRule(String file, int line, String linetext) {
		return createErrorMSG("wrongOpAbs", file, line, linetext);
	}
	
	public static Message createQuestionOrDiagnosisNotFoundException(String file, int line, String linetext, String question) {
		return createErrorMSG("qodnf", file, line, linetext, question);
	}
	
	public static Message createOnlyNumOrChoiceAllowedError(String file, int line, String linetext) {
		return createErrorMSG("onlyNumOrChoice", file, line, linetext);
	}
	
	public static Message createWrongOperatorForDiag(String file, int line, String linetext) {
		return createErrorMSG("wrongOpDiag", file, line, linetext);
	}
	
	public static Message createScoreDoesntExistError(String file, int line, String linetext, String score) {
		return createErrorMSG("scoreMissing", file, line, linetext, score);
	}
	
	public static Message createScoreDoesntExistError(String file, int line, int column, String linetext, String score) {
		return createErrorMSG("scoreMissing", file, line, column, linetext, score);
	}
	
	public static Message createWrongOperatorforChoiceQuestionsException(String file, int line, String linetext) {
		return createErrorMSG("wrongOpChoice", file, line, linetext);
	}
	
	public static Message createOnlyNumInFormulaError(String file, int line, String linetext) {
		return createErrorMSG("onlyNumInFormula", file, line, linetext);
	}
	
	public static Message createOnlyNumOrDoubleError(String file, int line, String linetext) {
		return createErrorMSG("onlyNumOrDouble", file, line, linetext);
	}
	
	public static Message createAntlrInputError(String file, int line, String linetext) {
		return createErrorMSG("antlrInputError", file, line, linetext);
	}
	
	public static Message createDiagnosisNotFoundException(String file, int line, String linetext, String diagnosis) {
		return createErrorMSG("dnf", file, line, linetext, diagnosis);
	}
	
	public static Message createDiagnosisNotFoundException(String file, int line, int column, String linetext, String diagnosis) {
		return createErrorMSG("dnf", file, line, column, linetext, diagnosis);
	}
	
	public static Message createAnswerNotNumericException(String file, int line, String linetext, String answer) {
		return createErrorMSG("ann", file, line, linetext, answer);
	}
	
	public static Message createAnswerNotNumericException(String file, int line, int column, String linetext, String answer) {
		return createErrorMSG("ann", file, line, column, linetext, answer);
	}
	
	public static Message createAnswerNotYNException(String file, int line, String linetext, String answer) {
		return createErrorMSG("anyn", file, line, linetext, answer);
	}
	
	public static Message createAnswerNotYNException(String file, int line, int column, String linetext, String answer) {
		return createErrorMSG("anyn", file, line, column, linetext, answer);
	}
	
	public static Message createAnswerCreationUnambiguousException(String file, int line, String linetext, String answer) {
		return createErrorMSG("acu", file, line, linetext, answer);
	}
	
	public static Message createAnswerCreationUnambiguousException(String file, int line, int column, String linetext, String answer) {
		return createErrorMSG("acu", file, line, column, linetext, answer);
	}
	
	public static Message createQuestionTypeNotSupportetException(String file, int line, String linetext, String question) {
		return createErrorMSG("qtns", file, line, linetext, question);
	}
	
	public static Message createQuestionTypeNotSupportetException(String file, int line, int column, String linetext, String question) {
		return createErrorMSG("qtns", file, line, linetext, column, question);
	}
	
	public static Message createNoXlsFileException(String file, int line, String linetext) {
		return createErrorMSG("nxls", file, line, linetext);
	}
	
	public static Message createNoValidWeightException(String file, int line, String linetext, String weight) {
		return createErrorMSG("nweight", file, line, linetext, weight);
	}
	
	public static Message createNoValidWeightException(String file, int line, int column, String linetext, String weight) {
		return createErrorMSG("nweight", file, line, column, linetext, weight);
	}
	
	public static Message createNoValidThresholdException(String file, int line, String linetext, String threshold) {
		return createErrorMSG("tnf", file, line, linetext, threshold);
	}
	
	public static Message createIntervallOutOfBoundsWarning(String file, int line, String linetext, Double d, String question, String op, NumericalInterval range) {
		return createWarningMSG("iobe", file, line, linetext, op+d, range.toString(), question);
	}
	
	public static Message createIntervallOutOfBoundsWarning(String file, int line, String linetext, Double a, Double b, String question, NumericalInterval range) {
		return createWarningMSG("iobe2", file, line, linetext, a, b, range, question);
	}
	
	public static Message createNoNumQuestionException(String file, int line, String linetext) {
		return createErrorMSG("numanswer", file, line, linetext);
	}
	
	public static Message createNoQuestionOnStack(String file, int line, String linetext) {
		return createErrorMSG("noquestiononstack", file, line, linetext);
	}
	
	public static Message createNoQuestionOnStack(String file, int line, int column, String linetext) {
		return createErrorMSG("noquestiononstack", file, line, column, linetext);
	}
	
	public static Message createTooManyPropertiesOnQuestionLinkWarning(String file, int line, String linetext) {
		return createWarningMSG("tmpoqc", file, line, linetext);
	}
	
	public static Message createNoDescriptionsAtQuestionClassWarning(String file, int line, String linetext) {
		return createWarningMSG("ndaqc", file, line, linetext);
	}
	
	public static Message createNoAddtoLink(String file, int line, String linetext) {
		return createErrorMSG("natl", file, line, linetext);
	}
	
	public static Message createTypeNotAllowed(String file, int line, String linetext, String type) {
		return createErrorMSG("tna", file, line, linetext, type);
	}
	
	public static Message createDescriptionNotAllowed(String file, int line, String linetext) {
		return createErrorMSG("dna", file, line, linetext);
	}
	
	public static Message createNotUsedDescriptionsAtFeatureDerivationWarning(String file, int line, String linetext) {
		return createWarningMSG("nudafd", file, line, linetext);
	}
	
	public static Message createSetOnlyAllowedAtFeatureDerivationWarning(String file, int line, String linetext) {
		return createWarningMSG("soaafd", file, line, linetext);
	}
	
	public static Message createNoParentQuestionError(String file, int line, String linetext) {
		return createErrorMSG("npq", file, line, linetext);
	}
	
	public static Message createTypInexistentError(String file, int line, String linetext, String question) {
		return createErrorMSG("tie", file, line, linetext, question);
	}
	
	public static Message createUnitAndRangeOnlyAtNumWarning(String file, int line, String linetext) {
		return createWarningMSG("uaroan", file, line, linetext);
	}
	
	public static Message createDescriptionTextNotFoundError(String file, int line, String linetext, String value) {
		return createErrorMSG("dtnf", file, line, linetext, value);
	}
	
	public static Message createNaNAtFeatureDerivationError(String file, int line, String linetext, String value) {
		return createErrorMSG("nanafd", file, line, linetext, value);
	}
	
	public static Message createDTparsedNote(String file, int line, String linetext, int i) {
		return createNoteMSG("dtparsed", file, line, linetext, i);
	}
	
	public static Message createSolutionsParsedNote(String file, int line, String linetext, int i) {
		return createNoteMSG("solutionsparsed", file, line, linetext, i);
	}
	
	public static Message createQContainerParsedNote(String file, int line, String linetext, int i) {
		return createNoteMSG("qcontainersparsed", file, line, linetext, i);
	}
	
	public static Message createTestsuiteParsedNote(String file, int line, String linetext, int i) {
		return createNoteMSG("testsuiteparsed", file, line, linetext, i);
	}
	
	public static Message createNameNotAllowedWarning(String file, int line, String linetext, String name) {
		return createWarningMSG("nna", file, line, linetext, name);
	}
	
	public static Message createWrongDiagState(String file, int line, String linetext, String state) {
		return createErrorMSG("wds", file, line, linetext, state);
	}
	
	public static Message createWrongDiagScore(String file, int line, String linetext) {
		return createErrorMSG("wdsc", file, line, linetext);
	}
	
	public static Message createTooManyDashes(String file, int line, String linetext) {
		return createErrorMSG("tmd", file, line, linetext);
	}
	
	public static Message createAmbiguousOrderError(String file, int line, String linetext, int id) {
		return createErrorMSG("ambiorder", file, line, linetext, id);
	}
	
	/**
	 * Eigentliche Methode um Fehler zu generieren
	 * @param key Schlüssel in der properties Datei
	 * @param file Datei in der der Fehler auftrat
	 * @param line Zeile in der der Fehler auftrat
	 * @param linetext Text der Zeile in der der Fehler auftrat
	 * @param adds In die Fehlermeldung einzufügende Objekte
	 * @return Fehlermeldung
	 */
	public static Message createErrorMSG(String key, String file, int line, String linetext, Object... adds ) {
		return Message.createError(generateText(key, adds), file, line, linetext);
	}
	
	public static Message createErrorMSG(String key, String file, int line, int column, String linetext, Object... adds ) {
		return Message.createError(generateText(key, adds), file, line, column, linetext);
	}
	
	/**
	 * Eigentliche Methode um Warnungen zu generieren
	 * @param key Schlüssel in der properties Datei
	 * @param file Datei in der der Fehler auftrat
	 * @param line Zeile in der der Fehler auftrat
	 * @param linetext Text der Zeile in der der Fehler auftrat
	 * @param adds In die Fehlermeldung einzufügende Objekte
	 * @return Fehlermeldung
	 */
	public static Message createWarningMSG(String key, String file, int line, String linetext, Object... adds ) {
		return Message.createWarning(generateText(key, adds), file, line, linetext);
	}
	
	public static Message createWarningMSG(String key, String file, int line, int column, String linetext, Object... adds ) {
		return Message.createWarning(generateText(key, adds), file, line, column, linetext);
	}
	
	public static Message createNoteMSG(String key, String file, int line, String linetext, Object... adds ) {
		return Message.createNote(generateText(key, adds), file, line, linetext);
	}
	
	public static Message createNoteMSGWithCount(String key, String file, int line, String linetext, Object... adds) {
		int count = 0;
		if (adds.length == 1 && adds[0] instanceof Integer) {
			count = (Integer) adds[0];
		}
		return Message.createNoteWithCount(generateText(key, adds), file, line, linetext, count);
	}

	private static String generateText(String key, Object... adds) {
		String result = rb.getString("unknownError") + ": " + key;
		try {
			result = MessageFormat.format(rb.getString(key), adds);
		} catch (Exception e) {
			
		}
		return result;
	}

	public static Message createRulesFinishedNote(String file, int rulecount) {
		return createNoteMSGWithCount("rule", file, 0, "", rulecount);
	}
	
	public static Message createXCLFinishedNote(String file, int i, String countfindings) {
		return createNoteMSG("xcl", file, 0, "", i, countfindings);
	}

	public static Message createNoDiagsError(String file, int startrow) {
		return createErrorMSG("nodiagsinxls", file, startrow, "");
	}

	public static Message createXLSFileParsed(String file, int counter) {
		return createNoteMSG("xls", file, 0, "", counter);
	}

	public static Message createParserFPE(String file,
			FailedPredicateException fpe) {
		if (fpe.toString().equals("FailedPredicateException(line,{(i<=dashcount+1)}?)")) {
			return createTooManyDashes(file, fpe.line, "");
		} else {
			return createUnknownParserError(file, fpe);
		}
	}
	
	

}
