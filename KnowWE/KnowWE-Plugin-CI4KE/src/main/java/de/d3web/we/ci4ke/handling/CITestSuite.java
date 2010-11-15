/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.ci4ke.handling;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.ci4ke.build.CIBuildResultset;

/**
 * A Suite of CITests.
 * 
 * @author Marc-Oliver Ochlast
 * @created 03.06.2010
 */
public class CITestSuite implements CITest {

	private final List<CITest> testsSuite;

	public CITestSuite(List<CITest> tests) {
		this.testsSuite = new ArrayList<CITest>();
		this.testsSuite.addAll(tests);
	}

	@Override
	public void init(CIConfig config) {
		for (CITest test : testsSuite) {
			test.init(config);
		}
	}

	@Override
	public void setParameters(List<String> parameters) {
		for (CITest test : testsSuite) {
			test.setParameters(parameters);
		}
	}

	@Override
	public CITestResult call() throws Exception {

		CIBuildResultset results = new CIBuildResultset();
		for (CITest test : testsSuite) {// simple build execution
			results.addTestResult(test.getClass().getSimpleName(), test.call());
		}
		return new CITestResult(results.getOverallResult(), results.getTestresultMessages());
	}

}
