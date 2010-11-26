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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.ci4ke.handling.AbstractCITest;
import de.d3web.we.ci4ke.handling.CITestResult;
import de.d3web.we.ci4ke.handling.CITestResult.TestResultType;
import de.d3web.we.core.KnowWEEnvironment;


/**
 * 
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 26.11.2010
 */
public class EmptyQuestionnaireTest extends AbstractCITest {

	@Override
	public CITestResult call() throws Exception {

		if (!checkIfParametersAreSufficient(1)) {
			return numberOfParametersNotSufficientError(1);
		}
		String articleName = getParameter(0);
		KnowledgeBase kb = D3webModule.getAD3webKnowledgeServiceInTopic(
				KnowWEEnvironment.DEFAULT_WEB, articleName);
		for (QASet qaset : kb.getQASets()) {
			if (!qaset.isQuestionOrHasQuestions()) {
				return new CITestResult(TestResultType.FAILED,
						"Article " + articleName + " has empty questionnaires!");
			}
		}
		return new CITestResult(TestResultType.SUCCESSFUL);
	}

}

