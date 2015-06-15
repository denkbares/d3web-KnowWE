/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.testcase.kdom;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.QuestionValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.empiricaltesting.TestCase;
import de.d3web.testcase.stc.STCWrapper;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.SolutionReference;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.Number;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.DefaultTestCaseStorage;
import de.knowwe.testcases.SingleTestCaseProvider;
import de.knowwe.testcases.TestCaseProvider;
import de.knowwe.testcases.TestCaseUtils;
import de.knowwe.core.kdom.basicType.TimeStampType;

/**
 * TestsuiteContent
 * 
 * @author Sebastian Furth
 * 
 */
public class TestCaseContent extends AbstractType {

	protected TestCaseContent() {
		this.setSectionFinder(AllTextFinder.getInstance());
		this.addChildType(new SequentialTestCase());
		this.addCompileScript(Priority.LOWEST, new TestSuiteSubTreeHandler());
	}

	public class TestSuiteSubTreeHandler implements D3webHandler<TestCaseContent> {

		@Override
		public Collection<Message> create(D3webCompiler compiler, Section<TestCaseContent> s) {

			List<Message> messages = new LinkedList<Message>();
			KnowledgeBase kb = getKnowledgeBase(compiler);
			if (kb != null) {

				List<de.d3web.empiricaltesting.SequentialTestCase> repository =
						new LinkedList<de.d3web.empiricaltesting.SequentialTestCase>();

				// Get all SequentialTestCase sections
				List<Section<SequentialTestCase>> stcSections = new LinkedList<Section<SequentialTestCase>>();
				Sections.successors(s, SequentialTestCase.class, stcSections);

				// Process each SequentialTestCase section
				for (Section<SequentialTestCase> stcSection : stcSections) {

					int index = stcSections.indexOf(stcSection) + 1;

					// Create new STC
					de.d3web.empiricaltesting.SequentialTestCase stc =
							new de.d3web.empiricaltesting.SequentialTestCase();
					createSTCName(stcSection, index, stc, messages);
					if (messages.size() > 0) return messages;
					createRTCs(stcSection, index, stc, kb, messages);
					if (messages.size() > 0) return messages;

					// Add STC to Repository
					repository.add(stc);
				}

				if (messages.size() == 0) {

					// Create the test suite
					TestCase testSuite = new TestCase();
					testSuite.setKb(kb);
					testSuite.setRepository(repository);
					List<TestCaseProvider> providers = new LinkedList<TestCaseProvider>();
					Section<DefaultMarkupType> markupSection = Sections.ancestor(s,
							DefaultMarkupType.class);
					for (de.d3web.empiricaltesting.SequentialTestCase stc : testSuite.getRepository()) {
						providers.add(new SingleTestCaseProvider(compiler, markupSection,
								new STCWrapper(stc), s.getArticle().getTitle() + "/"
										+ stc.getName()));
					}
					Section<DefaultMarkupType> defaultMarkupSection = Sections.ancestor(
							s, DefaultMarkupType.class);
					TestCaseUtils.storeTestCaseProviderStorage(compiler, defaultMarkupSection,
							new DefaultTestCaseStorage(providers));
					// Store the test suite
					s.storeObject(TestCaseType.TESTCASEKEY, testSuite);
				}

			}
			else {
				return Messages.asList(Messages.error(
						"Unable to get knowledge base"));
			}

			return messages;
		}

		private void createSTCName(Section<SequentialTestCase> stcSection, int index, de.d3web.empiricaltesting.SequentialTestCase stc, List<Message> messages) {
			Section<SequentialTestCaseName> stcName = Sections.successor(stcSection,
					SequentialTestCaseName.class);
			if (stcName == null) {
				messages.add(Messages.error("There is no name for STC" + index));
			}
			else {
				stc.setName(clean(stcName.getText().trim()));
			}
		}

		private void createRTCs(Section<SequentialTestCase> stcSection, int stcIndex, de.d3web.empiricaltesting.SequentialTestCase stc, KnowledgeBase kb, List<Message> messages) {

			// Get all RatedTestCase sections
			List<Section<RatedTestCase>> rtcSections = new LinkedList<Section<RatedTestCase>>();
			Sections.successors(stcSection, RatedTestCase.class, rtcSections);

			// Process each RatedTestCase section
			for (Section<RatedTestCase> rtcSection : rtcSections) {

				int rtcIndex = rtcSections.indexOf(rtcSection);
				de.d3web.empiricaltesting.RatedTestCase rtc = new de.d3web.empiricaltesting.RatedTestCase();
				setTimeStamp(rtcSection, rtc);
				createFindings(rtcSection, stcIndex, rtcIndex, rtc, kb, messages);
				createRatedFindings(rtcSection, stcIndex, rtcIndex, rtc, kb, messages);
				createRatedSolutions(rtcSection, stcIndex, rtcIndex, rtc, kb, messages);
				stc.add(rtc);
			}

		}

		/**
		 * 
		 * @created 19.07.2011
		 * @param rtcSection
		 * @param stcIndex
		 * @param rtcIndex
		 * @param rtc
		 * @param kb
		 * @param messages
		 */
		private void createRatedFindings(Section<RatedTestCase> rtcSection, int stcIndex, int rtcIndex, de.d3web.empiricaltesting.RatedTestCase rtc, KnowledgeBase kb, List<Message> messages) {

			// Get all Finding sections
			List<Section<RatedFinding>> findingSections = new LinkedList<Section<RatedFinding>>();
			Sections.successors(rtcSection, RatedFinding.class, findingSections);

			// Process each Finding section
			for (Section<RatedFinding> findingSection : findingSections) {

				// Get the QuestionReference section
				Section<QuestionReference> questionSection = Sections.successor(findingSection,
						QuestionReference.class);

				// Get the real Question
				if (questionSection != null) {
					String questionText = clean(questionSection.getText());
					Question question = kb.getManager().searchQuestion(
							questionText);

					// Create error message if there is no question with this
					// name in the KB
					if (question == null) {
						messages.add(Messages.noSuchObjectError(
								clean(questionSection.getText().trim())));
					}
					else {

						// Check if the question is a QuestionNum
						if (question instanceof QuestionNum) {

							Section<Number> valueSection = Sections.successor(
									findingSection, Number.class);

							if (valueSection == null) {
								messages.add(Messages.error(
										"The value has to be a number for Question: "
												+ clean(questionSection.getText().trim())));
								continue;
							}

							try {

								double value = Double.parseDouble(clean(valueSection.getText().trim()));
								rtc.addExpectedFinding(new de.d3web.empiricaltesting.Finding(
										question,
										new NumValue(value)));
							}
							catch (NumberFormatException e) {
								messages.add(Messages.error(
										"The value has to be a number for Question: "
												+ clean(questionSection.getText().trim())));
							}
						}
						// If not, it is a QuestionChoice
						else {

							// Get the value
							Section<AnswerReference> valueSection = Sections.successor(
									findingSection, AnswerReference.class);

							// Create error message if there is no value defined
							if (valueSection == null) {
								messages.add(Messages.error(
										"There is no Value defined for Question: "
												+ clean(questionSection.getText().trim())));
								return;
							}

							QuestionValue value = KnowledgeBaseUtils.findValue(question,
									clean(valueSection.getText().trim()));
							if (value == null) {
								messages.add(Messages.noSuchObjectError(
										clean(valueSection.getText().trim())));
							}
							else {
								rtc.addExpectedFinding(new de.d3web.empiricaltesting.Finding(
										question, value));
							}
						}
					}
				}
				else {
					messages.add(Messages.error(
							"There is no Question defined in expected Finding "
									+ findingSections.indexOf(findingSection) +
									" in RTC " + rtcIndex + " in STC " + stcIndex));
				}
			}

		}

		/**
		 * Sets the time of the RTC if a timestamp is supplied in the section.
		 * 
		 * @created 18.07.2011
		 * @param rtcSection
		 * @param rtc
		 */
		private void setTimeStamp(Section<RatedTestCase> rtcSection, de.d3web.empiricaltesting.RatedTestCase rtc) {

			Section<TimeStampType> timestamp = Sections.successor(rtcSection,
					TimeStampType.class);

			if (timestamp == null) return;
			try {
				long timeInMillis = TimeStampType.getTimeInMillis(timestamp);
				rtc.setTimeStamp(new Date(timeInMillis));
			}
			catch (NumberFormatException e) {
				// nothing to do here, not timestamp is ok
			}

		}

		private void createFindings(Section<RatedTestCase> rtcSection, int stcIndex, int rtcIndex, de.d3web.empiricaltesting.RatedTestCase rtc, KnowledgeBase kb, List<Message> messages) {

			// Get all Finding sections
			List<Section<Finding>> findingSections = new LinkedList<Section<Finding>>();
			Sections.successors(rtcSection, Finding.class, findingSections);

			// Process each Finding section
			for (Section<Finding> findingSection : findingSections) {

				// Get the QuestionReference section
				Section<QuestionReference> questionSection = Sections.successor(findingSection,
						QuestionReference.class);

				// Get the real Question
				if (questionSection != null) {
					String questionText = clean(questionSection.getText());
					Question question = kb.getManager().searchQuestion(
							questionText);

					// Create error message if there is no question with this
					// name in the KB
					if (question == null) {
						messages.add(Messages.noSuchObjectError(
								clean(questionSection.getText().trim())));
					}
					else {

						// Get the value
						Section<AnswerReference> valueSection = Sections.successor(
								findingSection, AnswerReference.class);

						// Create error message if there is no value defined
						if (valueSection == null) {
							messages.add(Messages.error(
									"There is no Value defined for Question: "
											+ clean(questionSection.getText().trim())));
							return;
						}

						// Check if the question is a QuestionNum
						if (question instanceof QuestionNum) {

							try {
								double value = Double.parseDouble(clean(valueSection.getText().trim()));
								rtc.add(new de.d3web.empiricaltesting.Finding(question,
										new NumValue(value)));
							}
							catch (NumberFormatException e) {
								messages.add(Messages.error(
										"The value has to be a number for Question: "
												+ clean(questionSection.getText().trim())));
							}
						}
						// If not, it is a QuestionChoice
						else {
							QuestionValue value = KnowledgeBaseUtils.findValue(question,
									clean(valueSection.getText().trim()));
							if (value == null) {
								messages.add(Messages.noSuchObjectError(
										clean(valueSection.getText().trim())));
							}
							else {
								rtc.add(new de.d3web.empiricaltesting.Finding(question, value));
							}
						}
					}
				}
				else {
					messages.add(Messages.error(
							"There is no Question defined in Finding "
									+ findingSections.indexOf(findingSection) +
									" in RTC " + rtcIndex + " in STC " + stcIndex));
				}
			}
		}

		private void createRatedSolutions(Section<RatedTestCase> rtcSection, int stcIndex, int rtcIndex, de.d3web.empiricaltesting.RatedTestCase rtc, KnowledgeBase kb, List<Message> messages) {

			// Get all RatedSolution sections
			List<Section<RatedSolution>> ratedSolutionSections = new LinkedList<Section<RatedSolution>>();
			Sections.successors(rtcSection, RatedSolution.class, ratedSolutionSections);

			// Process each RatedSolution section
			for (Section<RatedSolution> ratedSolutionSection : ratedSolutionSections) {

				Solution solution = null;

				// Get the SolutionReference section
				Section<SolutionReference> solutionSection = Sections.successor(
						ratedSolutionSection, SolutionReference.class);

				// Get the real Solution
				if (solutionSection != null) {
					solution = kb.getManager().searchSolution(
							clean(solutionSection.getText().trim()));

					// Create error message if there is no question with this
					// name in the KB
					if (solution == null) {
						messages.add(Messages.noSuchObjectError(
								clean(solutionSection.getText().trim())));
						return;
					}
				}
				else {
					messages.add(Messages.error(
							"There is no Solution defined in RatedSolution "
									+ ratedSolutionSections.indexOf(ratedSolutionSection) +
									" in RTC " + rtcIndex + " in STC " + stcIndex));
					return;
				}

				// Get the StateRating section
				Section<StateRating> stateSection = Sections.successor(ratedSolutionSection,
						StateRating.class);

				// Create a real StateRating
				if (stateSection != null) {
					if (solution != null) {
						de.d3web.empiricaltesting.StateRating rating = new de.d3web.empiricaltesting.StateRating(
								clean(stateSection.getText().trim()));

						// And finally create the RatedSolution
						rtc.addExpected(new de.d3web.empiricaltesting.RatedSolution(solution,
								rating));
					}

				}
				else {
					messages.add(Messages.error(
							"There is no Rating defined for Solution: "
									+ solution + " in RTC: " + rtcIndex + " in STC: " + stcIndex));
				}
			}

		}

		private String clean(String quotedString) {
			if (quotedString.startsWith("\"") && quotedString.endsWith("\"")) {
				return quotedString.substring(1, quotedString.length() - 1).trim();
			}
			return quotedString.trim();
		}

	}

}
