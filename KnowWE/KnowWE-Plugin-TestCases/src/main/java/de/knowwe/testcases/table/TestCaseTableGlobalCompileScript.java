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

import java.util.List;

import de.d3web.testcase.model.DefaultTestCase;
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

/**
 * This script does all the stuff of the TestCase creation, that is independent of the actual d3web Compilers and
 * KnowledgeBases.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 03.11.15
 */
public class TestCaseTableGlobalCompileScript extends DefaultGlobalScript<Table> {

	private static final String TEST_CASE_KEY = "textCaseKey";

	@Override
	public void compile(DefaultGlobalCompiler compiler, Section<Table> section) throws CompilerMessage {
		DefaultTestCase testCase = new DefaultTestCase();

		section.storeObject(TEST_CASE_KEY, testCase);

		List<Section<TableLine>> lines = Sections.successors(section, TableLine.class);

		long lastTimeStamp = Long.MIN_VALUE;
		for (Section<TableLine> line : lines) {
			// check sequential time stamps
			Section<TimeStampType> timeStampSection = Sections.successor(line, TimeStampType.class);
			if (timeStampSection != null) {
				try {
					long timeStamp = TimeStampType.getTimeInMillis(timeStampSection);
					if (lastTimeStamp > timeStamp) {
						Messages.storeMessage(compiler, timeStampSection, this.getClass(),
								Messages.error("Invalid time stamp '" + timeStampSection.getText()
										+ "', each time stamp has to be after the previous one."));
						return;
					}
					else {
						Messages.clearMessages(compiler, timeStampSection, this.getClass());
					}
				}
				catch (NumberFormatException ignore) {
					// invalid time, will be handled by TimeStampType already.... just skip here
				}
			}

		}

		List<Section<Table>> sections = $(section.getArticle().getRootSection()).successor(TestcaseTableType.class)
				.successor(Table.class)
				.asList();
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
		String name = DefaultMarkupType.getAnnotation(defaultMarkupSection, TestcaseTableType.NAME);
		if (name == null) {
			name = section.getArticle().getTitle() + "/TestCaseTable" + i;
		}
		testCase.setDescription(name);
	}

	public static DefaultTestCase getTestCase(Section<Table> tableSection) {
		return (DefaultTestCase) tableSection.getObject(TEST_CASE_KEY);
	}
}
