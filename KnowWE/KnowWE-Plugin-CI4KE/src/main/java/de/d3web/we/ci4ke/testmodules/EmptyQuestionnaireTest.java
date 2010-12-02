/*
 * Copyright (C) 2010 denkbares GmbH, Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.ci4ke.testmodules;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.ci4ke.testing.AbstractCITest;
import de.d3web.we.ci4ke.testing.CITestResult;
import de.d3web.we.ci4ke.testing.CITestResult.TestResultType;
import de.d3web.we.core.KnowWEEnvironment;

/**
 * This CITest searches for empty questionnaires on an article. It needs one
 * parameter for execution, eg.
 * <p>
 * \@test: EmptyQuestionnaireTest "Article - Master KB"
 * </p>
 * 
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 26.11.2010
 */
public class EmptyQuestionnaireTest extends AbstractCITest {

	@Override
	public CITestResult call() throws Exception {
		// check if one test-parameter was set
		if (!checkIfParametersAreSufficient(1)) {
			return numberOfParametersNotSufficientError(1);
		}
		// get the first parameter = article whose KB should be searched for
		// empty questionnaires
		String articleName = getParameter(0);
		// get the KB of this article
		KnowledgeBase kb = D3webModule.getAD3webKnowledgeServiceInTopic(
				KnowWEEnvironment.DEFAULT_WEB, articleName);
		if (kb != null) {
			List<String> emptyQASets = new ArrayList<String>();
			// iterate over QAsets and check if they are empty
			for (QASet qaset : kb.getQASets()) {
				if (!qaset.isQuestionOrHasQuestions()) {
					emptyQASets.add(qaset.getName());
				}
			}
			if (emptyQASets.size() > 0) {// empty QASets were found:
				String failedMessage = "Article '" + articleName +
						"' has empty questionnaires: " +
						createHTMLListFromStringList(emptyQASets);
				return new CITestResult(TestResultType.FAILED, failedMessage);
			}
		}
		// it seems everything was fine:
		return new CITestResult(TestResultType.SUCCESSFUL);
	}

	private String createHTMLListFromStringList(List<String> list) {
		StringBuilder htmlList = new StringBuilder();
		htmlList.append("<ul>");
		for (String listItem : list) {
			htmlList.append("<li>");
			htmlList.append(listItem);
			htmlList.append("</li>");
		}
		htmlList.append("</ul>");
		return htmlList.toString();
	}
}

