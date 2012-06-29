/*
 * Copyright (C) 2012 denkbares GmbH
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

import de.d3web.empiricaltesting.TestCase;
import de.d3web.empiricaltesting.caseAnalysis.functions.TestCaseAnalysis;
import de.d3web.empiricaltesting.caseAnalysis.functions.TestCaseAnalysisReport;
import de.d3web.testing.Message;
import de.d3web.testing.Message.Type;

/**
 * A simple test to execute test cases.
 * 
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 29.06.2012
 */
public class TestCaseTest extends AbstractTest<TestCase> {

	@Override
	public Message execute(TestCase testObject, String[] args) {
		if (!testObject.isConsistent()) {
			return new Message(Type.FAILURE, "Test is not consistent!");
		}
		TestCaseAnalysis analysis = new TestCaseAnalysis();
		TestCaseAnalysisReport result = analysis.runAndAnalyze(testObject);
		if (result.hasDiff()) {
			return new Message(Type.FAILURE, "Test failed");
		}
		return new Message(Type.SUCCESS);
	}

	@Override
	public Class<TestCase> getTestObjectClass() {
		return TestCase.class;
	}

	@Override
	public int numberOfArguments() {
		return 0;
	}

}
