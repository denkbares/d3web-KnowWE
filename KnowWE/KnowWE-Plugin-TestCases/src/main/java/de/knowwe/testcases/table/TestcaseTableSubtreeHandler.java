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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import de.d3web.core.inference.condition.CondAnd;
import de.d3web.core.inference.condition.Condition;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.TestCase;
import de.d3web.testcase.model.Check;
import de.d3web.testcase.stc.STCWrapper;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.Conjunct;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.testcases.DefaultTestCaseStorage;
import de.knowwe.testcases.SingleTestCaseProvider;
import de.knowwe.testcases.TestCaseProvider;
import de.knowwe.testcases.TestCaseUtils;
import de.knowwe.core.kdom.basicType.TimeStampType;

/**
 * @author Reinhard Hatko
 * @created 27.05.2011
 */
public class TestcaseTableSubtreeHandler implements D3webHandler<Table> {

	@Override
	public Collection<Message> create(D3webCompiler compiler, Section<Table> section) {

		TestCase testcase = new TestCase();
		SequentialTestCase stc = new SequentialTestCase();

		testcase.getRepository().add(stc);

		Map<RatedTestCase, List<Check>> conditionsForRTC = new IdentityHashMap<>();
		List<Section<TableLine>> lines = Sections.successors(section, TableLine.class);
		Date lastTimeStamp = null;
		for (Section<TableLine> line : lines) {
			// check for an rated test case of that line
			RatedTestCase rtc = (RatedTestCase) KnowWEUtils.getStoredObject(compiler, line,
					TestcaseTableLineSubtreeHandler.TESTCASE_KEY);
			if (rtc == null) continue;
			Date timeStamp = rtc.getTimeStamp();
			if (lastTimeStamp != null && timeStamp != null && !timeStamp.after(lastTimeStamp)) {
				Section<TimeStampType> timeStampSection = Sections.successor(line, TimeStampType.class);
				//noinspection ConstantConditions
				Messages.storeMessage(compiler, timeStampSection, this.getClass(),
						Messages.error("Invalid time stamp '" + timeStampSection.getText()
								+ "', each time stamp has to be after the previous one."));
				return Messages.noMessage();
			}

			stc.add(rtc);
			lastTimeStamp = timeStamp;

			// also check for additional check conditions
			Section<CompositeCondition> condSec =
					Sections.successor(line, CompositeCondition.class);
			if (condSec == null) continue;
			Condition condition = KDOMConditionFactory.createCondition(compiler, condSec);
			if (condition == null) continue;

			// add multiple checks for and-conditions
			List<Check> checks = new ArrayList<Check>();
			conditionsForRTC.put(rtc, checks);
			if (condition instanceof CondAnd) {
				List<Section<Conjunct>> parts = Sections.children(condSec, Conjunct.class);
				List<Condition> terms = ((CondAnd) condition).getTerms();
				if (parts.size() == terms.size()) {
					for (int i = 0; i < terms.size(); i++) {
						checks.add(new KnowWEConditionCheck(terms.get(i), parts.get(i)));
					}
				}
			}

			// add one check if we have not added multiple ones
			if (checks.isEmpty()) {
				checks.add(new KnowWEConditionCheck(condition, condSec));
			}
		}

		List<Section<Table>> sections = Sections.successors(section.getArticle().getRootSection(), Table.class);
		int i = 1;
		for (Section<Table> table : new TreeSet<>(sections)) {
			if (table.equals(section)) {
				break;
			}
			else {
				i++;
			}
		}
		Section<? extends Type> defaultmarkupSection = section.getParent().getParent();
		String name = DefaultMarkupType.getAnnotation(defaultmarkupSection, TestcaseTableType.NAME);
		if (name == null) {
			name = section.getArticle().getTitle() + "/TestCaseTable" + i;
		}

		// create a test case provider and a STC wrapper
		// for this test case. Also include the additional
		// tests of this test case
		stc.setName(name);
		STCWrapper wrapper = new STCWrapper(stc);
		for (RatedTestCase rtc : conditionsForRTC.keySet()) {
			List<Check> checks = conditionsForRTC.get(rtc);
			wrapper.addChecks(rtc, checks.toArray(new Check[checks.size()]));
		}
		SingleTestCaseProvider provider = new SingleTestCaseProvider(
				compiler, Sections.ancestor(section, DefaultMarkupType.class), wrapper,
				name);

		// append Storage of the TestCaseProvider
		// to the section of the default markup
		List<TestCaseProvider> list = new LinkedList<TestCaseProvider>();
		list.add(provider);
		TestCaseUtils.storeTestCaseProviderStorage(compiler, defaultmarkupSection,
				new DefaultTestCaseStorage(list));

		return null;
	}
}
