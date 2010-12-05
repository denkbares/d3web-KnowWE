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

import de.d3web.empiricaltesting.TestSuite;
import de.d3web.we.ci4ke.testing.AbstractCITest;
import de.d3web.we.ci4ke.testing.CITestResult;
import de.d3web.we.ci4ke.testing.CITestResult.TestResultType;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.testcase.TestCaseUtils;

public class TestsuiteRunner extends AbstractCITest {

	@Override
	public CITestResult call() {

		if (!checkIfParametersAreSufficient(1)) {
			return numberOfParametersNotSufficientError(1);
		}
		String monitoredArticleTitle = getParameter(0);

		TestSuite suite = TestCaseUtils.loadTestSuite(
				monitoredArticleTitle, KnowWEEnvironment.DEFAULT_WEB);

		if (suite != null) {
			if (!suite.isConsistent()) {
				return new CITestResult(TestResultType.FAILED, "Testsuite is not consistent!");
			}
			else if (suite.totalRecall() == 1.0 && suite.totalPrecision() == 1.0) {
				return new CITestResult(TestResultType.SUCCESSFUL, "Testsuite passed!");
			}
			else {
				return new CITestResult(TestResultType.FAILED,
						"Testsuite failed! (Total Precision: " + suite.totalPrecision() +
								", Total Recall: " + suite.totalRecall() + ")");
			}
		}
		else {
			return new CITestResult(TestResultType.ERROR,
					"Error while retrieving Testsuite from Article '" +
							monitoredArticleTitle + "' !");
		}

	}

}
