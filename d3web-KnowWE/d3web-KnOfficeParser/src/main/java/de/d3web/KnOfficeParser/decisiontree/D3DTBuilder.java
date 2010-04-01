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

package de.d3web.KnOfficeParser.decisiontree;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.tools.ant.util.ReaderInputStream;

import de.d3web.KnOfficeParser.DefaultLexer;
import de.d3web.KnOfficeParser.KnOfficeParameterSet;
import de.d3web.KnOfficeParser.KnOfficeParser;
import de.d3web.KnOfficeParser.util.ConditionGenerator;
import de.d3web.KnOfficeParser.util.D3webQuestionFactory;
import de.d3web.KnOfficeParser.util.DefaultD3webLexerErrorHandler;
import de.d3web.KnOfficeParser.util.DefaultD3webParserErrorHandler;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.KnOfficeParser.util.Scorefinder;
import de.d3web.abstraction.formula.FormulaExpression;
import de.d3web.abstraction.formula.FormulaNumber;
import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.TerminalCondition;
import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.Diagnosis;
import de.d3web.core.knowledge.terminology.DiagnosisState;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.knowledge.terminology.info.MMInfoSubject;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.knowledge.terminology.info.Properties;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.AnswerFactory;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.values.AnswerChoice;
import de.d3web.report.Message;
import de.d3web.scoring.Score;

/**
 * Builder um Mithilfe des Entscheidungsbaumparsers die geparsten Elemente in
 * ein KBM einzutragen
 * 
 * @author Markus Friedrich
 * 
 */
public class D3DTBuilder implements DTBuilder, KnOfficeParser {

	public static List<Message> parse(Reader reader, IDObjectManagement idom) {
		D3DTBuilder builder = new D3DTBuilder("", idom);
		return builder.addKnowledge(reader, idom, null);
	}

	private Question currentQuestion;
	private Stack<Tupel<Integer, Question>> questionStack = new Stack<Tupel<Integer, Question>>();;
	private Stack<Integer> conddashstack = new Stack<Integer>();
	private QASet currentQuestionclass;
	private Stack<TerminalCondition> conditionStack = new Stack<TerminalCondition>();
	private List<Tripel<String, Condition, Message>> qcontainertolink = new ArrayList<Tripel<String, Condition, Message>>();
	private List<Tripel<String, Object, Message>> descriptionlinks = new ArrayList<Tripel<String, Object, Message>>();
	private List<String> allowedNames;
	private List<Message> errors = new ArrayList<Message>();
	private String file;
	private boolean complexconditions = false;
	private IDObjectManagement idom;

	public boolean isComplexconditions() {
		return complexconditions;
	}

	public void setComplexconditions(boolean complexconditions) {
		this.complexconditions = complexconditions;
	}

	/**
	 * Innere Klasse um ein generisches Tupel aufzunehmen
	 * 
	 * @author Markus Friedrich
	 * 
	 * @param <T>
	 * @param <X>
	 */
	private class Tupel<T, X> {
		private T first;
		private X second;
		boolean used = false;

		public Tupel(T t, X x) {
			first = t;
			second = x;
		}
	}

	private class Tripel<T, X, U> extends Tupel<T, X> {
		private U third;

		public Tripel(T t, X x, U u) {
			super(t, x);
			third = u;
		}
	}

	public D3DTBuilder(String file, IDObjectManagement idom) {
		this.file = file;
		this.idom = idom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see DTBuilder#addAnswerOrQuestionLink(int, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void addAnswerOrQuestionLink(int dashes, String name, String ref,
			List<String> syn, boolean def, boolean init, int line,
			String linetext, String idlink) {
		if (questionStack.isEmpty()) {
			errors.add(MessageKnOfficeGenerator.createNoQuestionOnStack(file,
					line, linetext));
			return;
		}
		// answer
		if (dashes == questionStack.peek().first + 1) {
			currentQuestion = questionStack.peek().second;
			AnswerChoice answer = null;
			if (currentQuestion instanceof QuestionYN) {
				QuestionYN cq = (QuestionYN) currentQuestion;
				if (name.equalsIgnoreCase("ja") || name.equalsIgnoreCase("yes")) {
					answer = cq.yes;
				} else if (name.equalsIgnoreCase("nein")
						|| name.equalsIgnoreCase("no")) {
					answer = cq.no;
				} else {
					errors.add(MessageKnOfficeGenerator.createWrongYNAnswer(
							file, line, linetext, cq.getName()));
					return;
				}
			} else if (currentQuestion instanceof QuestionOC) {
				if (ref != null) {
					answer = createAnswer(name, currentQuestion.getId() + ref);
				} else {
					answer = createAnswer(name, null);
				}
				QuestionOC cq = (QuestionOC) currentQuestion;
				cq.addAlternative(answer);
			} else if (currentQuestion instanceof QuestionMC) {
				if (ref != null) {
					answer = createAnswer(name, currentQuestion.getId() + ref);
				} else {
					answer = createAnswer(name, null);
				}
				QuestionMC cq = (QuestionMC) currentQuestion;
				cq.addAlternative(answer);
			} else {
				errors.add(MessageKnOfficeGenerator
						.createNoAnswerAllowedException(file, line, linetext));
				return;
			}
			CondEqual c = new CondEqual((QuestionChoice) currentQuestion,
					answer);
			conditionStack.push(c);
			conddashstack.push(dashes);
			if (idlink != null) {
				descriptionlinks.add(new Tripel<String, Object, Message>(
						idlink, answer, MessageKnOfficeGenerator
								.createDescriptionTextNotFoundError(file, line,
										linetext, idlink)));
			}
			// answer is the default answer
			if (def) {
				setAnswerPropertytoCurrentQuestion(answer, Property.DEFAULT);
			}
			// the question is initialised with this answer
			if (init) {
				setAnswerPropertytoCurrentQuestion(answer, Property.INIT);
			}
		}
		// Link
		else if (dashes == questionStack.peek().first + 2) {
			Condition cond = getCondPath(line, linetext);
			addQcontainerIndication(name, ref, syn, def, line, linetext,
					idlink, cond);
			// Frageklasse an Diagnose
		} else if ((dashes == questionStack.peek().first + 3)) {
			if (dashes == conddashstack.peek() + 1) {
				Condition cond = conditionStack.peek();
				addQcontainerIndication(name, ref, syn, def, line, linetext,
						idlink, cond);
			} else {
				errors.add(MessageKnOfficeGenerator.createNoAddtoLink(file,
						line, linetext));
			}
		} else {
			errors.add(MessageKnOfficeGenerator.createNoAddtoLink(file, line,
					linetext));
		}

	}

	private void setAnswerPropertytoCurrentQuestion(AnswerChoice answer,
			Property property) {
		Properties properties = currentQuestion.getProperties();
		Object defproperty = properties.getProperty(property);
		if (defproperty != null) {
			if (defproperty instanceof String) {
				String s = (String) defproperty;
				if (!s.contains(answer.getId())) {
					s += ";" + answer.getId();
					defproperty = s;
				}
			}
		} else {
			defproperty = answer.getId();
		}
		properties.setProperty(property, defproperty);
	}

	private void addQcontainerIndication(String name, String ref,
			List<String> syn, boolean def, int line, String linetext,
			String idlink, Condition cond) {
		QContainer qcon = idom.findQContainer(name);
		if (qcon != null) {
			addQuestionOrQuestionclassIndication(qcon, getCondPath(line,
					linetext), line, linetext);
		} else {
			qcontainertolink
					.add(new Tripel<String, Condition, Message>(name,
							cond, MessageKnOfficeGenerator
									.createQuestionClassNotFoundException(file,
											line, linetext, name)));
			if ((ref != null) || (syn != null) || (def)) {
				errors.add(MessageKnOfficeGenerator
						.createTooManyPropertiesOnQuestionLinkWarning(file,
								line, linetext));
			}
			if (idlink != null) {
				errors.add(MessageKnOfficeGenerator
						.createNoDescriptionsAtQuestionClassWarning(file, line,
								linetext));
			}
		}
	}

	private Condition getCondPath(int line, String linetext) {
		Condition c;
		if (conditionStack.isEmpty()) {
			errors.add(MessageKnOfficeGenerator.createNoValidCondsException(
					file, line, linetext));
			return null;
		}
		if ((conditionStack.size() <= 1) || (!complexconditions)) {
			c = conditionStack.peek();
		} else {
			c = new CondAnd(new ArrayList<Condition>(conditionStack));
		}
		return c;
	}

	private AnswerChoice createAnswer(String name, String ref) {
		if (ref == null) {
			ref = idom
					.findNewIDForAnswerChoice((QuestionChoice) currentQuestion);
		}
		AnswerChoice answer = AnswerFactory.createAnswerChoice(ref, name);
		return answer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see DTBuilder#addDescription(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void addDescription(String id, String type, String des, String text,
			int line, String linetext, String language) {
		boolean inApostrophes = false;
		if ((des.startsWith("'")) && (des.endsWith("'"))) {
			inApostrophes = true;
			des = des.substring(1, des.length() - 1);
		}
		if (allowedNames != null && (!allowedNames.contains(des))
				&& (!inApostrophes)) {
			errors.add(MessageKnOfficeGenerator.createNameNotAllowedWarning(
					file, line, linetext, des));
		}

		// check if the subject is allowed
		// (if the type is a defined subject)
		boolean isTypeAllowed = false;
		for (MMInfoSubject subject : MMInfoSubject.getSubjects()) {
			if (subject.getName().equalsIgnoreCase(type)) {
				type = subject.getName(); // revert upper/lower case
				isTypeAllowed = true;
				break;
			}
		}
		if (!isTypeAllowed) {
			errors.add(MessageKnOfficeGenerator.createTypeNotAllowed(file,
					line, linetext, type));
			return;
		}

		for (Tupel<String, Object> t : descriptionlinks) {
			if (t.first.equals(id)) {
				t.used = true;
				if (t.second instanceof NamedObject) {
					NamedObject na = (NamedObject) t.second;
					addMMInfo(na, des, type, text, language);
				} else if (t.second instanceof AnswerChoice) {
					AnswerChoice ac = (AnswerChoice) t.second;
					ac.getProperties().setProperty(Property.EXPLANATION, text);
				} else {
					errors.add(MessageKnOfficeGenerator
							.createDescriptionNotAllowed(file, line, linetext));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see DTBuilder#addDiagnosis(int, java.lang.String)
	 */
	@Override
	public void addDiagnosis(int dashes, List<String> diags, boolean set,
			String value, String link, String linkdes, int line,
			String linetext, String idlink) {
		for (String name : diags) {
			Question q = idom.findQuestion(name);
			Condition cond = getCondPath(line, linetext);
			String newRuleID = idom.createRuleID();
			// Merkmalsherleitung
			if (q != null) {
				if (q instanceof QuestionNum) {
					Double d = parseGerDouble(value);
					if (d == null) {
						errors.add(MessageKnOfficeGenerator
								.createNaNAtFeatureDerivationError(file, line,
										linetext, value));
						continue;
					}
					FormulaNumber num = new FormulaNumber(d);
					FormulaExpression e = new FormulaExpression(q, num);
					if (set) {
						RuleFactory.createSetValueRule(newRuleID, q, e, cond);
					} else {
						RuleFactory.createAddValueRule(newRuleID, q,
								new Object[] { e }, cond);
					}
				} else {
					Answer a = idom.findAnswer(q, value);
					if (set) {
						RuleFactory.createSetValueRule(newRuleID, q,
								new Object[] { a }, cond);
					} else {
						RuleFactory.createAddValueRule(newRuleID, q,
								new Object[] { a }, cond);
					}
				}
				if (idlink != null) {
					descriptionlinks.add(new Tripel<String, Object, Message>(
							idlink, q, MessageKnOfficeGenerator
									.createDescriptionTextNotFoundError(file,
											line, linetext, idlink)));
				}
				if ((link != null) || (linkdes != null)) {
					errors
							.add(MessageKnOfficeGenerator
									.createNotUsedDescriptionsAtFeatureDerivationWarning(
											file, line, linetext));
				}
			}
			// Diagnoseherleitung
			else {
				Diagnosis diag = idom.findDiagnosis(name);
				if (diag == null) {
					errors.add(MessageKnOfficeGenerator
							.createDiagnosisNotFoundException(file, line,
									linetext, name));
					continue;
				} else if (set) {
					errors.add(MessageKnOfficeGenerator
							.createSetOnlyAllowedAtFeatureDerivationWarning(
									file, line, linetext));
				}
				Score score = Scorefinder.getScore(value);
				if (score == null) {
					errors.add(MessageKnOfficeGenerator
							.createScoreDoesntExistError(file, line, linetext,
									value));
					return;
				}
				RuleFactory.createHeuristicPSRule(newRuleID, diag, score, cond);
				addMMInfo(diag, "Link", MMInfoSubject.LINK.getName(), link,
						null);
				// TODO Linkbeschreibung?
				if (idlink != null) {
					descriptionlinks.add(new Tripel<String, Object, Message>(
							idlink, diag, MessageKnOfficeGenerator
									.createDescriptionTextNotFoundError(file,
											line, linetext, idlink)));
				}
				// Condition falls Frageklassenindikationen als Kinder
				// existieren
				DiagnosisState state;
				if (score == Score.P7 || score == Score.P6) {
					state = DiagnosisState.ESTABLISHED;
				} else if (score == Score.P5 || score == Score.P4
						|| score == Score.P3) {
					state = DiagnosisState.SUGGESTED;
				} else {
					state = DiagnosisState.UNCLEAR;
				}
				TerminalCondition c = new CondDState(diag, state);
				conditionStack.push(c);
				conddashstack.push(dashes);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see DTBuilder#addInclude(int, java.lang.String)
	 */
	@Override
	public void addInclude(String url, int line, String linetext) {
		// wird atm nicht ben�tigt
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see DTBuilder#addNumericAnswer(int, double, double)
	 */
	@Override
	public void addNumericAnswer(int dashes, Double a, Double b, String op,
			int line, String linetext) {
		if (currentQuestion instanceof QuestionNum) {
			QuestionNum qnum = (QuestionNum) currentQuestion;
			TerminalCondition c;
			if (op != null) {
				c = ConditionGenerator.condNum(qnum, op, a, errors, line,
						linetext, file);
			} else {
				c = ConditionGenerator.condNum(qnum, a, b, errors, line,
						linetext, file);
			}
			conditionStack.push(c);
			conddashstack.push(dashes);
		} else {
			errors.add(MessageKnOfficeGenerator.createNoNumQuestionException(
					file, line, linetext));
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see DTBuilder#addQuestion(int, java.lang.String, java.lang.String,
	 * boolean, java.lang.String, java.lang.String)
	 */
	@Override
	public void addQuestion(int dashes, String name, String longname,
			boolean abs, String type, String ref, Double lowerbound,
			Double upperbound, String unit, List<String> syn, int line,
			String linetext, String idlink, List<String> attributes,
			List<String> values) {
		QASet parent;
		if (dashes == 1) {
			parent = currentQuestionclass;
		} else {
			if (!questionStack.isEmpty()) {
				parent = questionStack.peek().second;
			} else {
				parent = null;
				errors.add(MessageKnOfficeGenerator
						.createNoParentQuestionError(file, line, linetext));
				return;
			}
		}
		if (type == null) {
			errors.add(MessageKnOfficeGenerator.createTypInexistentError(file,
					line, linetext, name));
			return;
		}
		currentQuestion = idom.findQuestion(name);
		if (currentQuestion != null) {
			if (parent != null) {
				parent.addLinkedChild(currentQuestion);
				currentQuestion.addLinkedParent(parent);
			}else {
				errors.add(MessageKnOfficeGenerator
						.createNoParentQuestionError(file, line, linetext));
				return;
			}
			if (ref!=null && !ref.equals(currentQuestion.getId())) {
				errors.add(MessageKnOfficeGenerator.createCannotChangeIDError(file, line, linetext, name));
			}
		} else {
			currentQuestion = D3webQuestionFactory.createQuestion(idom, parent,
					name, ref, type);
		}

		if (currentQuestion == null) {
			errors.add(MessageKnOfficeGenerator.createTypeRecognitionError(
					file, line, linetext, name, type));
			return;
		}
		if (currentQuestion instanceof QuestionNum) {
			Properties prop = currentQuestion.getProperties();
			if (upperbound != null && lowerbound != null) {
				if (lowerbound <= upperbound) {
					NumericalInterval range = new NumericalInterval(lowerbound,
							upperbound);
					prop.setProperty(Property.QUESTION_NUM_RANGE, range);
				} else {
					errors.add(MessageKnOfficeGenerator
							.createIntervallRangeError(file, line, linetext));
				}
			}
			if (unit != null) {
				prop.setProperty(Property.UNIT, unit);
			}
		} else if (unit != null || lowerbound != null || upperbound != null) {
			errors.add(MessageKnOfficeGenerator
					.createUnitAndRangeOnlyAtNumWarning(file, line, linetext));
		}
		Properties prop = currentQuestion.getProperties();
		// Setzen wenn die Frage abstrakt ist
		if (abs) {
			prop.setProperty(Property.ABSTRACTION_QUESTION, Boolean.TRUE);
		}
		// Setzen des langen Fragetextes
		if (longname != null) {
			addMMInfo(currentQuestion, "LT", MMInfoSubject.PROMPT.getName(),
					longname, null);
		}
		
		// Wenn die Frage eine Folgefrage auf eine Antwort ist, diese der
		// Antwort zuordnen
		if ((dashes != 1) && (dashes == questionStack.peek().first + 2)) {
			Condition abscon = getCondPath(line, linetext);
			addQuestionOrQuestionclassIndication(currentQuestion, abscon, line,
					linetext);

		}
		// Wenn die Frage eine Folgefrage auf eine Diagnose ist, diese der
		// Antwort zuordnen
		if ((dashes != 1) && (dashes == questionStack.peek().first + 3)) {
			Condition abscon = getCondPath(line, linetext);
			addQuestionOrQuestionclassIndication(currentQuestion, abscon, line,
					linetext);

		}
		
		
		questionStack
				.push(new Tupel<Integer, Question>(dashes, currentQuestion));
		if (idlink != null) {
			descriptionlinks.add(new Tripel<String, Object, Message>(idlink,
					currentQuestion, MessageKnOfficeGenerator
							.createDescriptionTextNotFoundError(file, line,
									linetext, idlink)));
		}
		if (attributes != null && values != null) {
			// TODO setzen der Dialogannotationen
			int i = 0;
			//System.out.println("Frage " + name + ": ");
			for (String s : attributes) {
				System.out.println(s + "=" + values.get(i));
				i++;
			}
		}
	}

	public void finishOldQuestionsandConditions(int dashes) {
		// System.out.println(dashes);
		while ((!conditionStack.isEmpty()) && (conddashstack.peek() >= dashes)) {
			conddashstack.pop();
			conditionStack.pop();
		}
		while (!questionStack.isEmpty()
				&& (questionStack.peek().first >= dashes)) {
			questionStack.pop();
		}
		if (!questionStack.isEmpty()) {
			currentQuestion = questionStack.peek().second;
		}
	}

	private void addQuestionOrQuestionclassIndication(QASet set,
			Condition abscon, int line, String linetext) {
		String newRuleID = idom.createRuleID();
		if (abscon instanceof CondDState) {
			CondDState statecond = (CondDState) abscon;
			List<QASet> action = new ArrayList<QASet>();
			action.add(set);
			if (statecond.getStatus() == DiagnosisState.ESTABLISHED) {
				RuleFactory.createRefinementRule(newRuleID, action, statecond
						.getDiagnosis(), statecond);
			} else if (statecond.getStatus() == DiagnosisState.SUGGESTED) {
				RuleFactory.createClarificationRule(newRuleID, action,
						statecond.getDiagnosis(), statecond);
			} else {
				errors.add(MessageKnOfficeGenerator.createWrongDiagScore(
						newRuleID, line, linetext));
			}
		} else {
			RuleFactory.createIndicationRule(newRuleID, set, abscon);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see DTBuilder#addQuestionclass(java.lang.String)
	 */
	@Override
	public void addQuestionclass(String name, int line, String linetext,
			List<String> attributes, List<String> values) {
		if (name.equalsIgnoreCase("default")) {
			// TODO defaults setzen
			int i = 0;
			System.out.println("Default: ");
			if (attributes != null && values != null) {
				for (String s : attributes) {
					System.out.println(s + "=" + values.get(i));
					i++;
				}
			}
			return;
		}
		QASet qs1 = idom.findQContainer(name);
		if (qs1 == null) {
			qs1 = idom.createQContainer(name, idom.getKnowledgeBase()
					.getRootQASet());
			if (idom.getKnowledgeBase().getInitQuestions().isEmpty()) {
				ArrayList<QASet> tmp = new ArrayList<QASet>();
				tmp.add(qs1);
				idom.getKnowledgeBase().setInitQuestions(tmp);
			}
		}
		currentQuestionclass = qs1;
		// hinzufügen von Regeln vorher eingefügter Links auf diesen QContainer
		List<Tupel<String, Condition>> delList = new ArrayList<Tupel<String, Condition>>();
		for (Tupel<String, Condition> t : qcontainertolink) {
			if (t.first.equals(name)) {
				addQuestionOrQuestionclassIndication(qs1, t.second, line,
						linetext);
				delList.add(t);
			}
		}
		// Löschen der gesetzten Links
		for (Tupel<String, Condition> t : delList) {
			qcontainertolink.remove(t);
		}
		if (attributes != null && values != null) {
			// TODO setzen der Dialogannotationen
			int i = 0;
			System.out.println("Frageklasse " + name + ": ");
			for (String s : attributes) {
				System.out.println(s + "=" + values.get(i));
				i++;
			}
		}
	}

	/**
	 * @author Jochen Reutelshoefer
	 * @param o
	 * @param title
	 * @param subject
	 * @param content
	 */
	private void addMMInfo(NamedObject o, String title, String subject,
			String content, String language) {
		if (o == null)
			return;
		if (content == null)
			return;

		if (content.startsWith("\"") && content.endsWith("\"")
				&& content.length() > 1) {
			content = content.substring(1, content.length() - 1);
		}

		MMInfoStorage mmis;
		DCMarkup dcm = new DCMarkup();
		dcm.setContent(DCElement.TITLE, title);
		dcm.setContent(DCElement.SUBJECT, subject);
		dcm.setContent(DCElement.SOURCE, o.getId());
		if (language != null)
			dcm.setContent(DCElement.LANGUAGE, language);
		MMInfoObject mmi = new MMInfoObject(dcm, content);
		if (o.getProperties().getProperty(Property.MMINFO) == null) {
			mmis = new MMInfoStorage();
		} else {
			mmis = (MMInfoStorage) o.getProperties().getProperty(
					Property.MMINFO);
		}
		o.getProperties().setProperty(Property.MMINFO, mmis);
		mmis.addMMInfo(mmi);
	}

	@Override
	public void addQuestionLink(int dashes, String name, int line,
			String linetext) {
		currentQuestion = idom.findQuestion(name);
		if (currentQuestion == null) {
			errors
					.add(MessageKnOfficeGenerator
							.createQuestionNotFoundException(file, line,
									linetext, name));
			return;
		}
		if (dashes == 1) {
			currentQuestion.addParent(currentQuestionclass);
		} else {
			currentQuestion.addParent(questionStack.peek().second);
			// Wenn die Frage eine Folgefrage auf eine Antwort ist, diese der
			// Antwort zuordnen
			if (dashes == questionStack.peek().first + 2) {
				Condition abs = getCondPath(line, linetext);
				addQuestionOrQuestionclassIndication(currentQuestion, abs,
						line, linetext);
			}
		}
		questionStack
				.push(new Tupel<Integer, Question>(dashes, currentQuestion));

	}

	private Double parseGerDouble(String s) {
		if (s == null)
			return null;
		s = s.replace(',', '.');
		Double d;
		try {
			d = Double.parseDouble(s);
		} catch (NumberFormatException e) {
			d = null;
		}
		return d;
	}

	@Override
	public void setallowedNames(List<String> allowedNames, int line,
			String linetext) {
		this.allowedNames = allowedNames;
	}

	public List<Message> getErrors() {
		List<Message> ret = new ArrayList<Message>(errors);
		// prüfen ob noch nichtgesetzte Links zu Frageklassen vorhanden sind
		for (Tripel<String, Condition, Message> t : qcontainertolink) {
			ret.add(t.third);
		}
		// prüfen ob noch nichtgesetzte Links zu Erklärungen vorhanden sind
		for (Tripel<String, Object, Message> t : descriptionlinks) {
			if (!t.used)
				ret.add(t.third);
		}
		if (ret.size() == 0) {
			ret.add(MessageKnOfficeGenerator.createDTparsedNote(file, 0, "",
					idom.getKnowledgeBase().getQuestions().size()));
		}
		return ret;
	}

	@Override
	public List<Message> addKnowledge(Reader r, IDObjectManagement idom,
			KnOfficeParameterSet s) {
		this.idom = idom;
		ReaderInputStream input = new ReaderInputStream(r);
		ANTLRInputStream istream = null;
		try {
			istream = new ANTLRInputStream(input);
		} catch (IOException e1) {
			errors.add(MessageKnOfficeGenerator.createAntlrInputError(file, 0,
					""));
		}
		DefaultLexer lexer = new DefaultLexer(istream,
				new DefaultD3webLexerErrorHandler(errors, file));
		lexer.setNewline(true);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		DecisionTree parser = new DecisionTree(tokens, this,
				new DefaultD3webParserErrorHandler(errors, file, "BasicLexer"));
		try {
			parser.knowledge();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
		return getErrors();
	}

	@Override
	public List<Message> checkKnowledge() {
		return getErrors();
	}

	@Override
	public void addManyQuestionClassLink(int dashes, List<String> qcs,
			int line, String string) {
		for (String s : qcs) {
			addAnswerOrQuestionLink(dashes, s, null, null, false, false, line,
					string, null);
		}

	}

	@Override
	public void line(String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void newLine() {
		// TODO Auto-generated method stub

	}

}
