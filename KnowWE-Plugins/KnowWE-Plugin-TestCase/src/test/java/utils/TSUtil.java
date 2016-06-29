package utils;

/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Rating.State;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.values.ChoiceID;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.empiricaltesting.Finding;
import de.d3web.empiricaltesting.RatedSolution;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.StateRating;
import de.d3web.empiricaltesting.TestCase;
import de.d3web.we.testcase.kdom.TestCaseContent;
import de.d3web.we.testcase.kdom.TestCaseType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * 
 * @author Sebastian Furth
 * @created 27/10/2010
 */
public class TSUtil {

	private static TSUtil instance = new TSUtil();
	private TestCase createdTS;
	private KnowledgeBase kb;

	private TSUtil() {
		createKB();
		createTestSuite();
	}

	public static TSUtil getInstance() {
		return instance;
	}

	public TestCase findTestSuite(Article article) {
		Section<TestCaseContent> s = Sections.successor(article.getRootSection(),
				TestCaseContent.class);
		return (TestCase) s.getObject(TestCaseType.TESTCASEKEY);
	}

	public KnowledgeBase getCreatedKB() {
		return kb;
	}

	/**
	 * Returns the TestSuite which was created manually.
	 * 
	 * @return TestSuite
	 */
	public TestCase getCreatedTS() {
		return createdTS;
	}

	private void createKB() {
		kb = new KnowledgeBase();

		// Root Solution
		Solution p0 = new Solution(kb, "P000");
		kb.setRootSolution(p0);

		// Solution
		Solution p = new Solution(kb, "Other problem");
		kb.getRootSolution().addChild(p);

		// Root Questionnaire
		QContainer qc0 = new QContainer(kb, "Q000");
		kb.setRootQASet(qc0);

		// Questionnaire
		QContainer qc = new QContainer(kb, "Observations");
		kb.getRootQASet().addChild(qc);

		// Add question:
		// - Driving [mc]
		// -- insufficient power on partial load
		// -- insufficient power on full load
		// -- unsteady idle speed
		// -- everything is fine
		new QuestionMC(qc, "Driving",
						"insufficient power on partial load",
						"insufficient power on full load",
						"unsteady idle speed",
						"everything is fine");

	}

	/**
	 * Creats a TestSuite similar to the one which is created in the
	 * Article
	 */
	private void createTestSuite() {

		// Create Finding
		Question q = kb.getManager().searchQuestion("Driving");
		Choice a = KnowledgeBaseUtils.findChoice((QuestionChoice) q, "everything is fine");
		Finding f = new Finding(q, new MultipleChoiceValue(new ChoiceID(a)));

		// Create RatedSolution
		Solution d = kb.getManager().searchSolution("Other problem");
		StateRating sr = new StateRating(new Rating(State.ESTABLISHED));
		RatedSolution rs = new RatedSolution(d, sr);

		// Add Finding and RatedSolution to RatedTestCase
		RatedTestCase rtc = new RatedTestCase();
		rtc.add(f);
		rtc.addExpected(rs);
		rtc.setName("STC1_RTC1");

		// Add RatedTestCase to SequentialTestCase
		SequentialTestCase stc = new SequentialTestCase();
		stc.addCase(rtc);
		stc.setName("STC1");

		// Add SequentialTestCase to the repository
		List<SequentialTestCase> repository = new ArrayList<>();
		repository.add(stc);

		// Create testSuite
		TestCase t = new TestCase();
		t.setKb(kb);
		t.setRepository(repository);
		createdTS = t;
	}

}
