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

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import de.d3web.we.ci4ke.handling.CITestResult;

/**
 * An instance of this class holds the result of a ci build
 * 
 * @author Marc-Oliver Ochlast
 */
public final class CIBuildResultset {

	/**
	 * The ID of the dashboard for which the build was made
	 */
	// private String dashboardID;

	/**
	 * time/date of build execution
	 */
	private Date buildExecutionDate;

	/**
	 * the version number of the article which was testet
	 */
	private int articleVersion;

	/**
	 * A Map of the names and results of the executed Tests.
	 */
	private Map<String, CITestResult> results;

	public CIBuildResultset() {
		super();
		this.articleVersion = -1;
		this.buildExecutionDate = new Date();
		this.results = new TreeMap<String, CITestResult>();
	}

	@Deprecated
	public CIBuildResultset(
			// String dashboardID,
			Date buildExecutionDate,
			Map<String, CITestResult> results) {
		super();
		// this.dashboardID = dashboardID;
		this.buildExecutionDate = buildExecutionDate;
		this.results = results;
	}

	@Deprecated
	public CIBuildResultset(
			// String dashboardID,
			Map<String, CITestResult> results) {
		super();
		// this.dashboardID = dashboardID;
		this.buildExecutionDate = new Date();
		this.results = results;
	}

	// GETTERS

	// public String getDashboardID() {
	// return dashboardID;
	// }

	public Date getBuildExecutionDate() {
		return buildExecutionDate;
	}

	public Map<String, CITestResult> getResults() {
		return results;
	}

	// MODIFIERS

	public void addTestResult(String testname, CITestResult testResult) {
		this.results.put(testname, testResult);
	}

	public int getArticleVersion() {
		return articleVersion;
	}

	public void setArticleVersion(int articleVersion) {
		this.articleVersion = articleVersion;
	}
}
