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

package de.d3web.we.ci4ke.testmodules;

import java.util.LinkedList;
import java.util.List;

import de.d3web.we.ci4ke.handling.AbstractCITest;
import de.d3web.we.ci4ke.handling.CITestResult;
import de.d3web.we.ci4ke.handling.CITestResult.TestResultType;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;

public class TestArticleContainsTODO extends AbstractCITest {

	/**
	 * This Test is successful, if and only if the given article does not
	 * contain the word "T O D O" (in upper-case, without spaces).
	 */
	@Override
	public CITestResult call() {

		List<String> namesOfArticlesWhichContainTODO = new LinkedList<String>();

		// iterate over all KnowWEArticles in the given web
		for (KnowWEArticle article : KnowWEEnvironment.getInstance().
				getArticleManager(KnowWEEnvironment.DEFAULT_WEB).getArticles()) {
			// if an article contains the word "T O D O" (in upper-case, without
			// spaces)...
			if (article.toString().contains("TODO"))
			// ...add its name to the list
			namesOfArticlesWhichContainTODO.add(article.getTitle());
		}

		// If at least one article was found, this test is FAILED
		if (namesOfArticlesWhichContainTODO.size() > 0) return new CITestResult(
				TestResultType.FAILED,
					"<b>The following Articles contain a 'TODO': " +
							de.d3web.we.utils.Strings.concat(", ",
									namesOfArticlesWhichContainTODO) + "</b>");
		else return new CITestResult(TestResultType.SUCCESSFUL);

	}
}
