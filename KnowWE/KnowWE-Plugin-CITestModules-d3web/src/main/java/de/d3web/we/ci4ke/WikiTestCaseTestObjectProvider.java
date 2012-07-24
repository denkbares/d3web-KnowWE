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
package de.d3web.we.ci4ke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.TestCase;
import de.d3web.testing.TestObjectProvider;
import de.d3web.we.testcase.TestCaseUtils;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;

/**
 * Searches the wiki for test cases. All found test case repositories are
 * divided in single-test test cases with names to serve as test-objects. The
 * test case names are checked against the given name parameter using regex.
 * 
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 29.06.2012
 */
public class WikiTestCaseTestObjectProvider implements TestObjectProvider {

	@Override
	public <T> List<T> getTestObjects(Class<T> c, String name) {

		if (c == null) {
			Logger.getLogger(this.getClass()).warn("Class given to TestObjectProvider was 'null'");
			return Collections.emptyList();
		}
		if (!c.equals(TestCase.class)) {
			return Collections.emptyList();
		}
		List<T> result = new ArrayList<T>();
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(name);
		} catch (java.util.regex.PatternSyntaxException e) {
			return result;
		}


		// looks in all articles
		Environment env = Environment.getInstance();
		if (env != null) {
			for (Article master : env.getArticleManager(Environment.DEFAULT_WEB).getArticles()) {
				TestCase testcase = TestCaseUtils.loadTestSuite(
						master.getTitle(), Environment.DEFAULT_WEB);
				if (testcase != null) {
					// a repo is found, it is separated in distinct named test
					// cases
					List<SequentialTestCase> repository = testcase.getRepository();
					for (SequentialTestCase sequentialTestCase : repository) {
						String testcaseName = sequentialTestCase.getName();
						if (testcaseName != null && pattern.matcher(testcaseName).find()) {
							TestCase newCase = new TestCase();
							newCase.setKb(testcase.getKb());
							newCase.setName(testcaseName);
							List<SequentialTestCase> singleList = new ArrayList<SequentialTestCase>();
							singleList.add(sequentialTestCase);
							newCase.setRepository(singleList);
							result.add(c.cast(newCase));
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public <T> String getTestObjectName(T testObject) {
		if (testObject instanceof TestCase) {
			return ((TestCase) testObject).getName();
		}
		return null;
	}

}
