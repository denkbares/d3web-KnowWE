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
import de.d3web.core.KnowledgeBase;
import de.d3web.core.session.values.AnswerChoice;
import de.d3web.core.terminology.Answer;
import de.d3web.core.terminology.Diagnosis;
import de.d3web.core.terminology.IDObject;
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


/**
 * Interface to control where to search for and create IDObjects
 * 
 * @author Markus Friedrich (denkbares GmbH)
 *
 */
public interface IDObjectManagement {
	
	QContainer findQContainer(String name);
	
	Question findQuestion(String name);
	
	Diagnosis findDiagnosis(String name);
	
	AnswerChoice findAnswerChoice(QuestionChoice qc, String name);
	
	Answer findAnswer(Question q, String name);
	
	QContainer createQContainer(String name, QASet parent);
	
	QuestionOC createQuestionOC(String name, QASet parent, AnswerChoice[] answers);
	QuestionZC createQuestionZC(String name, QASet parent);
	QuestionOC createQuestionOC(String name, QASet parent, String[] answers);
	QuestionMC createQuestionMC(String name, QASet parent, AnswerChoice[] answers);
	QuestionMC createQuestionMC(String name, QASet parent, String[] answers);
	QuestionNum createQuestionNum(String name, QASet parent);
	QuestionYN createQuestionYN(String name, QASet parent);
	QuestionSolution createQuestionState(String name, QASet parent);
	QuestionYN createQuestionYN(String name, String yesAlternativeText, String noAlternativeText, QASet parent);
	QuestionDate createQuestionDate(String name, QASet parent);
	QuestionText createQuestionText(String name, QASet parent);
	
	Diagnosis createDiagnosis(String name, Diagnosis parent);

	Answer addChoiceAnswer(QuestionChoice qc, String value);
	KnowledgeBase getKnowledgeBase();

	String findNewIDForAnswerChoice(QuestionChoice currentQuestion);

	String findNewIDFor(Class<? extends IDObject> object);

	boolean changeID(IDObject object, String ref);
	
}
