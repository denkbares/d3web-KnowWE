/*
 * Copyright (C) 2009 denkbares GmbH
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

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.knowledge.terminology.info.MMInfoSubject;
import de.d3web.core.knowledge.terminology.info.Properties;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.AnswerFactory;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.AnswerChoice;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.Unknown;

/**
 * An implementation of the IDObjectManagement for the package CostBenefit.
 * It searches for questions in the actual QContainer. This provides the possibility to differ between
 * questions with the same text.
 * @author Markus Friedrich (denkbares GmbH)
 */
public class RestrictedIDObjectManager extends SingleKBMIDObjectManager {

	private QContainer currentQContainer;
	private boolean lazyQuestions=false;
	private boolean lazyDiags=false;
	private boolean lazyAnswers=false;
	
	public RestrictedIDObjectManager(KnowledgeBaseManagement kbm) {
		super(kbm);
	}
	
	
	public boolean isLazyQuestions() {
		return lazyQuestions;
	}


	public void setLazyQuestions(boolean lazyQuestions) {
		this.lazyQuestions = lazyQuestions;
	}


	public boolean isLazyAnswers() {
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
	public Value findValue(Question q, String name) {
		Value answer = kbm.findValue(q, name);
		if (name.equalsIgnoreCase("unknown")||name.equalsIgnoreCase("unbekannt")) {
			return Unknown.getInstance();
		}
		if (answer==null&&lazyAnswers) {
			QuestionChoice qc = (QuestionChoice) q;
			AnswerChoice ac = AnswerFactory.createAnswerChoice(kbm.findNewIDForAnswerChoice(qc), name);
			qc.addAlternative(ac);
			answer = new ChoiceValue(ac);
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
	public Solution findDiagnosis(String name) {
		Solution diag = kbm.findDiagnosis(name);
		if (diag==null&&lazyDiags) {
			diag=createDiagnosis(name, null);
		}
		return diag;
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
					if (q.getName().equals(name)) {
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
	public Solution createDiagnosis(String id, String name, Solution parent) {
		if (parent==null) {
			parent=kbm.getKnowledgeBase().getRootDiagnosis();
		}
		return kbm.createDiagnosis(id, name, parent);
	}


	@Override
	public QContainer createQContainer(String name, QASet parent) {
		if (parent==null) {
			parent=kbm.getKnowledgeBase().getRootQASet();
		}
		return kbm.createQContainer(name, parent);
	}


	private void collectQuestions(TerminologyObject namedObject, List<Question> result) {
		if (namedObject instanceof Question && !result.contains(namedObject)) {
			result.add((Question) namedObject);
		}
		for (TerminologyObject child : namedObject.getChildren()) {
			collectQuestions(child, result);
		}
	}
}
