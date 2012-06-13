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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.empiricaltesting.TestCase;
import de.d3web.empiricaltesting.caseAnalysis.functions.TestCaseAnalysis;
import de.d3web.empiricaltesting.caseAnalysis.functions.TestCaseAnalysisReport;
import de.d3web.testing.Message;
import de.d3web.testing.Message.Type;
import de.d3web.we.testcase.TestCaseUtils;
import de.knowwe.core.Environment;

public class TestsuiteRunnerTest extends AbstractTest<KnowledgeBase> {

	@Override
	public de.d3web.testing.Message execute(KnowledgeBase testObject, String[] args) {
		String monitoredArticleTitle = args[0];

		TestCase suite = TestCaseUtils.loadTestSuite(
				monitoredArticleTitle, Environment.DEFAULT_WEB);

		if (suite == null) {
			return new Message(Type.ERROR, "No Testsuite found in article '"
					+ monitoredArticleTitle + "'");
		}

		TestCaseAnalysis analysis = new TestCaseAnalysis();
		TestCaseAnalysisReport result = analysis.runAndAnalyze(suite);

		if (!suite.isConsistent()) {
			return new Message(Type.FAILURE, "Testsuite is not consistent!");
		}
		else {
			double precision = result.precision();
			double recall = result.recall();

			if (recall == 1.0 && precision == 1.0) {
				return new Message(Type.SUCCESS, null);
			}
			else {
				String precisionRounded = "" + ((double) Math.round(precision * 100)) / 100;
				String recallRounded = "" + ((double) Math.round(recall * 100)) / 100;
				return new Message(Type.FAILURE,
							"Testsuite failed! (Total Precision: " + precisionRounded +
									", Total Recall: " + recallRounded + ")");
			}
		}
	}

	@Override
	public int numberOfArguments() {
		return 1;
	}

	@Override
	public Class<KnowledgeBase> getTestObjectClass() {
		return KnowledgeBase.class;
	}

}
