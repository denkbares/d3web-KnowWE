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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.CondDState;
import de.d3web.core.inference.condition.CondDate;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.inference.condition.CondNum;
import de.d3web.core.inference.condition.CondQuestion;
import de.d3web.core.inference.condition.CondRegex;
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
import de.d3web.empiricaltesting.Finding;
import de.d3web.empiricaltesting.RatedSolution;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.empiricaltesting.RegexFinding;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.StateRating;
import de.d3web.testcase.model.Check;
import de.d3web.testcase.model.TestCase;
import de.d3web.testcase.stc.DerivedQuestionCheck;
import de.d3web.testcase.stc.DerivedSolutionCheck;
import com.denkbares.utils.Triple;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.table.KnowWEConditionCheck;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 13.09.2012
 */
public class TestCaseUtils {

	private static final String PROVIDER_STORAGE_KEY = "TestCaseProviderStorage";

	public static Collection<TestCaseProviderStorage> getTestCaseProviderStorages(Section<?> section) {
		checkSection(section);
		Collection<TestCaseProviderStorage> storages = new ArrayList<>();
		Collection<D3webCompiler> compilers = Compilers.getCompilers(section, D3webCompiler.class);
		for (D3webCompiler compiler : compilers) {
			TestCaseProviderStorage storage = getTestCaseProviderStorage(compiler, section);
			if (storage != null) storages.add(storage);
		}
		return storages;
	}

	public static TestCaseProviderStorage getTestCaseProviderStorage(D3webCompiler compiler, Section<?> section) {
		checkSection(section);
		return (TestCaseProviderStorage) section.getObject(compiler, PROVIDER_STORAGE_KEY);
	}

	public static TestCaseProviderStorage getTestCaseProviderStorage(Section<?> section) {
		checkSection(section);
		return getTestCaseProviderStorages(section).iterator().next();
	}

	public static void storeTestCaseProviderStorage(D3webCompiler compiler, Section<?> section, TestCaseProviderStorage testCaseProviderStorage) {
		checkSection(section);
		section.storeObject(compiler, PROVIDER_STORAGE_KEY,	testCaseProviderStorage);
	}

	private static void checkSection(Section<?> section) {
		if (section.get() instanceof DefaultMarkupType) return;
		throw new IllegalArgumentException("section has to be of the type "
				+ DefaultMarkupType.class.getSimpleName());
	}

	public static List<TestCaseProvider> getTestCaseProviders(String web, Set<String> packageNames, String nameRegex) throws PatternSyntaxException {
		return getTestCaseProviders(web, packageNames,
				Pattern.compile(nameRegex, Pattern.CASE_INSENSITIVE));
	}

	public static List<TestCaseProvider> getTestCaseProviders(String web, Set<String> packageNames, Pattern nameRegex) {
		if (nameRegex == null) return Collections.emptyList();
		String[] packages = packageNames.toArray(new String[packageNames.size()]);
		List<ProviderTriple> testCaseProviders = TestCaseUtils.getTestCaseProviders(
				web, packages);
		List<TestCaseProvider> found = new LinkedList<>();
		for (Triple<TestCaseProvider, Section<?>, Section<? extends PackageCompileType>> triple : testCaseProviders) {
			TestCaseProvider provider = triple.getA();
			if (nameRegex.matcher(provider.getName()).matches()) {
				found.add(provider);
			}
		}
		return found;
	}

	/**
	 * Returns all {@link TestCaseProvider}s the given user is allowed to see
	 * and that are in the same package as the given section.
	 *
	 * @param context the context of the user
	 * @param section the section used as a reference for the package
	 * @return all {@link TestCaseProvider} visible to this user
	 * @created 07.10.2013
	 */
	public static List<ProviderTriple> getTestCaseProviders(UserContext context, Section<TestCasePlayerType> section) {
		List<ProviderTriple> testCaseProviders = getTestCaseProviders(section);
		List<ProviderTriple> filtered = new ArrayList<>();
		for (ProviderTriple triple : testCaseProviders) {
			boolean userCanViewCase = Environment.getInstance().getWikiConnector().userCanViewArticle(
					triple.getB().getTitle(), context.getRequest());
			if (userCanViewCase) {
				filtered.add(triple);
			}
		}
		return filtered;
	}

	/**
	 * Returns all {@link TestCaseProvider}s in the packages the given section
	 * is in.
	 *
	 * @param section the section used as a reference for the package
	 * @return all {@link TestCaseProvider} in the packages the given section is
	 * in
	 * @created 07.10.2013
	 */
	public static List<ProviderTriple> getTestCaseProviders(Section<TestCasePlayerType> section) {
		String[] kbPackages = getTestCasePackages(section);
		return de.knowwe.testcases.TestCaseUtils.getTestCaseProviders(section.getWeb(), kbPackages);
	}

	public static String[] getTestCasePackages(Section<TestCasePlayerType> section) {
		return section.getPackageNames().toArray(new String[0]);
	}

	public static List<ProviderTriple> getTestCaseProviders(String web, String... kbPackages) {
		PackageManager packageManager = KnowWEUtils.getPackageManager(web);
		List<ProviderTriple> providers = new LinkedList<>();
		for (String kbPackage : kbPackages) {
			Collection<Section<?>> sectionsInPackage = packageManager.getSectionsOfPackage(kbPackage);

			for (Section<?> section : sectionsInPackage) {

				if (!(section.get() instanceof DefaultMarkupType)) continue;

				D3webCompiler compiler = D3webUtils.getCompiler(section);
				TestCaseProviderStorage testCaseProviderStorage = getTestCaseProviderStorage(compiler, section);

				if (testCaseProviderStorage == null) continue;

				for (TestCaseProvider testCaseProvider : testCaseProviderStorage.getTestCaseProviders()) {
					providers.add(new ProviderTriple(testCaseProvider, section, compiler.getCompileSection()));
				}
			}
		}
		return providers;
	}

	public static SequentialTestCase transformToSTC(TestCase testCase, String testCaseName, KnowledgeBase kb) {
		if (testCase instanceof SequentialTestCase) {
			return (SequentialTestCase) testCase;
		}
		SequentialTestCase stc = new SequentialTestCase();
		if (testCaseName != null) {
			stc.setName(testCaseName);
		}
		for (Date date : testCase.chronology()) {
			RatedTestCase rtc = new RatedTestCase();
			rtc.setTimeStamp(date);
			addFindings(testCase, rtc, date, kb);
			addChecks(testCase, rtc, date, kb);
			stc.addCase(rtc);
		}

		return stc;
	}

	private static void addFindings(TestCase testCase, RatedTestCase rtc, Date date, KnowledgeBase kb) {
		for (de.d3web.testcase.model.Finding finding : testCase.getFindings(date, kb)) {
			if (finding.getTerminologyObject() instanceof Question) {
				Question question = (Question) finding.getTerminologyObject();
				QuestionValue value = (QuestionValue) finding.getValue();
				Finding rtcFinding = new Finding(question, value);
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
			else if (check instanceof KnowWEConditionCheck) {
				addConditionCheck(rtc, (KnowWEConditionCheck) check);
			}
			else {
				throwUntransformableObjectException(check);
			}
		}
	}

	private static void addDerivedSolutionCheck(RatedTestCase rtc, DerivedSolutionCheck check) {
		Solution solution = check.getSolution();
		Rating rating = check.getRating();
		// We always use expected solutions instread of derived solutions to
		// stay consistent. Derived solutions are also loaded as expected
		// solutions.
		rtc.addExpected(new RatedSolution(solution, new StateRating(rating)));
	}

	private static void addDerivedQuestionCheck(RatedTestCase rtc, DerivedQuestionCheck check) {
		Question question = check.getQuestion();
		QuestionValue value = check.getValue();
		rtc.addExpectedFinding(new Finding(question, value));
	}

	private static void addConditionCheck(RatedTestCase rtc, KnowWEConditionCheck check) {
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

	private static void addCondRegex(RatedTestCase rtc, CondRegex condition) {
		rtc.addExpectedRegexFinding(new RegexFinding(condition.getQuestion(), condition.getRegex()));
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
		else if (condition instanceof CondRegex) {
			addCondRegex(rtc, (CondRegex) condition);
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
				"The given test case contains a '" + object.getClass().getSimpleName()
						+ "' which is not compatible with a SequentialTestCase."
		);
	}

}
