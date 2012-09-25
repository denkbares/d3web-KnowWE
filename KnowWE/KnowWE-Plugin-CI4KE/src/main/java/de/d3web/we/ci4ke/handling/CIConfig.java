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

package de.d3web.we.ci4ke.handling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.d3web.testing.TestSpecification;
import de.d3web.we.ci4ke.handling.CIDashboardType.CIBuildTriggers;

public final class CIConfig implements Cloneable {

	public static final String CICONFIG_STORE_KEY = "CIConfig_Section_Store";

	public static final CIConfig DUMMY_CONFIG = new CIConfig("", "", "",
			new ArrayList<TestSpecification<?>>(), CIBuildTriggers.onDemand);

	private final String web;
	private final String dashboardArticleTitle;
	private final String dashboardName;

	// private final Map<String, List<String>> tests;
	private final List<TestSpecification<?>> tests;

	private final CIBuildTriggers trigger;

	public CIConfig(String web, String dashboardArticle, String dashboardName,
			List<TestSpecification<?>> tests, CIBuildTriggers trigger) {
		this.web = web;
		this.dashboardArticleTitle = dashboardArticle;
		this.dashboardName = dashboardName;
		this.tests = Collections.unmodifiableList(tests);
		this.trigger = trigger;
	}

	public String getWeb() {
		return web;
	}

	public String getDashboardName() {
		return dashboardName;
	}

	public String getDashboardArticleTitle() {
		return dashboardArticleTitle;
	}

	public List<TestSpecification<?>> getTestSpecifications() {
		return tests;
	}

	public List<String> getTestNames() {
		List<String> testNames = new ArrayList<String>(tests.size());
		for (TestSpecification<?> testAndParameters : tests) {
			testNames.add(testAndParameters.getTest().getClass().getSimpleName());
		}
		return testNames;
	}

	public CIBuildTriggers getTrigger() {
		return trigger;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		CIConfig c = new CIConfig(
				this.web,
				this.dashboardArticleTitle,
				this.dashboardName,
				this.tests,
				this.trigger);
		return c;
	}

}
