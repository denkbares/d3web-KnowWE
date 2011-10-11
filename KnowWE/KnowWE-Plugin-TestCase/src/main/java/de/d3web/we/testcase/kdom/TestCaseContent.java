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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.NumValue;
import de.d3web.empiricaltesting.TestCase;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.SolutionReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.basicType.Number;
import de.knowwe.core.kdom.objects.KnowWETerm;
import de.knowwe.core.kdom.objects.StringReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.report.KDOMReportMessage;
import de.knowwe.core.report.SimpleMessageError;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.report.message.NoSuchObjectError;
import de.knowwe.report.message.ObjectCreatedMessage;

/**
 * TestsuiteContent
 * 
 * @author Sebastian Furth
 * 
 */
public class TestCaseContent extends StringReference {

	protected TestCaseContent() {
		this.setTermScope(Scope.GLOBAL);
		this.setIgnorePackageCompile(true);
		this.sectionFinder = new AllTextSectionFinder();
		this.childrenTypes.add(new SequentialTestCase());
		this.addSubtreeHandler(Priority.LOWEST, new TestSuiteSubTreeHandler());
	}

	@Override
	public String getTermIdentifier(Section<? extends KnowWETerm<String>> s) {
		return DefaultMarkupType.getAnnotation(Sections.findAncestorOfType(s, TestCaseType.class),
				TestCaseType.ANNOTATION_MASTER);
	}

	@Override
	public String getTermObjectDisplayName() {
		return "Master";
	}

	public class TestSuiteSubTreeHandler extends D3webSubtreeHandler<TestCaseType> {

		@Override
		public boolean isIgnoringPackageCompile() {
			return true;
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<TestCaseType> s) {

			List<KDOMReportMessage> messages = new LinkedList<KDOMReportMessage>();
			KnowledgeBase kb = loadKB(article, s);

			if (kb != null) {

				List<de.d3web.empiricaltesting.SequentialTestCase> repository = new LinkedList<de.d3web.empiricaltesting.SequentialTestCase>();

				// Get all SequentialTestCase sections
				List<Section<SequentialTestCase>> stcSections = new LinkedList<Section<SequentialTestCase>>();
				Sections.findSuccessorsOfType(s, SequentialTestCase.class, stcSections);

				// Process each SequentialTestCase section
				for (Section<SequentialTestCase> stcSection : stcSections) {

					int index = stcSections.indexOf(stcSection) + 1;

					// Create new STC
					de.d3web.empiricaltesting.SequentialTestCase stc = new de.d3web.empiricaltesting.SequentialTestCase();
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

					// Store the test suite
					KnowWEUtils.storeObject(article, s,
							TestCaseType.TESTCASEKEY, testSuite);
					messages.add(new ObjectCreatedMessage(
							"Test Suite successfully created with "
									+ testSuite.getRepository().size() + " cases."));
				}

			}
			else {
				Section<TestCaseType> father = Sections.findAncestorOfType(s, TestCaseType.class);
				return Arrays.asList((KDOMReportMessage) new SimpleMessageError(
						"Unable to get knowledge base from article: "
								+ DefaultMarkupType.getAnnotation(father,
										TestCaseType.ANNOTATION_MASTER)));
			}

			return messages;
		}

		private void createSTCName(Section<SequentialTestCase> stcSection, int index, de.d3web.empiricaltesting.SequentialTestCase stc, List<KDOMReportMessage> messages) {
			Section<SequentialTestCaseName> stcName = Sections.findSuccessor(stcSection,
					SequentialTestCaseName.class);
			if (stcName == null) {
				messages.add(new SimpleMessageError("There is no name for STC" + index));
			}
			else {
				stc.setName(clean(stcName.getOriginalText().trim()));
			}
		}

		private void createRTCs(Section<SequentialTestCase> stcSection, int stcIndex, de.d3web.empiricaltesting.SequentialTestCase stc, KnowledgeBase kb, List<KDOMReportMessage> messages) {

			// Get all RatedTestCase sections
			List<Section<RatedTestCase>> rtcSections = new LinkedList<Section<RatedTestCase>>();
			Sections.findSuccessorsOfType(stcSection, RatedTestCase.class, rtcSections);

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
		private void createRatedFindings(Section<RatedTestCase> rtcSection, int stcIndex, int rtcIndex, de.d3web.empiricaltesting.RatedTestCase rtc, KnowledgeBase kb, List<KDOMReportMessage> messages) {

			// Get all Finding sections
			List<Section<RatedFinding>> findingSections = new LinkedList<Section<RatedFinding>>();
			Sections.findSuccessorsOfType(rtcSection, RatedFinding.class, findingSections);

			// Process each Finding section
			for (Section<RatedFinding> findingSection : findingSections) {

				// Get the QuestionReference section
				Section<QuestionReference> questionSection = Sections.findSuccessor(findingSection,
						QuestionReference.class);

				// Get the real Question
				if (questionSection != null) {
					String questionText = clean(questionSection.getOriginalText());
					Question question = kb.getManager().searchQuestion(
							questionText);

					// Create error message if there is no question with this
					// name in the KB
					if (question == null) {
						messages.add(new NoSuchObjectError(
								clean(questionSection.getOriginalText().trim())));
					}
					else {


						// Check if the question is a QuestionNum
						if (question instanceof QuestionNum) {

							Section<Number> valueSection = Sections.findSuccessor(
									findingSection, Number.class);

							if (valueSection == null) {
								messages.add(new SimpleMessageError(
										"The value has to be a number for Question: "
												+ clean(questionSection.getOriginalText().trim())));
								continue;
							}

							try {

								double value = Double.parseDouble(clean(valueSection.getOriginalText().trim()));
								rtc.addExpectedFinding(new de.d3web.empiricaltesting.Finding(
										question,
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

							// Get the value
							Section<AnswerReference> valueSection = Sections.findSuccessor(
									findingSection, AnswerReference.class);

							// Create error message if there is no value defined
							if (valueSection == null) {
								messages.add(new SimpleMessageError(
										"There is no Value defined for Question: "
												+ clean(questionSection.getOriginalText().trim())));
								return;
							}

							Value value = KnowledgeBaseUtils.findValue(question,
									clean(valueSection.getOriginalText().trim()));
							if (value == null) {
								messages.add(new NoSuchObjectError(
										clean(valueSection.getOriginalText().trim())));
							}
							else {
								rtc.addExpectedFinding(new de.d3web.empiricaltesting.Finding(
										question, value));
							}
						}
					}
				}
				else {
					messages.add(new SimpleMessageError(
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

			Section<TimeStampType> timestamp = Sections.findSuccessor(rtcSection,
					TimeStampType.class);

			if (timestamp == null) return;

			long timeInMillis = TimeStampType.getTimeInMillis(timestamp);

			rtc.setTimeStamp(new Date(timeInMillis));

		}

		private void createFindings(Section<RatedTestCase> rtcSection, int stcIndex, int rtcIndex, de.d3web.empiricaltesting.RatedTestCase rtc, KnowledgeBase kb, List<KDOMReportMessage> messages) {

			// Get all Finding sections
			List<Section<Finding>> findingSections = new LinkedList<Section<Finding>>();
			Sections.findSuccessorsOfType(rtcSection, Finding.class, findingSections);

			// Process each Finding section
			for (Section<Finding> findingSection : findingSections) {

				// Get the QuestionReference section
				Section<QuestionReference> questionSection = Sections.findSuccessor(findingSection,
						QuestionReference.class);

				// Get the real Question
				if (questionSection != null) {
					String questionText = clean(questionSection.getOriginalText());
					Question question = kb.getManager().searchQuestion(
							questionText);

					// Create error message if there is no question with this
					// name in the KB
					if (question == null) {
						messages.add(new NoSuchObjectError(
								clean(questionSection.getOriginalText().trim())));
					}
					else {

						// Get the value
						Section<AnswerReference> valueSection = Sections.findSuccessor(
								findingSection, AnswerReference.class);

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
							Value value = KnowledgeBaseUtils.findValue(question,
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

		private void createRatedSolutions(Section<RatedTestCase> rtcSection, int stcIndex, int rtcIndex, de.d3web.empiricaltesting.RatedTestCase rtc, KnowledgeBase kb, List<KDOMReportMessage> messages) {

			// Get all RatedSolution sections
			List<Section<RatedSolution>> ratedSolutionSections = new LinkedList<Section<RatedSolution>>();
			Sections.findSuccessorsOfType(rtcSection, RatedSolution.class, ratedSolutionSections);

			// Process each RatedSolution section
			for (Section<RatedSolution> ratedSolutionSection : ratedSolutionSections) {

				Solution solution = null;

				// Get the SolutionReference section
				Section<SolutionReference> solutionSection = Sections.findSuccessor(
						ratedSolutionSection, SolutionReference.class);

				// Get the real Solution
				if (solutionSection != null) {
					solution = kb.getManager().searchSolution(
							clean(solutionSection.getOriginalText().trim()));

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
				Section<StateRating> stateSection = Sections.findSuccessor(ratedSolutionSection,
						StateRating.class);

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
		private KnowledgeBase loadKB(KnowWEArticle a, Section<TestCaseType> s) {

			KnowledgeBase kb = getKB(a);

			// KBM contains only the ROOT-QASET and the ROOT-Solution
			// TODO: Remove this check. ATM necessary for the Test (@see
			// MyTestArticleManager)
			if (kb != null
					&& kb.getManager().getAllTerminologyObjects().size() == 2) {
				Section<TestCaseType> father = Sections.findAncestorOfType(s, TestCaseType.class);
				String source = DefaultMarkupType.getAnnotation(father,
						TestCaseType.ANNOTATION_MASTER);
				KnowWEArticle article = KnowWEEnvironment.getInstance().getArticle(a.getWeb(),
						source);
				kb = getKB(article);
			}

			return kb;
		}

		private String clean(String quotedString) {
			if (quotedString.startsWith("\"") && quotedString.endsWith("\"")) {
				return quotedString.substring(1, quotedString.length() - 1).trim();
			}
			return quotedString.trim();
		}

	}

}
