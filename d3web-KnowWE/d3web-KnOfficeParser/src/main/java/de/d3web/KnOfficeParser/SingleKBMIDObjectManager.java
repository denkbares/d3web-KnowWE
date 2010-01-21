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

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionDate;
import de.d3web.kernel.domainModel.qasets.QuestionMC;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionOC;
import de.d3web.kernel.domainModel.qasets.QuestionSolution;
import de.d3web.kernel.domainModel.qasets.QuestionText;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.kernel.domainModel.qasets.QuestionZC;

/**
 * default implementation for the IDObjectManagement, searches and creates objects in a single kbm
 * 
 * @author Markus Friedrich (denkbares GmbH)
 */
public class SingleKBMIDObjectManager implements IDObjectManagement {

	private KnowledgeBaseManagement kbm;
	
	
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

	/* (non-Javadoc)
	 * @see de.d3web.KnOfficeParser.IDObjectManagement#createDiagnosis(java.lang.String)
	 */
	@Override
	public Diagnosis createDiagnosis(String name, Diagnosis parent) {
		return kbm.createDiagnosis(name, parent);
	}

	/* (non-Javadoc)
	 * @see de.d3web.KnOfficeParser.IDObjectManagement#createQContainer(java.lang.String)
	 */
	@Override
	public QContainer createQContainer(String name, QASet parent) {
		return kbm.createQContainer(name, parent);
	}

	/* (non-Javadoc)
	 * @see de.d3web.KnOfficeParser.IDObjectManagement#createQuestion(java.lang.String)
	 */
	@Override
	public QuestionDate createQuestionDate(String name, QASet parent) {
		return kbm.createQuestionDate(name, parent);
	}

	/* (non-Javadoc)
	 * @see de.d3web.KnOfficeParser.IDObjectManagement#findAnswer(de.d3web.kernel.domainModel.qasets.Question, java.lang.String)
	 */
	@Override
	public Answer findAnswer(Question q, String name) {
		return kbm.findAnswer(q, name);
	}

	/* (non-Javadoc)
	 * @see de.d3web.KnOfficeParser.IDObjectManagement#findAnswerChoice(de.d3web.kernel.domainModel.qasets.QuestionChoice, java.lang.String)
	 */
	@Override
	public AnswerChoice findAnswerChoice(QuestionChoice qc, String name) {
		return kbm.findAnswerChoice(qc, name);
	}

	/* (non-Javadoc)
	 * @see de.d3web.KnOfficeParser.IDObjectManagement#findDiagnosis(java.lang.String)
	 */
	@Override
	public Diagnosis findDiagnosis(String name) {
		return kbm.findDiagnosis(name);
	}

	/* (non-Javadoc)
	 * @see de.d3web.KnOfficeParser.IDObjectManagement#findQContainer(java.lang.String)
	 */
	@Override
	public QContainer findQContainer(String name) {
		return kbm.findQContainer(name);
	}

	/* (non-Javadoc)
	 * @see de.d3web.KnOfficeParser.IDObjectManagement#findQuestion(java.lang.String)
	 */
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
	public KnowledgeBase getKnowledgeBase() {
		return kbm.getKnowledgeBase();
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

}
