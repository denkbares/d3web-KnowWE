/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.testcases;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.CondDate;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondNum;
import de.d3web.core.inference.condition.CondQuestion;
import de.d3web.core.inference.condition.CondUnknown;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.QuestionValue;
import de.d3web.core.session.values.NumValue;
import de.d3web.core.session.values.Unknown;
import de.d3web.core.utilities.Triple;
import de.d3web.empiricaltesting.Finding;
import de.d3web.empiricaltesting.RatedSolution;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.StateRating;
import de.d3web.testcase.model.Check;
import de.d3web.testcase.model.TestCase;
import de.d3web.testcase.stc.DerivedQuestionCheck;
import de.d3web.testcase.stc.DerivedSolutionCheck;
import de.d3web.testcase.stc.STCWrapper;
import de.d3web.we.knowledgebase.KnowledgeBaseType;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.table.ConditionCheck;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.09.2012
 */
public class TestCaseUtils {

	public static List<Triple<TestCaseProvider, Section<?>, Article>> getTestCaseProviders(Section<TestCasePlayerType> section) {
		String[] kbpackages = DefaultMarkupType.getPackages(section,
				KnowledgeBaseType.ANNOTATION_COMPILE);
		String web = section.getWeb();
		return de.knowwe.testcases.TestCaseUtils.getTestCaseProviders(web, kbpackages);
	}

	public static List<Triple<TestCaseProvider, Section<?>, Article>> getTestCaseProviders(String web, String... kbpackages) {
		Environment env = Environment.getInstance();
		PackageManager packageManager = env.getPackageManager(web);
		ArticleManager articleManager = env.getArticleManager(web);
		List<Triple<TestCaseProvider, Section<?>, Article>> providers = new LinkedList<Triple<TestCaseProvider, Section<?>, Article>>();
		for (String kbpackage : kbpackages) {
			List<Section<?>> sectionsInPackage = packageManager.getSectionsOfPackage(kbpackage);
			Set<String> articlesReferringTo = packageManager.getCompilingArticles(kbpackage);
			for (String masterTitle : articlesReferringTo) {
				Article masterArticle = articleManager.getArticle(masterTitle);
				for (Section<?> packageSections : sectionsInPackage) {
					TestCaseProviderStorage testCaseProviderStorage =
							(TestCaseProviderStorage) packageSections.getSectionStore().getObject(
									masterArticle,
									TestCaseProviderStorage.KEY);
					if (testCaseProviderStorage != null) {
						for (TestCaseProvider testCaseProvider : testCaseProviderStorage.getTestCaseProviders()) {
							providers.add(new Triple<TestCaseProvider, Section<?>, Article>(
									testCaseProvider,
									packageSections, masterArticle));
						}
					}
				}
			}
		}
		return providers;
	}

	public static SequentialTestCase transformToSTC(TestCase testCase, String testCaseName, KnowledgeBase kb) {
		SequentialTestCase stc = null;
		if (testCase instanceof STCWrapper) {
			stc = ((STCWrapper) testCase).getSequentialTestCase();
			for (RatedTestCase rtc : stc.getCases()) {
				addChecks(testCase, rtc, rtc.getTimeStamp(), kb);
			}
		}
		else {
			stc = new SequentialTestCase();
			if (testCaseName != null) {
				stc.setName(testCaseName);
			}
			for (Date date : testCase.chronology()) {
				RatedTestCase rtc = new RatedTestCase();
				rtc.setTimeStamp(date);
				addFindings(testCase, rtc, date, kb);
				addChecks(testCase, rtc, date, kb);
				stc.add(rtc);
			}
		}

		return stc;
	}

	private static void addFindings(TestCase testCase, RatedTestCase rtc, Date date, KnowledgeBase kb) {
		for (de.d3web.testcase.model.Finding finding : testCase.getFindings(date, kb)) {
			if (finding.getTerminologyObject() instanceof Question) {
				Question question = (Question) finding.getTerminologyObject();
				QuestionValue value = (QuestionValue) finding.getValue();
				Finding rtcFinding = new Finding(
						question, value);
				rtc.add(rtcFinding);
			}
			else {
				throw new TransformationException(
						"SequentialTestCases do not support Solution based Findings");
			}
		}
	}

	private static void addChecks(TestCase testCase, RatedTestCase rtc, Date date, KnowledgeBase kb) {
		for (Check check : testCase.getChecks(date, kb)) {
			if (check instanceof DerivedSolutionCheck) {
				addDerivedSolutionCheck(rtc, (DerivedSolutionCheck) check);
			}
			else if (check instanceof DerivedQuestionCheck) {
				addDerivedQuestionCheck(rtc, (DerivedQuestionCheck) check);
			}
			else if (check instanceof ConditionCheck) {
				addConditionCheck(rtc, (ConditionCheck) check);
			}
			else {
				throwUntransformableObjectException(check);
			}
		}
	}

	private static void addDerivedSolutionCheck(RatedTestCase rtc, DerivedSolutionCheck check) {
		Solution solution = check.getSolution();
		Rating rating = check.getRating();
		rtc.addDerived(new RatedSolution(solution, new StateRating(rating)));
	}

	private static void addDerivedQuestionCheck(RatedTestCase rtc, DerivedQuestionCheck check) {
		Question question = check.getQuestion();
		QuestionValue value = check.getValue();
		rtc.addExpectedFinding(new Finding(question, value));
	}

	private static void addConditionCheck(RatedTestCase rtc, ConditionCheck check) {
		Condition condition = check.getConditionObject();
		handleCondition(rtc, condition);
	}

	private static void handleCondition(RatedTestCase rtc, Condition condition) {
		if (condition instanceof CondQuestion) {
			addCondQuestion(rtc, (CondQuestion) condition);
		}
		else if (condition instanceof CondAnd) {
			addCondAnd(rtc, (CondAnd) condition);
		}
		else if (condition instanceof CondDState) {
			addCondDState(rtc, condition);
		}
		else {
			throwUntransformableObjectException(condition);
		}
	}

	private static void addCondQuestion(RatedTestCase rtc, CondQuestion condition) {
		Question question = condition.getQuestion();
		if (condition instanceof CondEqual) {
			QuestionValue value = (QuestionValue) ((CondEqual) condition).getValue();
			rtc.addExpectedFinding(new Finding(question, value));
		}
		else if (condition instanceof CondNum) {
			QuestionValue value = new NumValue(((CondNum) condition).getConditionValue());
			rtc.addExpectedFinding(new Finding(question, value));
		}
		else if (condition instanceof CondUnknown) {
			rtc.addExpectedFinding(new Finding(question, Unknown.getInstance()));
		}
		else if (condition instanceof CondDate) {
			QuestionValue value = ((CondDate) condition).getValue();
			rtc.addExpectedFinding(new Finding(question, value));
		}
		else {
			throwUntransformableObjectException(condition);
		}
	}

	private static void addCondAnd(RatedTestCase rtc, CondAnd condAnd) {
		for (Condition condition : condAnd.getTerms()) {
			handleCondition(rtc, condition);
		}
	}

	private static void addCondDState(RatedTestCase rtc, Condition condition) {
		Solution solution = ((CondDState) condition).getSolution();
		State ratingState = ((CondDState) condition).getRatingState();
		rtc.addExpected(new RatedSolution(solution, new StateRating(new Rating(ratingState))));
	}

	private static void throwUntransformableObjectException(Object object) {
		throw new TransformationException(
				"Unable to transform " + object.getClass().getName()
						+ " for a SequentialTestCase");
	}

}
