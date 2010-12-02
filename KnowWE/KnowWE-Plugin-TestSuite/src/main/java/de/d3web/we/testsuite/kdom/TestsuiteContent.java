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

package de.d3web.we.testsuite.kdom;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.NumValue;
import de.d3web.empiricaltesting.TestSuite;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.report.message.NoSuchObjectError;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.SolutionReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

/**
 * TestsuiteContent
 *
 * @author Sebastian Furth
 *
 */
public class TestsuiteContent extends XMLContent {

	protected TestsuiteContent() {
		this.childrenTypes.add(new SequentialTestCase());
		this.addSubtreeHandler(Priority.LOWEST, new TestSuiteSubTreeHandler());
	}

	public class TestSuiteSubTreeHandler extends D3webSubtreeHandler<TestSuiteType> {

		@Override
		public boolean isIgnoringPackageCompile() {
			return true;
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<TestSuiteType> s) {

			List<KDOMReportMessage> messages = new LinkedList<KDOMReportMessage>();
			KnowledgeBaseManagement kbm = loadKBM(article, s);

			if (kbm != null) {

				List<de.d3web.empiricaltesting.SequentialTestCase> repository = new LinkedList<de.d3web.empiricaltesting.SequentialTestCase>();

				// Get all SequentialTestCase sections
				List<Section<SequentialTestCase>> stcSections = new LinkedList<Section<SequentialTestCase>>();
				s.findSuccessorsOfType(SequentialTestCase.class, stcSections);

				// Process each SequentialTestCase section
				for (Section<SequentialTestCase> stcSection : stcSections) {

					int index = stcSections.indexOf(stcSection) + 1;

					// Create new STC
					de.d3web.empiricaltesting.SequentialTestCase stc = new de.d3web.empiricaltesting.SequentialTestCase();
					createSTCName(stcSection, index, stc, messages);
					if (messages.size() > 0) return messages;
					createRTCs(stcSection, index, stc, kbm, messages);
					if (messages.size() > 0) return messages;

					// Add STC to Repository
					repository.add(stc);
				}

				if (messages.size() == 0) {

					// Create the test suite
					TestSuite testSuite = new TestSuite();
					testSuite.setKb(kbm.getKnowledgeBase());
					testSuite.setRepository(repository);

					// Store the test suite
					KnowWEUtils.storeObject(s.getArticle().getWeb(), s.getTitle(), s.getID(),
							TestSuiteType.TESTSUITEKEY, testSuite);
					messages.add(new ObjectCreatedMessage(
							"Test Suite successfully created with "
									+ testSuite.getRepository().size() + " cases."));
				}

			}
			else {
				Section<TestSuiteType> father = s.findAncestorOfType(TestSuiteType.class);
				return Arrays.asList((KDOMReportMessage) new SimpleMessageError(
						"Unable to get knowledge base from article: "
								+ DefaultMarkupType.getAnnotation(father, TestSuiteType.KBSOURCE)));
			}

			return messages;
		}

		private void createSTCName(Section<SequentialTestCase> stcSection, int index, de.d3web.empiricaltesting.SequentialTestCase stc, List<KDOMReportMessage> messages) {
			Section<SequentialTestCaseName> stcName = stcSection.findSuccessor(SequentialTestCaseName.class);
			if (stcName == null) {
				messages.add(new SimpleMessageError("There is no name for STC" + index));
			}
			else {
				stc.setName(clean(stcName.getOriginalText().trim()));
			}
		}

		private void createRTCs(Section<SequentialTestCase> stcSection, int stcIndex, de.d3web.empiricaltesting.SequentialTestCase stc, KnowledgeBaseManagement kbm, List<KDOMReportMessage> messages) {

			// Get all RatedTestCase sections
			List<Section<RatedTestCase>> rtcSections = new LinkedList<Section<RatedTestCase>>();
			stcSection.findSuccessorsOfType(RatedTestCase.class, rtcSections);

			// Process each RatedTestCase section
			for (Section<RatedTestCase> rtcSection : rtcSections) {

				int rtcIndex = rtcSections.indexOf(rtcSection);
				de.d3web.empiricaltesting.RatedTestCase rtc = new de.d3web.empiricaltesting.RatedTestCase();
				createFindings(rtcSection, stcIndex, rtcIndex, rtc, kbm, messages);
				createRatedSolutions(rtcSection, stcIndex, rtcIndex, rtc, kbm, messages);
				stc.add(rtc);
			}

		}

		private void createFindings(Section<RatedTestCase> rtcSection, int stcIndex, int rtcIndex, de.d3web.empiricaltesting.RatedTestCase rtc, KnowledgeBaseManagement kbm, List<KDOMReportMessage> messages) {

			// Get all Finding sections
			List<Section<Finding>> findingSections = new LinkedList<Section<Finding>>();
			rtcSection.findSuccessorsOfType(Finding.class, findingSections);

			// Process each Finding section
			for (Section<Finding> findingSection : findingSections) {

				// Get the QuestionReference section
				Section<QuestionReference> questionSection = findingSection.findSuccessor(QuestionReference.class);

				// Get the real Question
				if (questionSection != null) {
					String questionText = clean(questionSection.getOriginalText());
					Question question = kbm.findQuestion(questionText);

					// Create error message if there is no question with this
					// name in the KB
					if (question == null) {
						messages.add(new NoSuchObjectError(
								clean(questionSection.getOriginalText().trim())));
					}
					else {

						// Get the value
						Section<AnswerReference> valueSection = findingSection.findSuccessor(AnswerReference.class);

						// Create error message if there is no value defined
						if (valueSection == null) {
							messages.add(new SimpleMessageError(
									"There is no Value defined for Question: "
											+ clean(questionSection.getOriginalText().trim())));
							return;
						}

						// Check if the question is a QuestionNum
						if (question instanceof QuestionNum) {

							try {
								double value = Double.parseDouble(clean(valueSection.getOriginalText().trim()));
								rtc.add(new de.d3web.empiricaltesting.Finding(question,
										new NumValue(value)));
							}
							catch (NumberFormatException e) {
								messages.add(new SimpleMessageError(
										"The value has to be a number for Question: "
												+ clean(questionSection.getOriginalText().trim())));
							}
						}
						// If not, it is a QuestionChoice
						else {
							Value value = kbm.findValue(question,
									clean(valueSection.getOriginalText().trim()));
							if (value == null) {
								messages.add(new NoSuchObjectError(
										clean(valueSection.getOriginalText().trim())));
							}
							else {
								rtc.add(new de.d3web.empiricaltesting.Finding(question, value));
							}
						}
					}
				}
				else {
					messages.add(new SimpleMessageError(
							"There is no Question defined in Finding "
									+ findingSections.indexOf(findingSection) +
									" in RTC " + rtcIndex + " in STC " + stcIndex));
				}
			}
		}

		private void createRatedSolutions(Section<RatedTestCase> rtcSection, int stcIndex, int rtcIndex, de.d3web.empiricaltesting.RatedTestCase rtc, KnowledgeBaseManagement kbm, List<KDOMReportMessage> messages) {

			// Get all RatedSolution sections
			List<Section<RatedSolution>> ratedSolutionSections = new LinkedList<Section<RatedSolution>>();
			rtcSection.findSuccessorsOfType(RatedSolution.class, ratedSolutionSections);

			// Process each RatedSolution section
			for (Section<RatedSolution> ratedSolutionSection : ratedSolutionSections) {

				Solution solution = null;

				// Get the SolutionReference section
				Section<SolutionReference> solutionSection = ratedSolutionSection.findSuccessor(SolutionReference.class);

				// Get the real Solution
				if (solutionSection != null) {
					solution = kbm.findSolution(clean(solutionSection.getOriginalText().trim()));

					// Create error message if there is no question with this
					// name in the KB
					if (solution == null) {
						messages.add(new NoSuchObjectError(
								clean(solutionSection.getOriginalText().trim())));
						return;
					}
				}
				else {
					messages.add(new SimpleMessageError(
							"There is no Solution defined in RatedSolution "
									+ ratedSolutionSections.indexOf(ratedSolutionSection) +
									" in RTC " + rtcIndex + " in STC " + stcIndex));
					return;
				}

				// Get the StateRating section
				Section<StateRating> stateSection = ratedSolutionSection.findSuccessor(StateRating.class);

				// Create a real StateRating
				if (stateSection != null) {
					if (solution != null) {
						de.d3web.empiricaltesting.StateRating rating = new de.d3web.empiricaltesting.StateRating(
								clean(stateSection.getOriginalText().trim()));

						// And finally create the RatedSolution
						rtc.addExpected(new de.d3web.empiricaltesting.RatedSolution(solution,
								rating));
					}

				}
				else {
					messages.add(new SimpleMessageError(
							"There is no Rating defined for Solution: "
									+ solution + " in RTC: " + rtcIndex + " in STC: " + stcIndex));
				}
			}

		}

		/**
		 * Due to the knowledge base is in another article we need this method.
		 */
		private KnowledgeBaseManagement loadKBM(KnowWEArticle a, Section<TestSuiteType> s) {

			KnowledgeBaseManagement kbm = getKBM(a);

			// KBM contains only the ROOT-QASET and the ROOT-Solution
			// TODO: Remove this check. ATM necessary for the Test (@see
			// MyTestArticleManager)
			if (kbm != null && kbm.getKnowledgeBase().getAllIDObjects().size() == 2) {
				Section<TestSuiteType> father = s.findAncestorOfType(TestSuiteType.class);
				String source = DefaultMarkupType.getAnnotation(father, TestSuiteType.KBSOURCE);
				KnowWEArticle article = KnowWEEnvironment.getInstance().getArticle(a.getWeb(),
						source);
				kbm = getKBM(article);
			}

			return kbm;
		}

		private String clean(String quotedString) {
			if (quotedString.startsWith("\"") && quotedString.endsWith("\"")) {
				return quotedString.substring(1, quotedString.length() - 1).trim();
			}
			return quotedString.trim();
		}

	}

}
