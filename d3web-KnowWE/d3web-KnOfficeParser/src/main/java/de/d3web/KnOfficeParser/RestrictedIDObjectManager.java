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
 
package de.d3web.KnOfficeParser;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.d3web.KnOfficeParser.IDObjectManagement;
import de.d3web.core.KnowledgeBase;
import de.d3web.core.manage.AnswerFactory;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.values.AnswerChoice;
import de.d3web.core.terminology.Answer;
import de.d3web.core.terminology.Diagnosis;
import de.d3web.core.terminology.IDObject;
import de.d3web.core.terminology.NamedObject;
import de.d3web.core.terminology.QASet;
import de.d3web.core.terminology.QContainer;
import de.d3web.core.terminology.Question;
import de.d3web.core.terminology.QuestionChoice;
import de.d3web.core.terminology.QuestionDate;
import de.d3web.core.terminology.QuestionMC;
import de.d3web.core.terminology.QuestionNum;
import de.d3web.core.terminology.QuestionOC;
import de.d3web.core.terminology.QuestionSolution;
import de.d3web.core.terminology.QuestionText;
import de.d3web.core.terminology.QuestionYN;
import de.d3web.core.terminology.QuestionZC;
import de.d3web.core.terminology.info.DCElement;
import de.d3web.core.terminology.info.DCMarkup;
import de.d3web.core.terminology.info.MMInfoObject;
import de.d3web.core.terminology.info.MMInfoStorage;
import de.d3web.core.terminology.info.MMInfoSubject;
import de.d3web.core.terminology.info.Properties;
import de.d3web.core.terminology.info.Property;

/**
 * An implementation of the IDObjectManagement for the package CostBenefit.
 * It searches for questions in the actual QContainer. This provides the possibility to differ between
 * questions with the same text.
 * @author Markus Friedrich (denkbares GmbH)
 */
public class RestrictedIDObjectManager implements IDObjectManagement {

	private KnowledgeBaseManagement kbm;
	private QContainer currentQContainer;
	private boolean lazyQuestions=false;
	private boolean lazyDiags=false;
	private boolean lazyAnswers=false;
	
	public RestrictedIDObjectManager(KnowledgeBaseManagement kbm) {
		super();
		this.kbm = kbm;
	}
	
	
	public KnowledgeBaseManagement getKbm() {
		return kbm;
	}



	public void setKbm(KnowledgeBaseManagement kbm) {
		this.kbm = kbm;
	}

	


	boolean isLazyQuestions() {
		return lazyQuestions;
	}


	public void setLazyQuestions(boolean lazyQuestions) {
		this.lazyQuestions = lazyQuestions;
	}


	boolean isLazyAnswers() {
		return lazyAnswers;
	}


	public void setLazyAnswers(boolean lazyAnswers) {
		this.lazyAnswers = lazyAnswers;
	}


	public boolean isLazyDiags() {
		return lazyDiags;
	}


	public void setLazyDiags(boolean lazyDiags) {
		this.lazyDiags = lazyDiags;
	}


	public QContainer getCurrentQContainer() {
		return currentQContainer;
	}



	public void setCurrentQContainer(QContainer currentQContainer) {
		this.currentQContainer = currentQContainer;
	}



	@Override
	public Answer findAnswer(Question q, String name) {
		Answer answer = kbm.findAnswer(q, name);
		if (name.equalsIgnoreCase("unknown")||name.equalsIgnoreCase("unbekannt")) {
			return q.getUnknownAlternative();
		}
		if (answer==null&&lazyAnswers) {
			QuestionChoice qc = (QuestionChoice) q;
			AnswerChoice ac = AnswerFactory.createAnswerChoice(kbm.findNewIDForAnswerChoice(qc), name);
			qc.addAlternative(ac);
			answer=ac;
		}
		return answer;
	}

	@Override
	public AnswerChoice findAnswerChoice(QuestionChoice qc, String name) {
		AnswerChoice answer = kbm.findAnswerChoice(qc, name);
		if (answer==null&&lazyAnswers) {
			answer = AnswerFactory.createAnswerChoice(kbm.findNewIDForAnswerChoice(qc), name);
			qc.addAlternative(answer);
		}
		return answer;
	}

	@Override
	public Diagnosis findDiagnosis(String name) {
		Diagnosis diag = kbm.findDiagnosis(name);
		if (diag==null&&lazyDiags) {
			diag=createDiagnosis(name, null);
		}
		return diag;
	}

	@Override
	public QContainer findQContainer(String name) {
		return kbm.findQContainer(name);
	}

	@Override
	public Question findQuestion(String name) {
		if (currentQContainer==null) {
			Question q = kbm.findQuestion(name);
			if (q==null&&lazyQuestions) {
				q=createQuestionOC(name, kbm.getKnowledgeBase().getRootQASet(), new String[0]);
			}
			return q;
		} else {
			List<Question> questions = new LinkedList<Question>();
			collectQuestions(currentQContainer, questions);
			for (Question q: questions) {
				Properties properties = q.getProperties();
				MMInfoStorage mmis = (MMInfoStorage) properties.getProperty(Property.MMINFO);
				if (mmis!=null) {
					DCMarkup markup = new DCMarkup();
					markup.setContent(DCElement.SUBJECT,
							MMInfoSubject.PROMPT.getName());
					Set<MMInfoObject> info = mmis.getMMInfo(markup);
					for (MMInfoObject mmio : info) {
						if (mmio.getContent().equals(name)) {
							return q;
						}
					}
				}
			}
			//if there is no question with the text, search for one with the name
			for (NamedObject no: questions) {
				if (no instanceof Question) {
					Question q = (Question) no;
					if (q.getText().equals(name)) {
						return q;
					}
				}
			}
			//if there is still no question found and lazy is true, generate it
			if (lazyQuestions) {
				return createQuestionOC(name, currentQContainer, new String[0]);
			}
			return kbm.findQuestion(name);
		}
	}


	@Override
	public Diagnosis createDiagnosis(String name, Diagnosis parent) {
		if (parent==null) {
			parent=kbm.getKnowledgeBase().getRootDiagnosis();
		}
		return kbm.createDiagnosis(name, parent);
	}


	@Override
	public QContainer createQContainer(String name, QASet parent) {
		if (parent==null) {
			parent=kbm.getKnowledgeBase().getRootQASet();
		}
		return kbm.createQContainer(name, parent);
	}


	@Override
	public QuestionDate createQuestionDate(String name, QASet parent) {
		return kbm.createQuestionDate(name, parent);
	}


	@Override
	public QuestionMC createQuestionMC(String name, QASet parent,
			AnswerChoice[] answers) {
		return kbm.createQuestionMC(name, parent, answers);
	}


	@Override
	public QuestionMC createQuestionMC(String name, QASet parent,
			String[] answers) {
		return kbm.createQuestionMC(name, parent, answers);
	}


	@Override
	public QuestionNum createQuestionNum(String name, QASet parent) {
		return kbm.createQuestionNum(name, parent);
	}


	@Override
	public QuestionOC createQuestionOC(String name, QASet parent,
			AnswerChoice[] answers) {
		return kbm.createQuestionOC(name, parent, answers);
	}


	@Override
	public QuestionOC createQuestionOC(String name, QASet parent,
			String[] answers) {
		return kbm.createQuestionOC(name, parent, answers);
	}


	@Override
	public QuestionSolution createQuestionState(String name, QASet parent) {
		return kbm.createQuestionState(name, parent);
	}


	@Override
	public QuestionText createQuestionText(String name, QASet parent) {
		return kbm.createQuestionText(name, parent);
	}


	@Override
	public QuestionYN createQuestionYN(String name, QASet parent) {
		return kbm.createQuestionYN(name, parent);
	}


	@Override
	public QuestionYN createQuestionYN(String name, String yesAlternativeText,
			String noAlternativeText, QASet parent) {
		return kbm.createQuestionYN(name, yesAlternativeText, noAlternativeText, parent);
	}


	@Override
	public QuestionZC createQuestionZC(String name, QASet parent) {
		return kbm.createQuestionZC(name, parent);
	}


	@Override
	public Answer addChoiceAnswer(QuestionChoice qc, String value) {
		return kbm.addChoiceAnswer(qc, value);
	}


	@Override
	public boolean changeID(IDObject object, String ref) {
		return kbm.changeID(object, ref);
	}


	@Override
	public String findNewIDFor(Class<? extends IDObject> object) {
		return kbm.findNewIDFor(object);
	}


	@Override
	public String findNewIDForAnswerChoice(QuestionChoice currentQuestion) {
		return kbm.findNewIDForAnswerChoice(currentQuestion);
	}


	@Override
	public KnowledgeBase getKnowledgeBase() {
		return kbm.getKnowledgeBase();
	}
	
	private void collectQuestions(NamedObject namedObject, List<Question> result) {
		if (namedObject instanceof Question && !result.contains(namedObject)) {
			result.add((Question) namedObject);
		}
		for (NamedObject child : namedObject.getChildren()) {
			collectQuestions(child, result);
		}
	}
}
