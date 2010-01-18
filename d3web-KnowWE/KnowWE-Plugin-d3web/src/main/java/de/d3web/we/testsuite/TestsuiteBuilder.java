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

package de.d3web.we.testsuite;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.tools.ant.util.ReaderInputStream;

import de.d3web.KnOfficeParser.IDObjectManagement;
import de.d3web.KnOfficeParser.KnOfficeParameterSet;
import de.d3web.KnOfficeParser.KnOfficeParser;
import de.d3web.KnOfficeParser.util.DefaultD3webLexerErrorHandler;
import de.d3web.KnOfficeParser.util.DefaultD3webParserErrorHandler;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.empiricalTesting.Finding;
import de.d3web.empiricalTesting.RatedSolution;
import de.d3web.empiricalTesting.RatedTestCase;
import de.d3web.empiricalTesting.Rating;
import de.d3web.empiricalTesting.StateRating;
import de.d3web.empiricalTesting.SequentialTestCase;
import de.d3web.empiricalTesting.TestSuite;
import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.answers.AnswerNum;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.report.Message;

/**
 * TestsuiteBuilder
 * This class creates a testsuite containing several SequentialTestCases.
 * Testsuites are parsed by TestsuiteANTLR
 * @author Sebastian Furth
 * @see TestsuiteANTLR.g
 *
 */
public class TestsuiteBuilder implements KnOfficeParser {
	
	private IDObjectManagement idom;
	private String file;
	private List<Message> errors = new ArrayList<Message>();
	private List<SequentialTestCase> sequentialTestCases = new ArrayList<SequentialTestCase>();
	private SequentialTestCase currentSequentialTestCase;
	private RatedTestCase currentRatedTestCase;	
	private TestSuite testSuite;
	
	public TestsuiteBuilder(String file, IDObjectManagement idom) {
		this.idom = idom;
		this.file = file;
	}
	
	@Override
	public List<Message> addKnowledge(Reader r,
			IDObjectManagement idom, KnOfficeParameterSet s) {
		
		// Initialization
		this.idom = idom;
		ReaderInputStream input = new ReaderInputStream(r);
		ANTLRInputStream istream = null;
		
		try {
			istream = new ANTLRInputStream(input);
		} catch (IOException e1) {
			errors.add(MessageKnOfficeGenerator.createAntlrInputError(file, 0, ""));
		}
		
		TestsuiteLexer lexer = new TestsuiteLexer(istream, new DefaultD3webLexerErrorHandler(errors, file));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		TestsuiteANTLR parser = 
			new TestsuiteANTLR(tokens, this, new DefaultD3webParserErrorHandler(errors, file, "BasicLexer"));
		
		// Parsing
		try {
			parser.knowledge();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}
		
		// Reporting
		return getErrors();
	}
	
	/**
	 * Initializes a new SequentialTestCase
	 * @param name of the new SequentialTestCase
	 */
	public void addSequentialTestCase(String name) {
		currentSequentialTestCase = new SequentialTestCase();
		currentSequentialTestCase.setName(name);
	}
	
	/**
	 * Initializes a new RatedTestCase
	 * @param i number of the RatedTestCase in current SQTestCase
	 * @param line 
	 * @param linetext
	 */
	public void addRatedTestCase(int i, int line, String linetext) {
		currentRatedTestCase = new RatedTestCase();
	}
	
	/**
	 * Adds a Finding to the current RatedTestCase
	 * @param question name of the question
	 * @parm answer name of the answer
	 * @param line linenumber
	 * @param linetext
	 */
	public void addFinding(String question, String answer, int line, String linetext) {

		Question q = idom.findQuestion(question);
		
		if (q == null) { 
			errors.add(MessageKnOfficeGenerator
					.createQuestionNotFoundException("", line, linetext, question));
		} else if (q instanceof QuestionNum) {
			try {
				double value = Double.parseDouble(answer);
				AnswerNum a = new AnswerNum();
				a.setValue(value);
				a.setQuestion(q);
				Finding currentFinding = new Finding(q, a);
				currentRatedTestCase.add(currentFinding);
				
			} catch (NumberFormatException e) {
				errors.add(MessageKnOfficeGenerator
						.createNaNException("", line, linetext, answer));
			}
		} else {
			Answer a = idom.findAnswer(q, answer);
			if (a == null) { 
				errors.add(MessageKnOfficeGenerator
						.createAnswerNotFoundException("", line, linetext, answer, question));
			} else { 
				Finding currentFinding = new Finding(q, a);
				currentRatedTestCase.add(currentFinding);
			}
		}
	}
	
	/**
	 * Adds a new Solution to the current RatedTestCase
	 * @param solution name of the solution
	 * @parm rating rating of the solution
	 * @param line linenumber
	 * @param linetext 
	 */
	public void addSolution(String name, String rating, int line, String linetext) {

		Diagnosis d = idom.findDiagnosis(name);

		if (d == null) {
			errors.add(MessageKnOfficeGenerator
					.createDiagnosisNotFoundException("", line, linetext, name));
		} else {
			StateRating r = new StateRating(rating);
			addRatedSolution(d, r);
			
		}
	
	}
	
	public void addHeuristicSolution(String name, String rating, int line, String linetext) {
		
		Diagnosis d = idom.findDiagnosis(name);
		
		if (d == null) {
			errors.add(MessageKnOfficeGenerator
					.createDiagnosisNotFoundException("", line, linetext, name));
		} else {
			StateRating r = new StateRating(rating);
			addRatedSolution(d, r);
		}
	}

	public void addXCLSolution(String name, String rating, int line, String linetext) {
		
		Diagnosis d = idom.findDiagnosis(name);
		
		if (d == null) {
			errors.add(MessageKnOfficeGenerator
					.createDiagnosisNotFoundException("", line, linetext, name));
		} else {
			StateRating r = new StateRating(rating);
			addRatedSolution(d, r);
		}
	}
	
	/**
	 * Adds a RatedSolution to the current RatedTestCase
	 * @param d Diagnosis
	 * @param r Rating
	 */
	private void addRatedSolution(Diagnosis d, Rating r) {
		RatedSolution rs = new RatedSolution(d, r);
		currentRatedTestCase.addExpected(rs);
	}
	
	/**
	 * Adds the current RatedTestCase to the current SequentialTestCase
	 */
	public void finishCurrentRatedTestCase() {
		if (currentRatedTestCase.getFindings().size() > 0) {
			String name;
			RatedTestCase equalRTC = searchForEqualPathRTC();
			if (equalRTC != null) {
				name = equalRTC.getName();
			} else {
				name = "RTC_" + System.currentTimeMillis();
			}
			currentRatedTestCase.setName(name);
			currentSequentialTestCase.add(currentRatedTestCase);
			
		}
	}
	
	private RatedTestCase searchForEqualPathRTC() {
		
		if (sequentialTestCases.size() == 0)
			return null;

		boolean equality;
		int pathLength = currentSequentialTestCase.getCases().size();
		RatedTestCase lastCase;
		
		for (SequentialTestCase stc : sequentialTestCases) {
			
			equality = true;
			
			// If the path is shorter, the cases can't be equal
			if (stc.getCases().size() < pathLength + 1)
				continue;
			
			// Compares the already added RatedTestCases
			for (int i = 0; i < pathLength; i++) {
				if (!equalCases(stc.getCases().get(i), currentSequentialTestCase.getCases().get(i))) {
					equality = false;
					break;
				}
			}
			
			// Compares the last RTC of the STC with the current RTC
			lastCase = stc.getCases().get(pathLength);
			if (equality && equalCases(lastCase, currentRatedTestCase))
				return lastCase;
		}
		
		// Return null if there is no equal RTC
		return null;
	}

	private boolean equalCases(RatedTestCase rtc1, RatedTestCase rtc2) {

		if (!rtc1.getExpectedSolutions().equals(rtc2.getExpectedSolutions()))
			return false;
		if (!rtc1.getFindings().equals(rtc2.getFindings()))
			return false;

		return true;
	}

	/**
	 * Adds the current SequentialTestCase to the list of SequentialTestCases
	 */
	public void finishCurrentSequentialTestCase() {
		if (currentSequentialTestCase.getCases().size() > 0) {
			sequentialTestCases.add(currentSequentialTestCase);
		}
	}
	
	/**
	 * Creates a new Testsuite containing the list of sequentialTestCases
	 */
	public void createTestSuite() {
		if (sequentialTestCases.size() > 0) {
			testSuite = new TestSuite();
			KnowledgeBase kb = idom.getKnowledgeBase();
			testSuite.setKb(kb);
			testSuite.setRepository(sequentialTestCases);
		}	
	}
	
	@Override
	public List<Message> checkKnowledge() {
		return getErrors();
	}
	
	private List<Message> getErrors() {
		List<Message> ret = new ArrayList<Message>(errors);
				
		if (ret.size() == 0) {
			ret.add(MessageKnOfficeGenerator.createTestsuiteParsedNote(file, 0, "", sequentialTestCases.size()));
		}
		
		return ret;
	}
	
	/**
	 * Returns the created TestSuite.
	 * @return testSuite
	 */
	public TestSuite getTestsuite() {
		return testSuite;
	}

}