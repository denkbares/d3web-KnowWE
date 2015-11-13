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

import java.util.LinkedList;
import java.util.List;

import de.d3web.testcase.model.DefaultTestCase;
import de.d3web.testcase.model.DescribedTestCase;
import de.d3web.testcase.model.TestCase;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.table.Table;
import de.knowwe.testcases.DefaultTestCaseStorage;
import de.knowwe.testcases.SingleTestCaseProvider;
import de.knowwe.testcases.TestCaseProvider;
import de.knowwe.testcases.TestCaseUtils;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Handles the stuff in {@link TestCase} creation, that is knowledge base or compiler specific.
 *
 * @author Albrecht Striffler (denkbares GmbH), Reinhard Hatko
 * @created 27.05.2011
 */
public class TestCaseTableD3webCompileScript implements D3webCompileScript<Table> {

	@Override
	public void compile(D3webCompiler compiler, Section<Table> section) {

		Section<DefaultMarkupType> defaultMarkupSection = $(section).ancestor(DefaultMarkupType.class).getFirst();

		DescribedTestCase testCase = TestCaseTableScript.getTestCase(section);

		SingleTestCaseProvider provider = new SingleTestCaseProvider(
				compiler, Sections.ancestor(section, DefaultMarkupType.class), testCase, testCase.getDescription());

		// append Storage of the TestCaseProvider to the section of the default markup
		List<TestCaseProvider> list = new LinkedList<>();
		list.add(provider);
		TestCaseUtils.storeTestCaseProviderStorage(compiler, defaultMarkupSection, new DefaultTestCaseStorage(list));

	}
}
