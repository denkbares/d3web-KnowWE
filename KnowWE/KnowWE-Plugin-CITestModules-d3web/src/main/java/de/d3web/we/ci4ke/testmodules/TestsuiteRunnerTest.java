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

import cc.denkbares.testing.ArgsCheckResult;
import cc.denkbares.testing.Message;
import cc.denkbares.testing.Message.Type;
import cc.denkbares.testing.Test;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.empiricaltesting.TestCase;
import de.d3web.empiricaltesting.caseAnalysis.functions.TestCaseAnalysis;
import de.d3web.empiricaltesting.caseAnalysis.functions.TestCaseAnalysisReport;
import de.d3web.we.testcase.TestCaseUtils;
import de.knowwe.core.Environment;

public class TestsuiteRunnerTest implements Test<KnowledgeBase> {

	@Override
	public cc.denkbares.testing.Message execute(KnowledgeBase testObject, String[] args) {
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
		else if (result.recall() == 1.0 && result.precision() == 1.0) {
			return new Message(Type.SUCCESS, null);
		}
		else {
			return new Message(Type.FAILURE,
						"Testsuite failed! (Total Precision: " + result.precision() +
								", Total Recall: " + result.recall() + ")");
		}
	}

	@Override
	public ArgsCheckResult checkArgs(String[] args) {
		if (args.length == 1) return new ArgsCheckResult(ArgsCheckResult.Type.FINE);
		return new ArgsCheckResult(ArgsCheckResult.Type.ERROR);
	}

	@Override
	public Class<KnowledgeBase> getTestObjectClass() {
		return KnowledgeBase.class;
	}

}
