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

import java.util.logging.Logger;

import de.d3web.empiricaltesting.TestCase;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Sections;
import de.d3web.we.testcase.kdom.TestCaseContent;
import de.d3web.we.testcase.kdom.TestCaseType;
import de.d3web.we.utils.KnowWEUtils;

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
	 * @param article KnowWEArticle containing a TestSuiteType section
	 * @param web the current web
	 * @return loaded test suite or null (if no test suite was found)
	 */
	public static TestCase loadTestSuite(String article, String web) {
		KnowWEArticle a = KnowWEEnvironment.getInstance().getArticleManager(web).getArticle(article);
		Section<TestCaseContent> s = null;
		TestCase testSuite = null;

		if (a != null) {
			s = Sections.findSuccessor(a.getSection(), TestCaseContent.class);
		}
		else {
			Logger.getLogger(TestCaseUtils.class.getName()).warning(
					"Article: \"" + article + "\" wasn't found. Unable to load test suite!");
		}

		if (s != null) {
			testSuite = (TestCase) KnowWEUtils.getStoredObject(a, s,
					TestCaseType.TESTCASEKEY);
		}
		else {
			Logger.getLogger(TestCaseUtils.class.getName()).warning(
					"Article: \"" + article + "\" doesn't contain a TestSuiteType section.");
		}

		return testSuite;
	}

}
