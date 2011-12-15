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
import java.util.regex.Pattern;

import de.d3web.we.ci4ke.testing.AbstractCITest;
import de.d3web.we.ci4ke.testing.CITestResult;
import de.d3web.we.ci4ke.testing.CITestResult.Type;
import de.knowwe.core.kdom.KnowWEArticle;

/**
 * This test can search articles (specified by a regexp pattern) for a keyword.
 * 
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 26.11.2010
 */
public class TestArticlesContain extends AbstractCITest {

	@Override
	public CITestResult call() {

		if (!checkIfParametersAreSufficient(2)) {
			return numberOfParametersNotSufficientError(2);
		}

		String articlesPattern = getParameter(0);
		String searchForKeyword = getParameter(1);

		String configuration = "article: " + articlesPattern +
				"; forbidden text: " + searchForKeyword;
		Pattern pattern = Pattern.compile(articlesPattern);

		List<String> namesOfArticlesWhichContainKeyword = new LinkedList<String>();

		for (KnowWEArticle article : getArticlesMatchingPattern(pattern)) {
			if (!article.getTitle().equals(config.getDashboardArticleTitle()) &&
					article.toString().contains(searchForKeyword)) {
				namesOfArticlesWhichContainKeyword.add(article.getTitle());
			}
		}

		// If at least one article was found, this test is FAILED
		int count = namesOfArticlesWhichContainKeyword.size();
		if (count > 0) {
			String message = "<b>Forbidden text found in " + count + " articles:</b>\n" +
					"<ul><li>" + de.knowwe.core.utils.Strings.concat("</li><li>",
							namesOfArticlesWhichContainKeyword) + "</li></ul>";
			return new CITestResult(Type.FAILED, message, configuration);
		}
		else {
			return new CITestResult(Type.SUCCESSFUL, null, configuration);
		}

	}

}
