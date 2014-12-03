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
package de.d3web.we.testcase;

import de.d3web.empiricaltesting.TestCase;
import de.d3web.we.testcase.kdom.TestCaseContent;
import de.d3web.we.testcase.kdom.TestCaseType;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Util class which offers convenience methods for the handling of test suites
 * 
 * @see TestCase
 * @author Sebastian Furth (denkbares GmbH)
 * @created 25/10/2010
 */
public class TestCaseUtils {

	/**
	 * Loads a test suite from the specified article.
	 * 
	 * @created 25/10/2010
	 * @param article Article containing a TestSuiteType section
	 * @param web the current web
	 * @return loaded test suite or null (if no test suite was found)
	 */
	public static TestCase loadTestSuite(String article, String web) {
		Article a = Environment.getInstance().getArticleManager(web).getArticle(article);
		Section<TestCaseContent> s = null;
		TestCase testSuite = null;

		if (a != null) {
			s = Sections.successor(a.getRootSection(), TestCaseContent.class);
		}
		if (s != null) {
			testSuite = (TestCase) s.getObject(TestCaseType.TESTCASEKEY);
		}
		return testSuite;
	}

}
