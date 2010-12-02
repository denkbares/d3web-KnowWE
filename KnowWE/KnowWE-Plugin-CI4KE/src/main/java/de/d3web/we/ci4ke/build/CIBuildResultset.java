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

package de.d3web.we.ci4ke.build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import de.d3web.core.utilities.Pair;
import de.d3web.we.ci4ke.testing.CITestResult;
import de.d3web.we.ci4ke.testing.CITestResult.TestResultType;

/**
 * An instance of this class holds the result of a ci build
 * 
 * @author Marc-Oliver Ochlast
 */
public final class CIBuildResultset {

	/**
	 * time/date of build execution
	 */
	private final Date buildExecutionDate;

	/**
	 * A Map of the names and results of the executed Tests.
	 */
	// private final Map<String, CITestResult> results;

	private final List<Pair<String, CITestResult>> results;

	public CIBuildResultset() {
		super();
		this.buildExecutionDate = new Date();
		this.results = new ArrayList<Pair<String, CITestResult>>();
	}

	public Date getBuildExecutionDate() {
		return buildExecutionDate;
	}

	public List<Pair<String, CITestResult>> getResults() {
		return Collections.unmodifiableList(results);
	}

	/**
	 * Computes the overall TestResultType of this resultset, determined by the
	 * "worst" Testresult
	 * 
	 * @created 03.06.2010
	 * @return
	 */
	public TestResultType getOverallResult() {

		TestResultType overallResult = TestResultType.SUCCESSFUL;
		for (Pair<String, CITestResult> resultPair : results) {
			CITestResult testResult = resultPair.getB();
			if (testResult != null && testResult.getResultType().
					compareTo(overallResult) > 0) {
				overallResult = testResult.getResultType();
			}
		}
		return overallResult;
	}

	public String getTestresultMessages() {
		StringBuffer sb = new StringBuffer();
		for (Pair<String, CITestResult> resultPair : results) {
			String testName = resultPair.getA();
			CITestResult testResult = resultPair.getB();

			sb.append(testName + ": ");
			if (testResult.getTestResultMessage().length() > 0) {
				sb.append(testResult.getTestResultMessage());
			}
			else {
				sb.append("(no resultmessage)");
			}
			sb.append("\n<br/><br/>\n");
		}
		return sb.toString();
	}

	// MODIFIERS

	public void addTestResult(String testname, CITestResult testResult) {
		if (testname != null && !testname.isEmpty() && testResult != null) {
			results.add(new Pair<String, CITestResult>(testname, testResult));
		}
		else {
			throw new IllegalArgumentException("addTestResult() received illegal arguments!");
		}
	}
}
