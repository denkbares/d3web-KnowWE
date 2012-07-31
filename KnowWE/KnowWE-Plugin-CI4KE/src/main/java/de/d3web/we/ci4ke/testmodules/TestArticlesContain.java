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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.d3web.testing.Message;
import de.d3web.we.ci4ke.handling.CIDashboardType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * This test can search articles (specified by a regexp pattern) for a keyword.
 * 
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 26.11.2010
 */
public class TestArticlesContain extends AbstractTest<Article> {

	@Override
	public Message execute(Article article, String[] args) {

		String searchForKeyword = args[0];

		List<String> namesOfArticlesWhichContainKeyword = new LinkedList<String>();

		if (article.getRootSection().getText().contains(searchForKeyword)) {
			// exclude findings from the CIDashboard because it will always
			// contain the seached string, because it is defined there.
			Collection<Section<?>> smallestSectionsContaining = Sections.findSmallestSectionsContaining(
					article.getRootSection(), searchForKeyword);
			boolean contains = false;
			for (Section<?> containingSection : smallestSectionsContaining) {
				if (Sections.findAncestorOfType(containingSection, CIDashboardType.class) == null) {
					contains = true;
					break;
				}
			}
			if (contains) {
				namesOfArticlesWhichContainKeyword.add(article.getTitle());
			}
		}

		// If at least one article was found, this test is FAILED
		int count = namesOfArticlesWhichContainKeyword.size();
		if (count > 0) {
			String message = "<b>Forbidden text found in " + count + " articles:</b>\n" +
					"<ul><li>" + de.knowwe.core.utils.Strings.concat("</li><li>",
							namesOfArticlesWhichContainKeyword) + "</li></ul>";
			return new Message(Message.Type.FAILURE, message);
		}
		else {
			return new Message(Message.Type.SUCCESS, null);
		}

	}

	@Override
	public Class<Article> getTestObjectClass() {
		return Article.class;
	}

	@Override
	public int numberOfArguments() {
		return 1;
	}
	
	@Override
	public String getDescription() {
		return "Checks, whether the articles contain the text defined by <searchString>.";
	}

}
