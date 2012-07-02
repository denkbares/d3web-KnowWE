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

import java.util.Collection;
import java.util.List;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.session.Value;
import de.d3web.empiricaltesting.RatedTestCase;
import de.d3web.empiricaltesting.SequentialTestCase;
import de.d3web.empiricaltesting.TestCase;
import de.d3web.empiricaltesting.caseAnalysis.RTCDiff;
import de.d3web.empiricaltesting.caseAnalysis.ValueDiff;
import de.d3web.empiricaltesting.caseAnalysis.functions.Diff;
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
			String messageText = "Test '" + testObject.getName() + "' failed: \n";
			List<SequentialTestCase> repository = testObject.getRepository();
			for (SequentialTestCase sequentialTestCase : repository) {
				if (result.hasDiff(sequentialTestCase)) {
					Diff diff = result.getDiffFor(sequentialTestCase);
					Collection<RatedTestCase> casesWithDifference = diff.getCasesWithDifference();
					for (RatedTestCase ratedTestCase : casesWithDifference) {
						if (diff.hasDiff(ratedTestCase)) {
							RTCDiff rtcDiff = diff.getDiff(ratedTestCase);
							Collection<TerminologyObject> diffObjects = rtcDiff.getDiffObjects();
							for (TerminologyObject terminologyObject : diffObjects) {
								ValueDiff valueDiff = rtcDiff.getDiffFor(terminologyObject);
								Value expected = valueDiff.getExpected();
								Value derived = valueDiff.getDerived();
								messageText += "* Value of object '" + terminologyObject.toString()
										+ "' was '" + derived + "' but expected was: '" + expected
										+ "'; \n";
							}
						}
					}
				}
			}
			return new Message(Type.FAILURE, messageText);
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
