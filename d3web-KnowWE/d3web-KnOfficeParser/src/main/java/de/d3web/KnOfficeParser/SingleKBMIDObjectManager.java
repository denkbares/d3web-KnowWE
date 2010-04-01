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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.QuestionZC;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.values.AnswerChoice;

/**
 * Default implementation for the IDObjectManagement, searches and creates objects in a single kbm
 * 
 * @author Markus Friedrich (denkbares GmbH)
 */
public class SingleKBMIDObjectManager implements IDObjectManagement {

	protected KnowledgeBaseManagement kbm;
	
	public SingleKBMIDObjectManager(KnowledgeBaseManagement kbm) {
		super();
		this.kbm = kbm;
	}

	public KnowledgeBaseManagement getKbm() {
		return kbm;
	}

	public void setKbm(KnowledgeBaseManagement kbm) {
		this.kbm = kbm;
	}

	@Override
	public Solution createDiagnosis(String name, Solution parent) {
		return kbm.createDiagnosis(name, parent);
	}

	@Override
	public QContainer createQContainer(String name, QASet parent) {
		return kbm.createQContainer(name, parent);
	}

	@Override
	public QuestionDate createQuestionDate(String name, QASet parent) {
		return kbm.createQuestionDate(name, parent);
	}

	@Override
	public Answer findAnswer(Question q, String name) {
		return kbm.findAnswer(q, name);
	}

	@Override
	public AnswerChoice findAnswerChoice(QuestionChoice qc, String name) {
		return kbm.findAnswerChoice(qc, name);
	}

	@Override
	public Solution findDiagnosis(String name) {
		return kbm.findDiagnosis(name);
	}

	@Override
	public QContainer findQContainer(String name) {
		return kbm.findQContainer(name);
	}

	@Override
	public Question findQuestion(String name) {
		return kbm.findQuestion(name);
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
	public KnowledgeBase getKnowledgeBase() {
		return kbm.getKnowledgeBase();
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
	public QContainer createQContainer(String id, String name, QASet parent) {
		return kbm.createQContainer(id, name, parent);
	}


	@Override
	public QuestionDate createQuestionDate(String id, String name, QASet parent) {
		return kbm.createQuestionDate(id, name, parent);
	}


	@Override
	public QuestionMC createQuestionMC(String id, String name, QASet parent, AnswerChoice[] answers) {
		return kbm.createQuestionMC(id, name, parent, answers);
	}


	@Override
	public QuestionMC createQuestionMC(String id, String name, QASet parent, String[] answers) {
		return kbm.createQuestionMC(id, name, parent, answers);
	}


	@Override
	public QuestionNum createQuestionNum(String id, String name, QASet parent) {
		return kbm.createQuestionNum(id, name, parent);
	}


	@Override
	public QuestionOC createQuestionOC(String id, String name, QASet parent, AnswerChoice[] answers) {
		return kbm.createQuestionOC(id, name, parent, answers);
	}


	@Override
	public QuestionOC createQuestionOC(String id, String name, QASet parent, String[] answers) {
		return kbm.createQuestionOC(name, parent, answers);
	}


	@Override
	public QuestionText createQuestionText(String id, String name, QASet parent) {
		return kbm.createQuestionText(id, name, parent);
	}


	@Override
	public QuestionYN createQuestionYN(String id, String name, QASet parent) {
		return kbm.createQuestionYN(id, name, parent);
	}


	@Override
	public QuestionYN createQuestionYN(String id, String name, String yesAlternativeText, String noAlternativeText, QASet parent) {
		return kbm.createQuestionYN(id, name, yesAlternativeText, noAlternativeText, parent);
	}


	@Override
	public QuestionZC createQuestionZC(String id, String name, QASet parent) {
		return kbm.createQuestionZC(id, name, parent);
	}

	@Override
	public Solution createDiagnosis(String id, String name, Solution parent) {
		return kbm.createDiagnosis(id, name, parent);
	}

	@Override
	public String createRuleID() {
		return kbm.createRuleID();
	}
	
}
