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

package de.d3web.textParser.complexRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.DiagnosisState;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.NumericalInterval;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.RuleAction;
import de.d3web.kernel.domainModel.RuleComplex;
import de.d3web.kernel.domainModel.RuleFactory;
import de.d3web.kernel.domainModel.Score;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.answers.AnswerNum;
import de.d3web.kernel.domainModel.formula.Add;
import de.d3web.kernel.domainModel.formula.Div;
import de.d3web.kernel.domainModel.formula.FormulaElement;
import de.d3web.kernel.domainModel.formula.FormulaExpression;
import de.d3web.kernel.domainModel.formula.FormulaNumber;
import de.d3web.kernel.domainModel.formula.FormulaNumberArgumentsTerm;
import de.d3web.kernel.domainModel.formula.FormulaNumberElement;
import de.d3web.kernel.domainModel.formula.Mult;
import de.d3web.kernel.domainModel.formula.QNumWrapper;
import de.d3web.kernel.domainModel.formula.Sub;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionText;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondAnd;
import de.d3web.kernel.domainModel.ruleCondition.CondChoiceNo;
import de.d3web.kernel.domainModel.ruleCondition.CondChoiceYes;
import de.d3web.kernel.domainModel.ruleCondition.CondDState;
import de.d3web.kernel.domainModel.ruleCondition.CondEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondKnown;
import de.d3web.kernel.domainModel.ruleCondition.CondMofN;
import de.d3web.kernel.domainModel.ruleCondition.CondNot;
import de.d3web.kernel.domainModel.ruleCondition.CondNumEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNumGreater;
import de.d3web.kernel.domainModel.ruleCondition.CondNumGreaterEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondNumIn;
import de.d3web.kernel.domainModel.ruleCondition.CondNumLess;
import de.d3web.kernel.domainModel.ruleCondition.CondNumLessEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondOr;
import de.d3web.kernel.domainModel.ruleCondition.CondTextEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondUnknown;
import de.d3web.kernel.domainModel.ruleCondition.NonTerminalCondition;
import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
import de.d3web.kernel.psMethods.heuristic.ActionHeuristicPS;
import de.d3web.kernel.psMethods.heuristic.PSMethodHeuristic;
import de.d3web.kernel.psMethods.nextQASet.ActionNextQASet;
import de.d3web.kernel.psMethods.questionSetter.ActionQuestionSetter;
import de.d3web.kernel.supportknowledge.Property;
import de.d3web.textParser.Utils.ConceptNotInKBError;
import de.d3web.textParser.Utils.KBUtils;
import de.d3web.textParser.Utils.QuestionNotInKBError;

/**
 * Ein Parser baut aus den vom Scanner gelieferten Tokens den ParseBaum auf und
 * speichert die Informationen in der uebergebenen Wissensbasis.
 * 
 * Hinweis: Die Grammatik hier wurde etwas eingeschrï¿½nkt, da die Wissensbasis
 * nicht so mï¿½chtig ist wie die XML-Variante. Diese Klasse wurde
 * umgeschrieben, um in die Wissensbasis statt nach XML zu Parsen.
 * 
 * 
 * 
 * <br>
 * <br>
 * Das Parsen erfolgt anhand folgender Grammatik:<br>
 * <br>
 * DOKUMENT := { REGEL }*<br>
 * <br>
 * REGEL := ENTFERNEN? 'WENN' BEDINGUNG {'KONTEXT' BEDINGUNG}? {'AUSSER'
 * BEDINGUNG}? 'DANN' AKTION |<br>
 * ENTFERNEN? 'WENN' BEDINGUNG {'AUSSER' BEDINGUNG}? {'KONTEXT' BEDINGUNG}?
 * 'DANN' AKTION<br>
 * <br>
 * BEDINGUNG := BEDINGUNG 'UND' BEDINGUNG |<br>
 * BEDINGUNG 'ODER' BEDINGUNG |<br>
 * 'NICHT' BEDINGUNG |<br>
 * '(' BEDINGUNG ')' |<br>
 * VERGLEICH |<br>
 * 'MINMAX' '(' NUM KOMMA NUM ')' 'AUS' '{' VERGLEICH {KOMMA VERGLEICH}* '}'<br>
 * <br>
 * VERGLEICH := FRAGE VERGLEICHSOPERATOR WERT<br>
 * <br>
 * AKTION := AKTION 'UND' AKTION |<br>
 * AKTION 'SOWIE' AKTION |<br>
 * DIAGNOSE ASSIGN_OP SCORE |<br>
 * FRAGE ASSIGN_OP FORMEL |<br>
 * FRAGE ASSIGN_OP WERT |<br>
 * FRAGE { ';' FRAGE }* |<br>
 * FRAGEKLASSE { ';' FRAGEKLASSE }*<br>
 * <br>
 * FORMEL := FORMEL RECHENZEICHEN FORMEL |<br>
 * '(' FORMEL ')' | ZAHL | FRAGE<br>
 * <br>
 * RECHENZEICHEN := '+' | '-' | '*' | '/'<br>
 * <br>
 * ASSIGN_OP := '='<br>
 * <br>
 * SCORE := ALPHANUM<br>
 * <br>
 * FRAGE := ALPHANUM<br>
 * <br>
 * DIAGNOSE := ALPHANUM<br>
 * <br>
 * FRAGEKLASSE := ALPHANUM<br>
 * <br>
 * 
 * @author Christian Braun
 * @version 1.035
 * @since JDK 1.4
 */
public class ComplexRuleParser extends ComplexRuleConfiguration {

	// Attribute:
	// ==========

	/** Speichert das aktuelle Token */
	private Token token;

	/** Speichert den Scanner */
	private Scanner scanner;

	/** Speichert den ParserKonfig */
	private ComplexRuleConfigReader cfg;

	/** Speichert den ReportGenerator */
	private ComplexRuleReportGenerator report;

	/** Speichert die Wissensbasis */
	private KnowledgeBaseManagement kbm;

	/** Speichert die Wissensbasis */
	private KnowledgeBase kb;

	/** Speichert die Anzahl der ï¿½nderungen in der Wissensbasis */
	private Counter stats;

	/** Gibt den Modus an: true --> UPDATE ; false --> REPLACE */
	private boolean update;

	private boolean syntaxCheckOnly = false;

	// Konstruktor:
	// ============

	/**
	 * Erzeugt einen neuen Parser und baut den ParseBaum auf.
	 * 
	 * @param scanner
	 *            Der Scanner, der die Tokens liefert
	 * @param config
	 *            Die Konfiguration des Parsers
	 * @param report
	 *            ReportGenerator, mit dem die Fehlermeldungen erzeugt werden
	 *            sollen
	 * @param kbm
	 *            Die Wissensbasis, in die geparst werden soll
	 * @param update
	 *            Gibt den Modus an: true --> UPDATE ; false --> REPLACE
	 * @throws NullPointerException
	 *             falls einer der Parameter null ist
	 */
	public ComplexRuleParser(Scanner scanner, ComplexRuleConfigReader config,
			ComplexRuleReportGenerator report, KnowledgeBaseManagement kbm,
			boolean update, boolean onlySyntaxCheck) {
		if (scanner == null || config == null || report == null || kbm == null)
			throw new NullPointerException();
		this.scanner = scanner;
		this.cfg = config;
		this.report = report;
		this.kbm = kbm;
		this.kb = kbm.getKnowledgeBase();
		this.update = update;
		this.syntaxCheckOnly = onlySyntaxCheck;
		stats = new Counter();

		token = scanner.getNext();
		readDOKUMENT();
	}

	public ComplexRuleParser(Scanner scanner, ComplexRuleConfigReader config,
			ComplexRuleReportGenerator report, KnowledgeBaseManagement kbm,
			boolean update) {
		if (scanner == null || config == null || report == null || kbm == null)
			throw new NullPointerException();
		this.scanner = scanner;
		this.cfg = config;
		this.report = report;
		this.kbm = kbm;
		this.kb = kbm.getKnowledgeBase();
		this.update = update;
		stats = new Counter();

		token = scanner.getNext();
		readDOKUMENT();
	}

	// Interne Methoden:
	// =================

	/**
	 * Liest das nï¿½chste Token vom Scanner
	 */
	private void nextToken() {
		token = scanner.getNext();
	}

	/**
	 * Liest so lange Tokens vom Scanner, bis eines der uebergebenen Tokens
	 * gefunden oder das Ende der Eingabe erreicht wurde.
	 * 
	 * @param tokens
	 *            Die Tokens, nach denen gesucht werden soll
	 */
	private void nextUntil(String[] tokens) {
		tokens = join(tokens, EOF);
		while (!token.is(tokens))
			nextToken();
	}

	/**
	 * Liefert das Token vor dem aktuellen Token.
	 * 
	 * @return das Token vor dem aktuellen Token (scanner.preview(-1))
	 */
	private Token markPrev() {
		return scanner.preview(-1);
	}

	/**
	 * ueberprueft, ob das aktuelle Token Vorzeichen des nï¿½chsten Tokens ist,
	 * d.h. ob das nï¿½chste Token ein SYMBOL ist und kein Leerzeichen zwischen
	 * den beiden Tokens in der Eingabe ist.
	 * 
	 * @return true, falls das aktuelle Token Vorzeichen des nï¿½chsten Tokens
	 *         ist
	 */
	private boolean isVorzeichen() {
		Token t = scanner.preview(1);
		return (t.is(SYMBOL) && token.getZeile() == t.getZeile() && t
				.getSpalte() == token.getSpalte() + token.getSymbol().length());
	}

	/**
	 * Liefert das QContainer-Objekt zur uebergebenen FrageKlasse.
	 * 
	 * @param frageklasse
	 *            Die FrageKlasse, zu dem das QContainer-Objekt geliefert werden
	 *            soll
	 * @return Das QContainer-Objekt zur uebergebenen FrageKlasse oder null
	 */
	private QContainer getQContainer(String frageklasse) {
		QContainer qcontainer;
		Iterator it = kb.getQContainers().iterator();
		while (it.hasNext()) {
			qcontainer = (QContainer) it.next();
			if (equals(qcontainer.getText(), frageklasse))
				return qcontainer;
		}
		return null;
	}

	private QASet getQASet(String qaText) {
		QASet qaSet;
		Iterator it = kb.getQASetIterator();
		while (it.hasNext()) {
			qaSet = (QASet) it.next();
			if (equals(qaSet.getText(), qaText))
				return qaSet;
		}
		return null;
	}

	/**
	 * Liefert das Question-Objekt zum uebergebenen Symptom.
	 * 
	 * @param symptom
	 *            Das Symptom, zu dem das Question-Objekt geliefert werden soll
	 * @return Das Question-Objekt zum uebergebenen Symptom oder null
	 */
	private Question getQuestion(String symptom) {
		Question question;
		Iterator it = kb.getQuestions().iterator();
		while (it.hasNext()) {
			question = (Question) it.next();
			if (equals(question.getText(), symptom))
				return question;
		}
		return null;
	}

	/**
	 * Liefert das Diagnosis-Objekt zur uebergebenen Diagnose.
	 * 
	 * @param diagnose
	 *            Die Diagnose, zu dem das Diagnosis-Objekt geliefert werden
	 *            soll
	 * @return Das Diagnosis-Objekt zur uebergebenen Diagnose oder null
	 */
	private Diagnosis getDiagnosis(String diagnose) {
		Diagnosis diagnosis;
		Iterator it = kb.getDiagnoses().iterator();
		while (it.hasNext()) {
			diagnosis = (Diagnosis) it.next();
			if (equals(diagnosis.getText(), diagnose))
				return diagnosis;
		}
		return null;
	}

	/**
	 * Liefert das Answer-Objekt zum uebergebenen Wert.
	 * 
	 * @param question
	 *            Das Symptom, zu dem die Antwort gesucht werden soll
	 * @param wert
	 *            Die Antwort, zu dem das Answer-Objekt geliefert werden soll
	 * @return Das Answer-Objekt zum uebergebenen Wert oder null
	 */
	private AnswerChoice getAnswer(QuestionChoice question, String wert) {
		if (question == null)
			return null;
		AnswerChoice answer;
		if (question instanceof QuestionYN) {
			return KBUtils.findAnswerYN(kbm, (QuestionYN) question, wert);
		}
		Iterator it = question.getAllAlternatives().iterator();
		while (it.hasNext()) {
			answer = (AnswerChoice) it.next();
			if (equals(answer.getText(), wert))
				return answer;
		}
		return null;
	}

	/**
	 * Liefert das Score-Objekt zum uebergebenen Score.
	 * 
	 * @param wert
	 *            Der Score, zu dem das Score-Objekt geliefert werden soll
	 * @return Das Score-Objekt zum uebergebenen Score oder null
	 */
	private Score getScore(String wert) {
		Score score;
		Iterator it = Score.getAllScores().iterator();
		while (it.hasNext()) {
			score = (Score) it.next();
			if (equals(score.getSymbol(), wert))
				return score;
		}
		return null;
	}

	/**
	 * Liefert das DiagnosisState-Objekt zum uebergebenen DiagnosisState.
	 * 
	 * @param wert
	 *            Der DiagnosisState, zu dem das DiagnosisState-Objekt geliefert
	 *            werden soll
	 * @return Das DiagnosisState-Objekt zum uebergebenen DiagnosisState oder
	 *         null
	 */
	private DiagnosisState getDiagnosisState(String wert) {
		for (DiagnosisState state : DiagnosisState.getAllStati()) {
			if (equals(state.getName(), wert))
				return state;
		}
		return null;
	}

	/**
	 * ueberprueft, ob die uebergebene Bedingung nicht die maximale Stufe
	 * ueberschreitet.
	 * 
	 * @param condition
	 *            Die zu ueberpruefende Bedingung
	 * @param token
	 *            Das Token, das den Beginn der Bedingung anzeigt
	 * @return true, falls die maximale Stufe nicht ueberschritten wurde
	 */
	private boolean checkStufe(AbstractCondition condition, Token token) {
		if (condition == null)
			return false;
		if (cfg.maxStufe == ComplexRuleConfigReader.UNBEGRENZT)
			return true;

		if (cfg.maxStufe == ComplexRuleConfigReader.KNF) {
			if (!isKNF(condition)) {
				report.NOT_KNF(token);
				return false;
			}
		} else if (cfg.maxStufe == ComplexRuleConfigReader.DNF) {
			if (!isDNF(condition)) {
				report.NOT_DNF(token);
				return false;
			}
		} else {
			int stufe = stufe(condition);
			if (stufe > cfg.maxStufe) {
				report.MAXSTUFE(stufe, cfg.maxStufe, token);
				return false;
			}
		}
		return true;
	}

	/**
	 * Liefert die Stufe einer Bedingung. Dabei hat eine Condition ohne Kinder
	 * die Stufe 0. UND, ODER, MINMAX sind jeweils einstufig; NICHT ist
	 * 0-stufig.
	 * 
	 * @param condition
	 *            Die Bedingung, von der die Stufe bestimmt werden soll
	 * @return Stufe der Bedingung
	 */
	private int stufe(AbstractCondition condition) {
		if (condition == null || !(condition instanceof NonTerminalCondition))
			return 0;
		NonTerminalCondition cond = (NonTerminalCondition) condition;

		int max = 0;
		Iterator it = cond.getTerms().iterator();
		while (it.hasNext()) {
			AbstractCondition c = (AbstractCondition) it.next();
			int stufe = stufe(c);
			if (stufe > max)
				max = stufe;
		}
		// NICHT ist 0-stufig
		return ((cond instanceof CondNot) ? 0 : 1) + max;
	}

	/**
	 * Prï¿½ft, ob die uebergebene Bedingung in konjunktiver Normalform ist.
	 * 
	 * @param condition
	 *            Die zu ueberpruefende Bedingung
	 * @return true, falls die uebergebene Bedingung in konjunktiver Normalform
	 *         ist
	 */
	private boolean isKNF(AbstractCondition condition) {
		if (condition == null)
			return true;

		boolean ok = true;
		if (condition instanceof CondAnd) {
			Iterator it = ((CondAnd) condition).getTerms().iterator();
			while (it.hasNext()) {
				AbstractCondition sub = (AbstractCondition) it.next();
				if (sub instanceof CondAnd)
					return false;
				ok = ok && isKNF(sub);
			}
		} else if (condition instanceof CondOr) {
			Iterator it = ((CondOr) condition).getTerms().iterator();
			while (it.hasNext()) {
				AbstractCondition sub = (AbstractCondition) it.next();
				if (sub instanceof CondAnd)
					return false;
				if (sub instanceof CondOr)
					return false;
				ok = ok && isKNF(sub);
			}
		} else if (condition instanceof CondNot) {
			Iterator it = ((CondNot) condition).getTerms().iterator();
			while (it.hasNext()) {
				AbstractCondition sub = (AbstractCondition) it.next();
				if (sub instanceof CondAnd)
					return false;
				if (sub instanceof CondOr)
					return false;
				if (sub instanceof CondNot)
					return false;
				ok = ok && isKNF(sub);
			}
		} else if (condition instanceof CondMofN)
			return false;
		else if (!(condition instanceof TerminalCondition))
			return false;
		return ok;
	}

	/**
	 * Prueft, ob die uebergebene Bedingung in disjunktiver Normalform ist.
	 * 
	 * @param condition
	 *            Die zu ueberpruefende Bedingung
	 * @return true, falls die uebergebene Bedingung in disjunktiver Normalform
	 *         ist
	 */
	private boolean isDNF(AbstractCondition condition) {
		if (condition == null)
			return true;

		boolean ok = true;
		if (condition instanceof CondOr) {
			Iterator it = ((CondOr) condition).getTerms().iterator();
			while (it.hasNext()) {
				AbstractCondition sub = (AbstractCondition) it.next();
				if (sub instanceof CondOr)
					return false;
				ok = ok && isKNF(sub);
			}
		} else if (condition instanceof CondAnd) {
			Iterator it = ((CondAnd) condition).getTerms().iterator();
			while (it.hasNext()) {
				AbstractCondition sub = (AbstractCondition) it.next();
				if (sub instanceof CondOr)
					return false;
				if (sub instanceof CondAnd)
					return false;
				ok = ok && isKNF(sub);
			}
		} else if (condition instanceof CondNot) {
			Iterator it = ((CondNot) condition).getTerms().iterator();
			while (it.hasNext()) {
				AbstractCondition sub = (AbstractCondition) it.next();
				if (sub instanceof CondOr)
					return false;
				if (sub instanceof CondAnd)
					return false;
				if (sub instanceof CondNot)
					return false;
				ok = ok && isKNF(sub);
			}
		} else if (condition instanceof CondMofN)
			return false;
		else if (!(condition instanceof TerminalCondition))
			return false;
		return ok;
	}

	/**
	 * Vergleicht, ob eine Regel aus den als Parameter uebergebenen Bedingungen
	 * und Aktionen besteht.
	 * 
	 * @param rule
	 *            Die zu ueberpruefende Regel
	 * @param condition
	 *            Die zu ueberpruefende Bedingung
	 * @param context
	 *            Die zu ueberpruefende Kontext-Bedingung
	 * @param exception
	 *            Die zu ueberpruefende Ausnahme-Bedingung
	 * @param action
	 *            Die zu ueberpruefende Aktion
	 * @return true, falls die Regel aus den als Parameter uebergebenen
	 *         Bedingungen und Aktionen besteht
	 */
	private boolean ruleCompare(RuleComplex rule, AbstractCondition condition,
			AbstractCondition context, AbstractCondition exception,
			PseudoAction action) {
		// Bedingungen gleich?
		if (!conditionCompare(rule.getCondition(), condition))
			return false;
		if (!conditionCompare(rule.getContext(), context))
			return false;
		if (!conditionCompare(rule.getException(), exception))
			return false;

		// Aktion gleich?
		RuleAction a = rule.getAction();
		if (a instanceof ActionHeuristicPS) {
			if (action.diagnosis == null)
				return false;
			Diagnosis d = ((ActionHeuristicPS) a).getDiagnosis();
			if (d == null || !equals(d.getText(), action.diagnosis.getText()))
				return false;
			Score s = ((ActionHeuristicPS) a).getScore();
			if (s == null || !s.equals(action.score))
				return false;
		} else if (a instanceof ActionQuestionSetter) {
			if (action.question == null)
				return false;
			Question q = ((ActionQuestionSetter) a).getQuestion();
			if (q == null || !equals(q.getText(), action.question.getText()))
				return false;
			Object[] values = ((ActionQuestionSetter) a).getValues();
			if (values == null || values.length < 1)
				return false;
			Object o = values[0];
			if (o instanceof AnswerChoice) {
				if (o == null && action.answer == null)
					return true;
				if (o == null || action.answer == null)
					return false;

				String ansText = "";
				if (action.answer instanceof AnswerChoice)
					ansText = ((AnswerChoice) (action.answer)).getText();
				else if (action.answer instanceof AnswerNum)
					ansText = action.answer.toString();

				return equals(((AnswerChoice) o).getText(), ansText);
			} else if (o instanceof FormulaExpression) {
				FormulaNumberElement f = ((FormulaExpression) o)
						.getFormulaElement();
				return formulaCompare(f, action.formula);
			} else
				return false;
		} else if (a instanceof ActionNextQASet) {
			if (action.indications == null)
				return false;
			List l = ((ActionNextQASet) a).getQASets();
			return listCompare(l, action.indications);
		} else
			return false;
		return true;
	}

	/**
	 * Vergleicht zwei Bedingungen.
	 * 
	 * @param c1
	 *            Die erste Bedingung
	 * @param c2
	 *            Die zweite Bedingung
	 * @return true, falls die die beiden Bedingungen gleich sind
	 */
	private boolean conditionCompare(AbstractCondition c1, AbstractCondition c2) {
		if (c1 == null && c2 == null)
			return true;
		if (c1 == null || c2 == null)
			return false;
		return c1.equals(c2);
	}

	/**
	 * Vergleicht zwei Formeln.
	 * 
	 * @param f1
	 *            Die erste Formel
	 * @param f2
	 *            Die zweite Formel
	 * @return true, falls die die beiden Formeln gleich sind
	 */
	private boolean formulaCompare(FormulaElement f1, FormulaElement f2) {
		if (f1 == null && f2 == null)
			return true;
		if (f1 == null || f2 == null)
			return false;
		if (f1.getClass() != f2.getClass())
			return false;

		if (f1 instanceof FormulaNumberArgumentsTerm) {
			FormulaNumberArgumentsTerm ft1 = (FormulaNumberArgumentsTerm) f1;
			FormulaNumberArgumentsTerm ft2 = (FormulaNumberArgumentsTerm) f2;
			return formulaCompare(ft1.getArg1(), ft2.getArg1())
					&& formulaCompare(ft1.getArg2(), ft2.getArg2());
		} else if (f1 instanceof QNumWrapper) {
			QuestionNum q1 = ((QNumWrapper) f1).getQuestion();
			QuestionNum q2 = ((QNumWrapper) f2).getQuestion();
			if (q1 == null && q2 == null)
				return true;
			if (q1 == null || q2 == null)
				return false;
			return equals(q1.getText(), q2.getText());
		} else if (f1 instanceof FormulaNumber) {
			Object o1 = ((FormulaNumber) f1).getValue();
			Object o2 = ((FormulaNumber) f2).getValue();
			if (o1 == null && o2 == null)
				return true;
			if (o1 == null || o2 == null)
				return false;
			return o1.equals(o2);
		}
		return false;
	}

	/**
	 * Vergleicht zwei Listen.
	 * 
	 * @param l1
	 *            Die erste Liste
	 * @param l2
	 *            Die zweite Liste
	 * @return true, falls die die beiden Listen gleich sind
	 */
	private boolean listCompare(List l1, List l2) {
		if (l1 == null && l2 == null)
			return true;
		if (l1 == null || l2 == null)
			return false;
		if (l1.size() != l2.size())
			return false;
		try {
			return l1.containsAll(l2);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Sucht in der Wissensbasis nach Regeln, die aus den als Parameter
	 * uebergebenen Bedingungen und Aktionen bestehen.
	 * 
	 * @param condition
	 *            Die zu ueberpruefende Bedingung
	 * @param context
	 *            Die zu ueberpruefende Kontext-Bedingung
	 * @param exception
	 *            Die zu ueberpruefende Ausnahme-Bedingung
	 * @param action
	 *            Die zu ueberpruefende Aktion
	 * @return Liste mit den gefundenen Regeln
	 */
	private List<RuleComplex> ruleSearch(AbstractCondition condition,
			AbstractCondition context, AbstractCondition exception,
			PseudoAction action) {

		Iterator slices = kb.getAllKnowledgeSlices().iterator();
		List<RuleComplex> rules = new ArrayList<RuleComplex>();
		while (slices.hasNext()) {
			Object o = slices.next();
			if (!(o instanceof RuleComplex))
				continue;
			RuleComplex r = (RuleComplex) o;
			if (ruleCompare(r, condition, context, exception, action))
				rules.add(r);
		}
		return rules;
	}

	/**
	 * Erstellt eine neue Regel in der Wissensbasis mit den als Parameter
	 * uebergebenen Bedingungen und Aktionen.
	 * 
	 * @param condition
	 *            Die zu ueberpruefende Bedingung
	 * @param context
	 *            Die zu ueberpruefende Kontext-Bedingung
	 * @param exception
	 *            Die zu ueberpruefende Ausnahme-Bedingung
	 * @param action
	 *            Die zu ueberpruefende Aktion
	 */
	private void addRule(AbstractCondition condition,
			AbstractCondition context, AbstractCondition exception,
			PseudoAction action, String comment) {

		if (this.syntaxCheckOnly) {
			return;
		}

		RuleComplex rule = new RuleComplex(); // <-- DummyRule
		String id = kbm.findNewIDFor(rule);

		// wenn action.answers != null dann createSupressAnswerRule
		if (action.answerList != null) {
			Object[] theAnswers = new Object[action.answerList.size()];
			int count = 0;
			for (Answer answer : action.answerList) {
				theAnswers[count] = answer;
				count++;
			}
			rule = RuleFactory.createSuppressAnswerRule(id,
					(QuestionChoice) action.question, theAnswers, condition);
		} else if (action.qaList != null) {
			rule = RuleFactory.createContraIndicationRule(id, action.qaList,
					condition);
		} else if (action.diagnosis != null)
			rule = RuleFactory.createHeuristicPSRule(id, action.diagnosis,
					action.score, condition);
		else if (action.question != null) {
			if (action.formula != null)
				if (!action.add)
					rule = RuleFactory.createSetValueRule(id, action.question,
							new FormulaExpression(action.question,
									action.formula), condition);
				else {
					Object[] formulaArray = new Object[] { new FormulaExpression(
							action.question, action.formula) };
					rule = RuleFactory.createAddValueRule(id, action.question,
							formulaArray, condition);
				}
			else if (action.answer != null) {
				if (action.add)
					rule = RuleFactory.createAddValueRule(id, action.question,
							new Object[] { action.answer }, condition);
				else
					rule = RuleFactory.createSetValueRule(id, action.question,
							new Object[] { action.answer }, condition);
			}
		} else if (action.indications != null) {
			rule = RuleFactory.createIndicationRule(id, action.indications,
					condition);
		} else if (action.instantIndications != null
				&& action.instantIndications.size() > 0) {
			rule = RuleFactory.createInstantIndicationRule(id,
					action.instantIndications, condition);
		}

		if (comment != null)
			rule.setComment(comment);
		if (context != null)
			rule.setContext(context);
		if (exception != null)
			rule.setException(exception);
	}

	/**
	 * Fuegt eine geparste Regel in die Wissensbasis hinzu oder loescht diese.
	 * uebergebenen Bedingungen und Aktionen.
	 * 
	 * @param condition
	 *            Die zu ueberpruefende Bedingung
	 * @param context
	 *            Die zu ueberpruefende Kontext-Bedingung
	 * @param exception
	 *            Die zu ueberpruefende Ausnahme-Bedingung
	 * @param action
	 *            Die zu ueberpruefende Aktion
	 * @param clear
	 *            Gibt an, ob eine Regel geloescht oder hinzugefuegt werden soll
	 * @param start
	 *            Gibt das Token an, ab dem die Regel beginnt
	 */
	private void applyToKB(AbstractCondition condition,
			AbstractCondition context, AbstractCondition exception,
			PseudoAction action, boolean clear, int start, String comment) {

		List<RuleComplex> rules = ruleSearch(condition, context, exception,
				action);
		boolean exist = !rules.isEmpty();

		if (update && !clear && !exist) {
			addRule(condition, context, exception, action, comment);
			stats.added++;
		} else if (update && !clear && exist) {
			stats.ignored++;
		} else if (update && clear && !exist) {
			report.NOT_IN_KB(scanner, start, scanner.getPosition());
			stats.ignored++;
		} else if (update && clear && exist) {
			for (RuleComplex rule : rules)
				kb.remove(rule);
			stats.cleared += rules.size();
		} else if (!update && !clear && !exist) {
			addRule(condition, context, exception, action, comment);
			stats.added++;
		} else if (!update && !clear && exist) {
			for (RuleComplex rule : rules)
				kb.remove(rule);
			addRule(condition, context, exception, action, comment);
			stats.replaced += rules.size();
		} else if (!update && clear && !exist) {
			report.NOT_IN_KB(scanner, start, scanner.getPosition());
			stats.ignored++;
		} else if (!update && clear && exist) {
			for (RuleComplex rule : rules)
				kb.remove(rule);
			stats.cleared += rules.size();
		}

	}

	// Spezial-Funktionen:
	// ===================

	/*
	 * Die folgenden Methoden lesen das Dokument so ein, wie in der Definition
	 * in EBNF beschrieben. Dabei entspricht je ein Nichtterminal einer Methode.
	 * Die Methoden liefern dabei jeweils den entsprechenden Parse-Teil-Baum
	 * oder null, falls ein Fehler auftrat.
	 * 
	 * FIRST (NICHTTERMINAL): Gibt die Menge der TERMINALE an, mit denen das
	 * NICHTTERMINAL beginnen kann. FOLLOW (NICHTTERMINAL): Gibt die Menge der
	 * TERMINALE an, die nach dem NICHTTERMINAL kommen koennen. KEYS
	 * (NICHTTERMINAL): Gibt die relevanten TERMINALE an, an denen sich der
	 * Parser bei diesem NICHTTERMINAL (im Fehlerfall) orientiert.
	 */

	/*
	 * DOKUMENT := { REGEL }*
	 * 
	 * FIRST (DOKUMENT) = { ENTFERNEN, WENN, EOF } FOLLOW (DOKUMENT) = { EOF }
	 * KEYS (DOKUMENT) = { EOF }
	 */
	private void readDOKUMENT() {
		while (!token.is(EOF))
			readREGEL();
		if (stats.added == 0 && stats.cleared == 0 && stats.ignored == 0
				&& stats.replaced == 0)
			report.KEINE_REGEL();
		else
			report.REGELN(stats.added, stats.cleared, stats.ignored,
					stats.replaced);
	}

	/*
	 * REGEL := ENTFERNEN? 'WENN' BEDINGUNG {'KONTEXT' BEDINGUNG}? {'AUSSER'
	 * BEDINGUNG}? 'DANN' AKTION | ENTFERNEN? 'WENN' BEDINGUNG {'AUSSER'
	 * BEDINGUNG}? {'KONTEXT' BEDINGUNG}? 'DANN' AKTION
	 * 
	 * FIRST (REGEL) = { ENTFERNEN, WENN } FOLLOW (REGEL) = { ENTFERNEN, WENN,
	 * EOF } KEYS (DOKUMENT) = { ENTFERNEN, WENN, KONTEXT, AUSSER, DANN }
	 */
	private void readREGEL() {
		String comment = null;
		int start = scanner.getPosition();
		boolean clear = false, ruleError = false;
		AbstractCondition condition = null, context = null, exception = null;

		// KOMMENTAR?
		if (token.getSymbol().equals(RULE_COMMENT)) {
			comment = token.getToken();
			if (comment.contains("\"") || comment.contains("'")) {
				report.illegalCharacterInComment("\"\"\" und \"'\"", token);
			}
			nextToken();
		}

		// ENTFERNEN?
		if (token.is(ENTFERNEN)) {
			clear = true;
			nextToken();
		}

		// WENN BEDINGUNG
		if (token.is(WENN)) {
			Token temp = token;
			nextToken();
			condition = readBEDINGUNG();
			if (!checkStufe(condition, temp))
				condition = null;
			if (condition == null)
				ruleError = true;
		} else {
			report.WENN(token);
			nextUntil(new String[] { ENTFERNEN, WENN, KONTEXT, AUSSER, DANN,
					RULE_COMMENT });
			if (token.is(new String[] { ENTFERNEN, WENN, RULE_COMMENT })) {
				readREGEL();
				return;
			}
		}

		// KONTEXT / AUSSER BEDINGUNG ; DANN AKTION
		boolean error = false, kontext = false, ausser = false;
		boolean kontextMeldung = false, ausserMeldung = false;

		while (!(error && (token.is(WENN) || token.is(ENTFERNEN) || token
				.is(EOF)))) {
			error = false;
			Token temp = token;

			// KONTEXT
			if (token.is(KONTEXT)) {
				if (kontext) {
					if (!kontextMeldung) {
						report.KONTEXT(token);
						kontextMeldung = true;
					}
					nextUntil(new String[] { ENTFERNEN, WENN, AUSSER, DANN });
					ruleError = error = true;
				} else {
					nextToken();
					context = readBEDINGUNG();
					if (!checkStufe(context, temp))
						context = null;
					if (context == null)
						ruleError = true;
					kontext = true;
				}
			}

			// AUSSER
			else if (token.is(AUSSER)) {
				if (ausser) {
					if (!ausserMeldung) {
						report.AUSSER(token);
						ausserMeldung = true;
					}
					nextUntil(new String[] { ENTFERNEN, WENN, KONTEXT, DANN });
					ruleError = error = true;
				} else {
					nextToken();
					exception = readBEDINGUNG();
					if (!checkStufe(exception, temp))
						exception = null;
					if (exception == null)
						ruleError = true;
					ausser = true;
				}
			}

			// DANN
			else if (token.is(DANN)) {
				nextToken();
				List actions = readAKTION();
				if (actions == null || actions.size() < 1)
					ruleError = true;
				if (!ruleError) {
					Iterator it = actions.iterator();
					while (it.hasNext()) {
						PseudoAction action = (PseudoAction) it.next();
						applyToKB(condition, context, exception, action, clear,
								start, comment);
					}
				}
				return;
			} else {
				report.TOKEN(DANN, markPrev());
				nextUntil(new String[] { ENTFERNEN, WENN, KONTEXT, AUSSER, DANN });
				ruleError = error = true;
			}

		}
		return;
	}

	/*
	 * BEDINGUNG := TEILBEDINGUNG { (UND|ODER) TEILBEDINGUNG }*
	 * 
	 * FIRST (BEDINGUNG) = { NICHT, NORMALE_KLAMMER_AUF, SYMBOL, MINMAX } FOLLOW
	 * (BEDINGUNG) = { KONTEXT, AUSSER, DANN, NORMALE_KLAMMER_ZU,
	 * GESCHWEIFTE_KLAMMER_ZU, KOMMA } KEYS (BEDINGUNG) = { UND, ODER }
	 */
	private AbstractCondition readBEDINGUNG() {
		ArrayList<AbstractCondition> liste;
		AbstractCondition subcondition, condition = readTEILBEDINGUNG();

		String zeichen = "";
		while (true) {
			// UND
			if (token.is(UND)) {
				if (!token.is(cfg.erlaubteOperatoren)) {
					report.LOGIKOPERATOR(token);
					nextToken();
					readTEILBEDINGUNG();
					condition = null;
				} else {
					nextToken();
					if (zeichen.equalsIgnoreCase(UND)) {
						subcondition = readTEILBEDINGUNG();
						if (subcondition == null) {
							condition = null;
						}
						if (condition != null) {
							((NonTerminalCondition) condition).getTerms().add(
									subcondition);
						}
					} else {
						subcondition = readTEILBEDINGUNG();
						if (subcondition == null) {
							condition = null;
						}
						if (condition != null) {
							liste = new ArrayList<AbstractCondition>();
							liste.add(condition);
							liste.add(subcondition);
							condition = new CondAnd(liste);
							zeichen = UND;
						}
					}
				}
			}
			// ODER
			else if (token.is(ODER)) {
				if (!token.is(cfg.erlaubteOperatoren)) {
					report.LOGIKOPERATOR(token);
					nextToken();
					readTEILBEDINGUNG();
					condition = null;
				} else {
					nextToken();
					if (zeichen.equalsIgnoreCase(ODER)) {
						subcondition = readTEILBEDINGUNG();
						if (subcondition == null) {
							condition = null;
						}
						if (condition != null) {
							((NonTerminalCondition) condition).getTerms().add(
									subcondition);
						}
					} else {
						subcondition = readTEILBEDINGUNG();
						if (subcondition == null) {
							condition = null;
						}
						if (condition != null) {
							liste = new ArrayList<AbstractCondition>();
							liste.add(condition);
							liste.add(subcondition);
							condition = new CondOr(liste);
							zeichen = ODER;
						}
					}
				}
			} else
				return condition;
		}

	}

	/*
	 * TEILBEDINGUNG := 'NICHT' TEILBEDINGUNG | '(' BEDINGUNG ')' | VERGLEICH |
	 * MINMAXAUSDRUCK
	 * 
	 * FIRST (TEILBEDINGUNG) = { NICHT, NORMALE_KLAMMER_AUF, SYMBOL, MINMAX }
	 * FOLLOW (TEILBEDINGUNG) = { KONTEXT, AUSSER, DANN, UND, ODER, KLAMMER_ZU,
	 * KOMMA } KEYS (TEILBEDINGUNG) = { NICHT, NORMALE_KLAMMER_AUF, SYMBOL,
	 * MINMAX, KOMMA }
	 */
	private AbstractCondition readTEILBEDINGUNG() {
		if (token.is(NICHT)) {
			if (cfg.nichtErlaubt) {
				Token temp = token;
				nextToken();
				AbstractCondition condition = readTEILBEDINGUNG();
				if (condition == null)
					return null;
				if (cfg.nichtNurVorLiteralen
						&& !(condition instanceof TerminalCondition)) {
					report.NICHT_NUR_LITERALE(temp);
					return null;
				}
				return new CondNot(condition);
			} else {
				report.NOT_NICHT(token);
				nextToken();
				readTEILBEDINGUNG();
				return null;
			}
		} else if (token.is(NORMALE_KLAMMER_AUF)) {
			nextToken();
			AbstractCondition condition = readBEDINGUNG();
			if (token.is(NORMALE_KLAMMER_ZU)) {
				nextToken();
				return condition;
			} else {
				report.KLAMMER_ZU(markPrev());
				return null;
			}
		} else if (token.is(SYMBOL)) {
			return readVERGLEICH();
		} else if (token.is(MINMAX)) {
			return readMINMAXAUSDRUCK();
		} else {
			report.VERGLEICH(markPrev());
			nextUntil(new String[] { ENTFERNEN, WENN, KONTEXT, AUSSER, DANN,
					UND, ODER, KOMMA });
		}
		return null;
	}

	/*
	 * VERGLEICH := FRAGE VERGLEICHSOPERATOR WERT
	 * 
	 * FIRST (VERGLEICH) = { SYMBOL } FOLLOW (VERGLEICH) = { KONTEXT, AUSSER,
	 * DANN, UND, ODER, KOMMA, KLAMMER_ZU } KEYS (VERGLEICH) = { ENTFERNEN,
	 * WENN, KONTEXT, AUSSER, DANN, UND, ODER, KOMMA, KLAMMER_ZU }
	 */
	private TerminalCondition readVERGLEICH() {
		String[] label = new String[] { ENTFERNEN, WENN, KONTEXT, AUSSER, DANN,
				UND, ODER };

		// BEGRIFF
		String symbol = token.getSymbol();
		String spezial = null;
		Question question = null;
		Diagnosis diagnosis = null;
		if (token.is(SYMBOL)) {
			question = getQuestion(symbol);
			diagnosis = getDiagnosis(symbol);
			if (equals(symbol, BEKANNT))
				spezial = BEKANNT;
			if (equals(symbol, UNBEKANNT))
				spezial = UNBEKANNT;
			if (question == null && diagnosis == null && spezial == null) {
				String nextToken = showNextToken(1);
				if (nextToken.equals(ASSIGN_OP)) {
					String scndNextToken = showNextToken(2);
					if (QuestionNotInKBError.isYesOrNo(scndNextToken)) {
						report.KB_KEIN_BEGRIFF1(symbol, token, scndNextToken);
					} else {
						report.KB_KEIN_BEGRIFF(symbol, token);
					}
				} else {
					report.KB_KEIN_BEGRIFF(symbol, token);
				}
				nextUntil(label);
				return null;
			}
			nextToken();
		} else {
			report.VERGLEICH(markPrev());
			nextUntil(label);
			return null;
		}

		// OPERATOR
		String typ = "";
		if (token.is(SMALLER))
			typ = SMALLER;
		else if (token.is(GREATER))
			typ = GREATER;
		else if (token.is(SMALLER_EQ) || token.is(SMALLER_EQ2))
			typ = SMALLER_EQ;
		else if (token.is(GREATER_EQ) || token.is(GREATER_EQ2))
			typ = GREATER_EQ;
		else if (token.is(EQUALS_OP) || token.is(ASSIGN_OP))
			typ = EQUALS_OP;
		else if (token.is(ECKIGE_KLAMMER_AUF))
			typ = ECKIGE_KLAMMER_AUF;
		else {
			if (question == null && diagnosis == null && spezial != null)
				report.ECKIGE_KLAMMER_AUF(markPrev());
			else
				report.OPERATOR(markPrev());
			nextUntil(label);
			return null;
		}

		nextToken();
		int position = scanner.getPosition();
		String wert = readSymbol();

		// Intervall / BEKANNT / UNBEKANNT [Frage]
		if (typ.equalsIgnoreCase(ECKIGE_KLAMMER_AUF)) {
			if (spezial == null && question == null) {
				report.OPERATOR(scanner.getToken(position - 2));
				nextUntil(label);
				return null;
			}

			if (wert == null) {
				if (spezial != null)
					report.KB_FRAGE_ERWARTET(symbol, scanner
							.getToken(position - 1));
				else
					report.INTERVALL_ERWARTET(symbol, scanner
							.getToken(position - 1));
				nextUntil(label);
				return null;
			}

			// Cond(Un)Known?
			if (spezial != null) {
				Question q = getQuestion(wert);
				if (q != null) {
					if (token.is(ECKIGE_KLAMMER_ZU))
						nextToken();
					else
						report.ECKIGE_KLAMMER_ZU(markPrev());
					if (spezial.equalsIgnoreCase(BEKANNT)
							|| spezial.equalsIgnoreCase(KNOWN))
						return new CondKnown(q);
					else
						return new CondUnknown(q);
				}
				if (question == null) {
					report.KB_KEIN_SYMPTOM(wert, scanner.getToken(position));
					nextUntil(label);
					return null;
				}

			}

			// Intervall
			String s = readSymbol();
			while (s != null) {
				wert += " " + s;
				s = readSymbol();
			}

			String[] tokens = normalizeBlanks(wert).split(" ", 2);
			if (tokens == null || tokens.length != 2) {
				report.KB_KEIN_INTERVALL(wert, scanner.getToken(position));
				nextUntil(label);
				return null;
			}
			Double min, max;
			try {
				min = Double.valueOf(tokens[0].replace(',', '.'));
			} catch (Exception e) {
				report.KB_KEINE_ZAHL(tokens[0], scanner.getToken(position));
				nextUntil(label);
				return null;
			}
			try {
				max = Double.valueOf(tokens[1].replace(',', '.'));
			} catch (Exception e) {
				report.KB_KEINE_ZAHL(tokens[1], scanner.getToken(position));
				nextUntil(label);
				return null;
			}

			if (token.is(ECKIGE_KLAMMER_ZU))
				nextToken();
			else
				report.ECKIGE_KLAMMER_ZU(markPrev());
			if (question instanceof QuestionNum) {
				return new CondNumIn((QuestionNum) question, min, max);
			} else {
				report.KB_KEINE_QNUM(symbol, scanner.getToken(position - 2));
				nextUntil(label);
				return null;
			}
		}

		// DiagnoseState?
		if (typ.equalsIgnoreCase(EQUALS_OP)) {
			if (question == null && diagnosis == null) {
				report.ECKIGE_KLAMMER_AUF(scanner.getToken(position - 2));
				nextUntil(label);
				return null;
			}
			if (diagnosis != null) {
				if (wert == null) {
					if (question == null)
						report.KB_STATUS_ERWARTET(symbol, scanner
								.getToken(position - 1));
					else
						report.WERT(symbol, scanner.getToken(position - 1));
					nextUntil(label);
					return null;
				}
				DiagnosisState state = getDiagnosisState(wert);
				if (state == null) {
					if (question == null) {
						report.KB_KEIN_STATUS(wert, scanner.getToken(position));
						nextUntil(label);
						return null;
					}
				} else
					return new CondDState(diagnosis, state,
							PSMethodHeuristic.class);
			}
		}

		// Frage?
		if (question == null) {
			report.KB_KEIN_BEGRIFF5(symbol, scanner.getToken(position - 2));
			nextUntil(label);
			return null;
		}
		if (wert == null) {
			report.WERT(symbol, scanner.getToken(position - 1));
			nextUntil(label);
			return null;
		}

		// Ja/Nein-Frage
		if (question instanceof QuestionYN) {
			if (!typ.equalsIgnoreCase(EQUALS_OP)) {
				report.KB_NUR_EQUALS(scanner.getToken(position - 1));
				nextUntil(label);
				return null;
			}
			QuestionYN q = (QuestionYN) question;
			if (equals(wert, JA) || equals(wert, YES) || equals(wert, WAHR))
				return new CondChoiceYes(q);
			else if (equals(wert, NEIN) || equals(wert, NO)
					|| equals(wert, FALSCH))
				return new CondChoiceNo(q);
			else {
				report.KB_NUR_JA_NEIN(scanner.getToken(position));
				nextUntil(label);
				return null;
			}
		}

		// QuestionChoice-Frage
		else if (question instanceof QuestionChoice) {
			if (!typ.equalsIgnoreCase(EQUALS_OP)) {
				report.KB_NUR_EQUALS(scanner.getToken(position - 1));
				nextUntil(label);
				return null;
			}
			QuestionChoice q = (QuestionChoice) question;
			if (equals(wert, ANTWORT_UNBEKANNT) || equals(wert, ANTWORT_EGAL)
					|| equals(wert, ANTWORT_UNKNOWN))
				return new CondUnknown(q);
			AnswerChoice a = getAnswer(q, wert);
			if (a == null) {
				report.KB_KEINE_ANTWORT(symbol, wert, scanner
						.getToken(position), q);
				nextUntil(label);
				return null;
			}
			return new CondEqual(q, a);
		}

		// QuestionNum-Frage
		else if (question instanceof QuestionNum) {

			// Ist die Zahl gueltig?
			Double d;
			try {
				d = new Double(Double.parseDouble(wert.replace(',', '.')));
			} catch (Exception e) {
				report.KB_KEINE_ZAHL(wert, scanner.getToken(position));
				nextUntil(label);
				return null;
			}

			// liegt der Wert im gueltigen Bereich?
			QuestionNum q = (QuestionNum) question;
			try {
				NumericalInterval bereich = (NumericalInterval) q
						.getProperties().getProperty(
								Property.QUESTION_NUM_RANGE);
				if (!bereich.contains(d.doubleValue())) {
					report.KB_NOT_BEREICH(wert, bereich.toString(), scanner
							.getToken(position));
					nextUntil(label);
					return null;
				}
			} catch (Exception e) {
			}

			if (typ.equalsIgnoreCase(SMALLER))
				return new CondNumLess(q, d);
			else if (typ.equalsIgnoreCase(GREATER))
				return new CondNumGreater(q, d);
			else if (typ.equalsIgnoreCase(SMALLER_EQ))
				return new CondNumLessEqual(q, d);
			else if (typ.equalsIgnoreCase(GREATER_EQ))
				return new CondNumGreaterEqual(q, d);
			else
				return new CondNumEqual(q, d);
		}

		// QuestionText-Frage
		else if (question instanceof QuestionText) {
			if (!typ.equalsIgnoreCase(EQUALS_OP)) {
				report.KB_NUR_EQUALS(scanner.getToken(position - 1));
				nextUntil(label);
				return null;
			}
			if (is(wert, ALPHANUM) != null) {
				report.KB_KEIN_STRING(wert, scanner.getToken(position));
				nextUntil(label);
				return null;
			}
			QuestionText q = (QuestionText) question;
			return new CondTextEqual(q, wert);
		}

		// Sonstige Frage
		else {
			report.KB_NOT_SUPPORTED(scanner.getToken(position - 2));
			nextUntil(label);
			return null;
		}
	}

	/*
	 * MINMAXAUSDRUCK := 'MINMAX' KLAMMER_AUF NUM NUM KLAMMER_ZU KLAMMER_AUF
	 * BEDINGUNG { ';' BEDINGUNG }* KLAMMER_ZU
	 * 
	 * FIRST (MINMAXAUSDRUCK) = { MINMAX } FOLLOW (MINMAXAUSDRUCK) = { KONTEXT,
	 * AUSSER, DANN, UND, ODER, NORMALE_KLAMMER_ZU, GESCHWEIFTE_KLAMMER_ZU,
	 * KOMMA } KEYS (MINMAXAUSDRUCK) = { ENTFERNEN, WENN, KONTEXT, AUSSER, DANN,
	 * UND, ODER }
	 */
	private CondMofN readMINMAXAUSDRUCK() {
		int min = 0, max = 0;
		boolean fehler = false, kb_fehler = false;

		// Minmax
		if (token.is(MINMAX)) {
			nextToken();
			fehler = false;
		} else if (!fehler) {
			report.MINMAX(MINMAX, markPrev());
			kb_fehler = fehler = true;
		}

		// Klammer auf
		if (token.is(KLAMMER_AUF)) {
			nextToken();
			fehler = false;
		} else if (!fehler) {
			report.MINMAX(NORMALE_KLAMMER_AUF, markPrev());
			kb_fehler = fehler = true;
		}

		// Intervall
		int position = scanner.getPosition();
		String wert = readSymbol();

		if (wert != null) {
			String s = readSymbol();
			while (s != null) {
				wert += " " + s;
				s = readSymbol();
			}

			String[] tokens = normalizeBlanks(wert).split(" ", 2);
			if (tokens == null || tokens.length != 2) {
				if (!fehler) {
					report.KB_KEIN_INTERVALL(wert, scanner.getToken(position));
					kb_fehler = fehler = true;
				}
			} else {
				try {
					min = Integer.parseInt(tokens[0]);
					if (min < 0)
						throw new NumberFormatException();
				} catch (Exception e) {
					if (!fehler) {
						report
								.NO_POS_INT(tokens[0], scanner
										.getToken(position));
						kb_fehler = fehler = true;
					}
				}
				try {
					max = Integer.parseInt(tokens[1]);
					if (max < 0)
						throw new NumberFormatException();
				} catch (Exception e) {
					if (!fehler) {
						report
								.NO_POS_INT(tokens[1], scanner
										.getToken(position));
						kb_fehler = fehler = true;
					}
				}
				fehler = false;
			}
			if (!fehler && min > max) {
				report.MIN_GT_MAX(wert, scanner.getToken(position));
			}
		} else if (!fehler) {
			report.MINMAX("Intervall (min max)", markPrev());
			kb_fehler = fehler = true;
		}

		// Klammer zu
		if (token.is(KLAMMER_ZU)) {
			nextToken();
			fehler = false;
		} else if (!fehler) {
			report.MINMAX(NORMALE_KLAMMER_ZU, markPrev());
			kb_fehler = fehler = true;
		}

		// Klammer auf
		if (token.is(KLAMMER_AUF)) {
			nextToken();
			fehler = false;
		} else if (!fehler) {
			report.MINMAX(GESCHWEIFTE_KLAMMER_AUF, markPrev());
			kb_fehler = fehler = true;
		}

		ArrayList<AbstractCondition> liste = new ArrayList<AbstractCondition>();

		// Alternativen
		while (true) {
			if (token.is(new String[] { NICHT, NORMALE_KLAMMER_AUF, SYMBOL,
					MINMAX })) {
				AbstractCondition condition = readBEDINGUNG();
				if (condition == null)
					kb_fehler = true;
				else
					liste.add(condition);
				fehler = false;
			} else if (token.is(KOMMA)) {
				nextToken();
				fehler = false;
			} else if (token.is(KLAMMER_ZU)) {
				nextToken();
				if (kb_fehler)
					return null;
				try {
					return new CondMofN(liste, min, max);
				} catch (Exception e) {
					return null;
				}
			} else if (token.is(new String[] { ENTFERNEN, WENN, KONTEXT,
					AUSSER, DANN, UND, ODER, EOF })) {
				report.MINMAX(GESCHWEIFTE_KLAMMER_ZU, markPrev());
				return null;
			} else {
				if (!fehler) {
					report.NOT_TOKEN(token);
					kb_fehler = fehler = true;
				}
				nextToken();
			}
		}

	}

	/*
	 * AKTION := TEILAKTION { (UND|SOWIE) TEILAKTION }*
	 * 
	 * FIRST (AKTION) = { SYMBOL } FOLLOW (AKTION) = { ENTFERNEN, WENN, EOF }
	 * KEYS (AKTION) = { ENTFERNEN, WENN, KONTEXT, AUSSER, DANN, UND, SOWIE }
	 */
	private List<PseudoAction> readAKTION() {
		ArrayList<PseudoAction> actions = new ArrayList<PseudoAction>();
		PseudoAction action = readTEILAKTION();
		if (action != null)
			actions.add(action);
		while (true) {
			if (token.is(UND) || token.is(SOWIE)) {
				nextToken();
				action = readTEILAKTION();
				if (action != null)
					actions.add(action);
			} else {
				if (!token.is(new String[] { INSTANT, ENTFERNEN, WENN, KONTEXT,
						AUSSER, DANN, EOF, RULE_COMMENT })) {
					report.SOWIE(token);
					nextUntil(new String[] { INSTANT, WENN, KONTEXT, AUSSER,
							DANN, RULE_COMMENT });
				}
				return (actions.size() > 0 ? actions : null);
			}
		}

	}

	// TODO: Fehler abfangen
	private List<Answer> readANTWORTEN(QuestionChoice qc) {
		List<Answer> answerList = new LinkedList<Answer>();
		nextToken();
		if (token.is(ASSIGN_OP)) {
			nextToken();
			if (token.is(ECKIGE_KLAMMER_AUF)) {

				nextToken();
				boolean endOfList = false;
				while (!endOfList && token.is(SYMBOL)) {
					String tokenText = token.getSymbol();
					Answer answer = getAnswer(qc, tokenText);
					answerList.add(answer);
					nextToken();
					if (!token.is(KOMMA) && token.is(ECKIGE_KLAMMER_ZU)) {
						endOfList = true;
					} else if (token.is(KOMMA)) {
						nextToken();
					}
				}
			}
		}
		nextToken();
		return answerList;
	}

	// TODO: Fehler abfangen
	private List<QASet> readFRAGEN() {
		List<QASet> qaList = new LinkedList<QASet>();
		nextToken();
		if (token.is(ECKIGE_KLAMMER_AUF)) {
			nextToken();
			boolean endOfList = false;
			while (!endOfList && token.is(SYMBOL)) {
				String tokenText = token.getSymbol();
				QASet qaSet = getQASet(tokenText);
				if (qaSet == null) {
					report.KB_FRAGE_ERWARTET(tokenText, token);
				} else {
					qaList.add(qaSet);
					nextToken();
				}
				if (!token.is(KOMMA) && token.is(ECKIGE_KLAMMER_ZU)) {
					endOfList = true;
				} else if (token.is(KOMMA)) {
					nextToken();
				}
			}
		}
		nextToken();
		return qaList;
	}

	/*
	 * TEILAKTION := (FRAGE|DIAGNOSE) ASSIGN_OP (WERT|FORMEL|SCORE) TEILAKTION :=
	 * FRAGE ASSIGN_OP (WERT|FORMEL) | DIAGNOSE ASSIGN_OP SCORE | FRAGE { ';'
	 * FRAGE }* | FRAGEKLASSE { ';' FRAGEKLASSE }* TEILAKTION := VERBERGE FRAGE
	 * ASSIGN_OP ANTWORTENLISTE
	 * 
	 * FIRST (TEILAKTION) = { SYMBOL , VERBERGE } FOLLOW (TEILAKTION) = {
	 * ENTFERNEN, WENN, UND, SOWIE, EOF } KEYS (TEILAKTION) = { ENTFERNEN, WENN,
	 * KONTEXT, AUSSER, DANN, UND, SOWIE }
	 */
	private PseudoAction readTEILAKTION() {
		PseudoAction action = new PseudoAction();
		String[] label = new String[] { INSTANT, ENTFERNEN, WENN, KONTEXT,
				AUSSER, DANN, UND, SOWIE };

		// BEGRIFF
		String symbol = token.getSymbol();
		if (token.is(SYMBOL)) {
			action.question = getQuestion(symbol);
			action.diagnosis = getDiagnosis(symbol);
			action.qcontainer = getQContainer(symbol);
			if (action.question == null && action.diagnosis == null
					&& action.qcontainer == null) {

				String nextToken = showNextToken(1);
				if (nextToken.equals(ASSIGN_OP)) {
					String scndNextToken = showNextToken(2);
					if (QuestionNotInKBError.isYesOrNo(scndNextToken)) {
						report.KB_KEIN_BEGRIFF1(symbol, token, scndNextToken);
					} else if (ConceptNotInKBError.isValidScore(scndNextToken)) {
						report.KB_KEIN_BEGRIFF1(symbol, token, scndNextToken);
					} else {
						report.KB_KEIN_BEGRIFF(symbol, token);
					}
				} else {
					report.KB_KEIN_BEGRIFF(symbol, token);
				}

				nextUntil(label);
				return null;
			}
			nextToken();
		} else if (token.is(VERBERGE)) {
			nextToken();
			if (token.is(SYMBOL)) {
				String tokenText = token.getSymbol();
				Question question = getQuestion(tokenText);
				if (question == null || !(question instanceof QuestionChoice)) {
					report.KB_KEIN_BEGRIFF(symbol, token);
					nextUntil(label);
					return null;
				}
				// TODO: // orientiere an readMINMAX
				List<Answer> answerList = readANTWORTEN((QuestionChoice) question);
				PseudoAction pa = new PseudoAction();
				pa.question = question;
				pa.answerList = answerList;
				return pa;

			}
		} else if (token.is(INSTANT)) {
			List<QASet> qaiList = readFRAGEN();
			if (qaiList == null) {
				report.KB_KEIN_BEGRIFF(symbol, token);
				nextUntil(label);
				return null;
			} else {
				PseudoAction pa = new PseudoAction();
				pa.instantIndications.addAll(qaiList);
				return pa;
			}
		} else if (token.is(NICHT)) {
			// TODO: // orientiere an readMINMAX
			List<QASet> qaList = readFRAGEN();
			PseudoAction pa = new PseudoAction();
			pa.qaList = qaList;
			return pa;

		} else {
			report.AKTION(markPrev());
			nextUntil(label);
			return null;
		}

		// OPERATOR
		if (token.is(ASSIGN_OP) || token.is(EQUALS_OP)) {
			if (action.question == null && action.diagnosis == null) {
				report.KB_KEIN_BEGRIFF2(symbol, scanner.preview(-1));
				nextUntil(label);
				return null;
			}
			action.qcontainer = null;
			nextToken();
			int position = scanner.getPosition();
			String wert = readSymbol();

			// Diagnose, Score
			if (action.diagnosis != null) {
				if (wert == null) {
					if (action.question == null) {
						report.KB_SCORE_ERWARTET(symbol, scanner
								.getToken(position - 1));
						nextUntil(label);
						return null;
					}
					if (!token.is(NORMALE_KLAMMER_AUF)) {
						report.KB_WERT_ERWARTET(symbol, scanner
								.getToken(position - 1));
						nextUntil(label);
						return null;
					}
				} else {
					action.score = getScore(wert);
					if (action.score == null) {
						if (action.question == null) {
							report.KB_KEIN_SCORE(wert, symbol, scanner
									.getToken(position));
							nextUntil(label);
							return null;
						}
					} else {
						action.question = null;
						return action;
					}
				}
			}

			// Frage, Antwort/Formel
			action.diagnosis = null;
			action.score = null;
			if (wert == null && !token.is(NORMALE_KLAMMER_AUF)) {
				report.KB_ANTWORT_FORMEL_ERWARTET(symbol, scanner
						.getToken(position - 1));
				nextUntil(label);
				return null;
			}

			// Antwortalternative
			if (!token.is(NORMALE_KLAMMER_AUF)
					&& action.question instanceof QuestionChoice) {
				QuestionChoice q = (QuestionChoice) action.question;
				action.answer = getAnswer(q, wert);
				if (action.answer == null) {
					report.KB_KEINE_ANTWORT(symbol, wert, scanner
							.getToken(position), q);
					nextUntil(label);
					return null;
				}
				return action;
			}

			// Formel
			else {
				action.formula = readFORMEL();
				if (action.formula == null)
					return null;
				return action;
			}
		}

		// Antwortalternative hinzufuegen
		else if (token.is(PLUSGLEICH)) {
			if (action.question == null) {
				report.KB_KEIN_BEGRIFF3(symbol, scanner.preview(-1));
				nextUntil(label);
				return null;
			}
			nextToken();
			int position = scanner.getPosition();
			String wert = readSymbol();
			action.add = true;

			// Frage, Antwort/Formel
			action.diagnosis = null;
			action.score = null;
			if (wert == null && !token.is(NORMALE_KLAMMER_AUF)) {
				report.KB_ANTWORT_FORMEL_ERWARTET(symbol, scanner
						.getToken(position - 1));
				nextUntil(label);
				return null;
			}

			// Antwortalternative
			if (!token.is(NORMALE_KLAMMER_AUF)
					&& action.question instanceof QuestionChoice) {
				QuestionChoice q = (QuestionChoice) action.question;
				action.answer = getAnswer(q, wert);
				if (action.answer == null) {
					report.KB_KEINE_ANTWORT(symbol, wert, scanner
							.getToken(position), q);
					nextUntil(label);
					return null;
				}
				return action;
			}

			// Formel
			else {
				action.formula = readFORMEL();
				if (action.formula == null)
					return null;
				return action;
			}
		}

		// Indikationsregeln
		else if (token.is(KOMMA)) {
			if (action.question == null && action.qcontainer == null) {
				report.KB_KEIN_BEGRIFF4(symbol, scanner.preview(-1));
				nextUntil(label);
				return null;
			}
			action.diagnosis = null;
			boolean isContainer = true;
			action.indications = new ArrayList<QASet>();
			if (action.qcontainer == null) {
				isContainer = false;
				action.indications.add(action.question);
			} else
				action.indications.add(action.qcontainer);

			while (true) {
				nextToken();

				if (token.is(SYMBOL)) {
					String wert = token.getSymbol();
					action.question = getQuestion(wert);
					action.qcontainer = getQContainer(wert);
					if (action.question == null && action.qcontainer == null) {
						report.KB_KEIN_BEGRIFF4(wert, token);
						nextUntil(label);
						return null;
					}
					if (isContainer && action.qcontainer != null) {
						action.indications.add(action.qcontainer);
					} else if (!isContainer && action.question != null) {
						action.indications.add(action.question);
					} else {
						report.KB_NICHT_MISCHEN(wert, symbol, token);
					}
				} else if (!token.is(KOMMA)) {
					action.question = null;
					action.qcontainer = null;
					return action;
				}
			}
		} else {
			if (action.question == null && action.qcontainer == null) {
				report.ZUWEISUNG(scanner.preview(-1));
				nextUntil(label);
				return null;
			}
			ArrayList<QASet> list = new ArrayList<QASet>();
			if (action.qcontainer != null)
				list.add(action.qcontainer);
			else
				list.add(action.question);
			action.diagnosis = null;
			action.score = null;
			action.question = null;
			action.qcontainer = null;
			action.answer = null;
			action.formula = null;
			action.indications = list;
			return action;
		}

	}

	private String showNextToken(int i) {
		return scanner.showNext(i).getSymbol();

	}

	/*
	 * FORMEL := TEILFORMEL { RECHENOPERATOR TEILFORMEL }*
	 * 
	 * FIRST (FORMEL) = { PLUS, MINUS, SYMBOL, NORMALE_KLAMMER_AUF } FOLLOW
	 * (FORMEL) = { ENTFERNEN, WENN, UND, SOWIE, NORMALE_KLAMMER_ZU, EOF } KEYS
	 * (FORMEL) = { PLUS, MINUS, MAL, GETEILT }
	 */
	private FormulaNumberElement readFORMEL() {
		FormulaNumberElement teil, formel = readTEILFORMEL();
		boolean plusminus = false;

		while (true) {
			// PLUS
			if (token.is(PLUS)) {
				nextToken();
				teil = readTEILFORMEL();
				if (teil == null)
					formel = null;
				if (formel != null) {
					formel = new Add(formel, teil);
					plusminus = true;
				}
			}
			// MINUS
			else if (token.is(MINUS)) {
				nextToken();
				teil = readTEILFORMEL();
				if (teil == null)
					formel = null;
				if (formel != null) {
					formel = new Sub(formel, teil);
					plusminus = true;
				}
			}
			// MAL
			else if (token.is(MAL)) {
				nextToken();
				teil = readTEILFORMEL();
				if (teil == null)
					formel = null;
				if (formel != null) {
					if (plusminus) {
						Mult mult = new Mult();
						mult.setArg1(((FormulaNumberArgumentsTerm) formel)
								.getArg2());
						mult.setArg2(teil);
						((FormulaNumberArgumentsTerm) formel).setArg2(mult);
					} else {
						formel = new Mult(formel, teil);
					}
				}
			}
			// GETEILT
			else if (token.is(GETEILT)) {
				nextToken();
				teil = readTEILFORMEL();
				if (teil == null)
					formel = null;
				if (formel != null) {
					if (plusminus) {
						Div div = new Div();
						div.setArg1(((FormulaNumberArgumentsTerm) formel)
								.getArg2());
						div.setArg2(teil);
						((FormulaNumberArgumentsTerm) formel).setArg2(div);
					} else {
						formel = new Div(formel, teil);
					}
				}
			} else
				return formel;
		}
	}

	/*
	 * TEILFORMEL := (FORMEL) | SYMPTOM | ZAHL
	 * 
	 * FIRST (TEILFORMEL) = { PLUS, MINUS, SYMBOL, NORMALE_KLAMMER_AUF } FOLLOW
	 * (TEILFORMEL) = { ENTFERNEN, WENN, UND, SOWIE, NORMALE_KLAMMER_ZU, EOF,
	 * PLUS, MINUS, MAL, GETEILT } KEYS (TEILFORMEL) = { ENTFERNEN, WENN,
	 * KONTEXT, AUSSER, DANN, UND, SOWIE, PLUS, MINUS, MAL, GETEILT }
	 */
	private FormulaNumberElement readTEILFORMEL() {
		FormulaNumberElement formel = null;
		String vorzeichen = "";

		if (token.is(VORZEICHEN)) {
			if (isVorzeichen()) {
				vorzeichen = token.getToken();
				nextToken();
			}
		}
		if (token.is(SYMBOL)) {
			String symbol = token.getSymbol();
			if (!eos(vorzeichen))
				symbol = (vorzeichen.equalsIgnoreCase(MINUS) ? vorzeichen : "")
						+ token.getSymbol();

			// Zahl?
			try {
				formel = new FormulaNumber(new Double(Double.parseDouble(symbol
						.replace(',', '.'))));
				nextToken();
				return formel;
			} catch (Exception e) {
			}

			// Frage?
			Question question = getQuestion(symbol);
			if (question == null) {
				report.KB_KEINE_FORMEL(symbol, token);
			} else if (question instanceof QuestionNum) {
				formel = new QNumWrapper((QuestionNum) question);
			} else {
				report.KB_KEINE_QNUM(symbol, token);
			}
			nextToken();
		} else if (token.is(NORMALE_KLAMMER_AUF)) {
			nextToken();
			formel = readFORMEL();
			if (token.is(NORMALE_KLAMMER_ZU))
				nextToken();
			else {
				report.KLAMMER_ZU(markPrev());
				formel = null;
			}
		} else {
			report.FORMEL(markPrev());
			nextUntil(join(RECHENOPERATOR, new String[] { ENTFERNEN, WENN,
					KONTEXT, AUSSER, DANN, UND, SOWIE, NORMALE_KLAMMER_ZU }));
		}
		return formel;
	}

	/*
	 * SYMBOL := SYMBOL | VORZEICHEN SYMBOL
	 */
	private String readSymbol() {
		String vorzeichen = "";
		if (token.is(VORZEICHEN)) {
			if (isVorzeichen()) {
				vorzeichen = token.getToken();
				nextToken();
			}
		}
		if (token.is(SYMBOL)) {
			String wert = token.getSymbol();
			if (!eos(vorzeichen))
				wert = (vorzeichen.equalsIgnoreCase(MINUS) ? vorzeichen : "")
						+ token.getSymbol();
			nextToken();
			return wert;
		}
		return null;
	}

	/**
	 * Diese Klasse kapselt eine Aktion. (Entweder eine Diagnose, die ein Score
	 * erhï¿½llt, oder eine Frage, der ein Wert zugewiesen wird)
	 */
	private class PseudoAction {
		public Diagnosis diagnosis = null;
		public Score score = null;
		public Question question = null;
		public QContainer qcontainer = null;
		// public AnswerChoice answer = null;
		public Answer answer = null;
		public FormulaNumberElement formula = null;
		public List<QASet> indications = null;
		public List<QASet> instantIndications = new ArrayList<QASet>();
		public boolean add = false;
		// fuer Suppress-Regeln:
		public List<Answer> answerList = null;
		// fuer Contraindication-Regeln:
		public List<QASet> qaList = null;
	}

	/**
	 * Diese Klasse kapselt Statistiken. (Anzahl hinzugefuegter, entfernter,
	 * ersetzter Regeln)
	 */
	private class Counter {
		public int added = 0;
		public int cleared = 0;
		public int ignored = 0;
		public int replaced = 0;
	}

}
