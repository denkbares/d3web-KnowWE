package tests;

/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

import java.io.IOException;

import junit.framework.TestCase;
import utils.TSUtil;
import utils.TestArticleManager;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.plugin.test.InitPluginManager;
import de.knowwe.core.kdom.Article;

/**
 * This class tests whether the TestCases are equal
 * 
 * @author Sebastian Furth
 * @see TSUtil to modify the TestSuite against which everything is tested
 * @see TestCaseTest.txt to modify the Article which is tested
 * 
 */
public class TestCaseTest extends TestCase {

	private final String TESTSUITEARTICLE = "src/test/resources/TestCaseTest.txt";

	@Override
	protected void setUp() throws IOException {
		InitPluginManager.init();
	}

	/**
	 * Test some aspects of the TestCase NOTE: derived Solutions are not tested
	 * because they have nothing to do with the creation/ parsing of the the
	 * TestSuite(-section)
	 */
	public void testTestCases() {
		Article article =
				TestArticleManager.getArticle(TESTSUITEARTICLE);
		de.d3web.empiricaltesting.TestCase loadedTS =
				TSUtil.getInstance().findTestSuite(article);
		de.d3web.empiricaltesting.TestCase createdTS = TSUtil.getInstance().getCreatedTS();

		assertNotNull("TestCase has no Sequential-Test-Case.",
				loadedTS.getRepository().get(0));
		assertNotNull("TestCase has no Sequential-Test-Case.",
				createdTS.getRepository().get(0));

		assertEquals("Sequential-Test-Case has wrong name.",
				createdTS.getRepository().get(0).getName(),
				loadedTS.getRepository().get(0).getName());

		RatedTestCase loadedRTC =
				loadedTS.getRepository().get(0).getCases().get(0);
		RatedTestCase createdRTC =
				createdTS.getRepository().get(0).getCases().get(0);

		assertNotNull("Sequential-Test-Case has no Rated-Test-Case.",
				loadedRTC);
		assertNotNull("Sequential-Test-Case has no Rated-Test-Case.",
				createdRTC);

		assertEquals("Rated-Test-Case has wrong findings.",
				loadedRTC.getFindings(),
				createdRTC.getFindings());
		assertEquals("Rated-Test-Case has wrong expected solutions.",
				loadedRTC.getExpectedSolutions(),
				createdRTC.getExpectedSolutions());
	}

}
