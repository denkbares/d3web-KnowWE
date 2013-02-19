/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.testcases.table;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import de.d3web.core.inference.condition.Condition;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.TestCase;
import de.d3web.testcase.model.Check;
import de.d3web.testcase.stc.STCWrapper;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.DefaultTestCaseStorage;
import de.knowwe.testcases.SingleTestCaseProvider;
import de.knowwe.testcases.TestCaseProvider;
import de.knowwe.testcases.TestCaseProviderStorage;

/**
 * 
 * @author Reinhard Hatko
 * @created 27.05.2011
 */
public class TestcaseTableSubtreeHandler extends SubtreeHandler<TestcaseTable> {

	@Override
	public Collection<Message> create(Article article, Section<TestcaseTable> s) {

		TestCase testcase = new TestCase();
		SequentialTestCase stc = new SequentialTestCase();

		testcase.getRepository().add(stc);

		Map<RatedTestCase, Check> conditionsForRTC =
				new IdentityHashMap<RatedTestCase, Check>();
		List<Section<TestcaseTableLine>> lines =
				Sections.findSuccessorsOfType(s, TestcaseTableLine.class);
		for (Section<TestcaseTableLine> section : lines) {
			// check for an rated test case of that line
			RatedTestCase rtc = (RatedTestCase) KnowWEUtils.getStoredObject(article, section,
					TestcaseTableLine.TESTCASE_KEY);
			if (rtc == null) continue;
			stc.add(rtc);

			// also check for additional check conditions
			Section<CompositeCondition> condSec =
					Sections.findSuccessor(section, CompositeCondition.class);
			if (condSec == null) continue;
			Condition condition = KDOMConditionFactory.createCondition(article, condSec);
			if (condition == null) continue;
			Check check = new ConditionCheck(condition, condSec.getText());
			conditionsForRTC.put(rtc, check);
		}

		KnowWEUtils.storeObject(s.getArticle(), s, TestcaseTable.TESTCASE_KEY, testcase);

		List<Section<TestcaseTable>> sections = Sections.findSuccessorsOfType(
				s.getArticle().getRootSection(), TestcaseTable.class);
		int i = 1;
		for (Section<TestcaseTable> section : new TreeSet<Section<TestcaseTable>>(sections)) {
			if (section.equals(s)) {
				break;
			}
			else {
				i++;
			}
		}
		Section<? extends Type> defaultmarkupSection = s.getFather().getFather();
		String name = DefaultMarkupType.getAnnotation(defaultmarkupSection, TestcaseTableType.NAME);
		if (name == null) {
			name = s.getArticle().getTitle() + "/TestCaseTable" + i;
		}

		// create a test case provider and a STC wrapper
		// for this test case. Also include the additional
		// tests of this test case
		stc.setName(name);
		STCWrapper wrapper = new STCWrapper(stc);
		for (RatedTestCase rtc : conditionsForRTC.keySet()) {
			wrapper.addCheck(rtc, conditionsForRTC.get(rtc));
		}
		SingleTestCaseProvider provider = new SingleTestCaseProvider(
				article, Sections.findAncestorOfType(s, DefaultMarkupType.class), wrapper, name);

		// append Storage of the TestCaseProvider
		// to the section of the default markup
		List<TestCaseProvider> list = new LinkedList<TestCaseProvider>();
		list.add(provider);
		defaultmarkupSection.getSectionStore().storeObject(article,
				TestCaseProviderStorage.KEY,
				new DefaultTestCaseStorage(list));

		return null;
	}
}
