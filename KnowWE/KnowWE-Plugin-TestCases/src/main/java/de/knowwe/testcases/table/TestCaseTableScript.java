/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;

import de.d3web.testcase.model.CachingDefaultTestCase;
import de.d3web.testcase.model.CheckTemplate;
import de.d3web.testcase.model.DefaultTestCase;
import de.d3web.testcase.model.DescribedTestCase;
import de.d3web.testcase.model.FindingTemplate;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;
import de.knowwe.kdom.table.TableLine;

import static de.knowwe.core.kdom.parsing.Sections.$;
import static de.knowwe.testcases.table.TestCaseTableLineScript.*;

/**
 * This script does all the stuff of the TestCase creation, that is independent of the actual d3web Compilers and
 * KnowledgeBases. Checks sequential time stamps and adds all the artifacts to the TestCase.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 03.11.15
 */
public class TestCaseTableScript extends DefaultGlobalScript<Table> {

	private static final String TEST_CASE_KEY = "textCaseKey";

	@Override
	public void compile(DefaultGlobalCompiler compiler, Section<Table> section) throws CompilerMessage {
		DefaultTestCase testCase = new CachingDefaultTestCase();
		setTestCase(section, testCase);

		List<Section<TableLine>> lines = Sections.successors(section, TableLine.class);

		checkSequentialTimeStamps(compiler, lines, (lastDate, date) -> lastDate.before(date));
		testCase.setDescription(getTestCaseName(section));

		for (Section<TableLine> line : lines) {
			Date date = getDate(line);
			if (date == null) continue;

			// just make sure the entry exists in the test case, even if there are no other contents...
			testCase.addFinding(date);

			for (FindingTemplate findingTemplate : getFindingTemplates(line)) {
				testCase.addFinding(date, findingTemplate);
			}

			for (CheckTemplate checkTemplate : getCheckTemplates(line)) {
				testCase.addCheck(date, checkTemplate);
			}

			String description = getDescription(line);
			if (description != null) testCase.addDescription(date, description);
		}

	}

	protected void checkSequentialTimeStamps(DefaultGlobalCompiler compiler, List<Section<TableLine>> lines, BiFunction<Date, Date, Boolean> compareFunction) {
		Date lastDate = new Date(Long.MIN_VALUE);
		for (Section<TableLine> line : lines) {
			Section<TimeStampType> timeStampSection = Sections.successor(line, TimeStampType.class);
			if (timeStampSection != null) {
				Date date = getDate(line);
				if (date != null && !compareFunction.apply(lastDate, date)) {
					Messages.storeMessage(compiler, timeStampSection, this.getClass(),
							Messages.error("Invalid time stamp '" + timeStampSection.getText()
									+ "', each time stamp has to be after the previous one."));
				}
				else {
					Messages.clearMessages(compiler, timeStampSection, this.getClass());
				}
				if (date != null) lastDate = date;
			}
		}
	}

	protected String getTestCaseName(Section<Table> section) {
		List<Section<Table>> sections = $(section.getArticle().getRootSection()).successor(TestCaseTableType.class)
				.successor(Table.class).asList();
		int i = 1;
		for (Section<Table> table : sections) {
			if (table.equals(section)) {
				break;
			}
			else {
				i++;
			}
		}

		Section<DefaultMarkupType> defaultMarkupSection = $(section).ancestor(DefaultMarkupType.class).getFirst();
		String name = DefaultMarkupType.getAnnotation(defaultMarkupSection, TestCaseTableType.NAME);
		if (name == null) {
			name = section.getArticle().getTitle() + "/TestCaseTable" + i;
		}
		return name;
	}

	public static DescribedTestCase getTestCase(Section<Table> tableSection) {
		return (DescribedTestCase) tableSection.getObject(TEST_CASE_KEY);
	}

	protected static void setTestCase(Section<Table> tableSection, DescribedTestCase testCase) {
		tableSection.storeObject(TEST_CASE_KEY, testCase);
	}
}
