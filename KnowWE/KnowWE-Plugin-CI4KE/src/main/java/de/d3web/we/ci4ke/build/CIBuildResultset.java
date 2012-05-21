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

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.d3web.we.ci4ke.testing.CITestResult;
import de.d3web.we.ci4ke.testing.CITestResult.Type;

/**
 * An instance of this class holds the result of a ci build
 * 
 * @author Marc-Oliver Ochlast
 */
public final class CIBuildResultset {

	/**
	 * List of all test results of the executed tests
	 */
	private final List<CITestResult> results = new LinkedList<CITestResult>();

	/**
	 * The number of this build
	 */
	private final int buildNumber;

	/**
	 * time/date of build execution
	 */
	private final Date buildDate;

	/**
	 * Duration how long this build has taken
	 */
	private long buildDuration = 0;

	public CIBuildResultset(int buildNumber) {
		this(buildNumber, new Date());
	}

	public CIBuildResultset(int buildNumber, Date buildDate) {
		this.buildNumber = buildNumber;
		this.buildDate = buildDate;
	}

	/**
	 * Returns the duration this build has required to be performed, given in
	 * milliseconds.
	 * 
	 * @created 03.02.2012
	 * @return in milliseconds
	 */
	public long getBuildDuration() {
		return buildDuration;
	}

	/**
	 * Sets the duration this build has required to be performed, given in
	 * milliseconds.
	 * 
	 * @created 03.02.2012
	 * @param timeSpentForBuild in milliseconds
	 */
	public void setBuildDuration(long timeSpentForBuild) {
		this.buildDuration = timeSpentForBuild;
	}

	/**
	 * Returns the date this build has been started.
	 * 
	 * @created 19.05.2012
	 * @return the build start time
	 */
	public Date getBuildDate() {
		return buildDate;
	}

	/**
	 * Returns the list of all test results of this build.
	 * 
	 * @created 19.05.2012
	 * @return the results of this build
	 */
	public List<CITestResult> getResults() {
		return Collections.unmodifiableList(results);
	}

	/**
	 * Computes the overall TestResultType of this resultset, determined by the
	 * "worst" Testresult
	 * 
	 * @created 03.06.2010
	 * @return
	 */
	public Type getOverallResult() {

		Type overallResult = Type.SUCCESSFUL;
		for (CITestResult testResult : results) {
			if (testResult != null && testResult.getType().
					compareTo(overallResult) > 0) {
				overallResult = testResult.getType();
			}
		}
		return overallResult;
	}

	public void addTestResult(CITestResult testResult) {
		results.add(testResult);
	}

	public int getBuildNumber() {
		return buildNumber;
	}
}
