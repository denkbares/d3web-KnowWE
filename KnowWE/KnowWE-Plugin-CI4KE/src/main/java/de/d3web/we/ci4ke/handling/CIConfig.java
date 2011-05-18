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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.d3web.we.ci4ke.handling.CIDashboardType.CIBuildTriggers;
import de.d3web.we.ci4ke.util.CIUtilities;
import de.d3web.we.ci4ke.util.Pair;

public final class CIConfig implements Cloneable {

	public static final String CICONFIG_STORE_KEY = "CIConfig_Section_Store";

	public static final CIConfig DUMMY_CONFIG = new CIConfig("", "",
			new ArrayList<Pair<String, List<String>>>(), CIBuildTriggers.onDemand);

	private final String dashboardName;
	private final String dashboardArticleTitle;

	// private final Map<String, List<String>> tests;
	private final List<Pair<String, List<String>>> tests;

	private final CIBuildTriggers trigger;

	public CIConfig(String dashboardName, String dashboardArticle,
			List<Pair<String, List<String>>> tests, CIBuildTriggers trigger) {
		super();
		this.dashboardName = dashboardName;
		this.dashboardArticleTitle = dashboardArticle;
		this.tests = Collections.unmodifiableList(tests);
		this.trigger = trigger;
	}

	public String getDashboardName() {
		return dashboardName;
	}

	/**
	 * Use {@link CIUtilities#utf8Escape(String)} instead!
	 * 
	 * @created 01.12.2010
	 * @return
	 */
	@Deprecated
	public String getDashboardNameEscaped() {
		try {
			return URLEncoder.encode(dashboardName, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return dashboardName;
		}
	}

	public String getDashboardArticleTitle() {
		return dashboardArticleTitle;
	}

	public List<Pair<String, List<String>>> getTests() {
		return tests;
	}

	public List<String> getTestNames() {
		List<String> testNames = new ArrayList<String>(tests.size());
		for (Pair<String, List<String>> testAndParameters : tests) {
			testNames.add(testAndParameters.getA());
		}
		return testNames;
	}

	public CIBuildTriggers getTrigger() {
		return trigger;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		CIConfig c = new CIConfig(this.dashboardName,
				this.dashboardArticleTitle,
				this.tests,
				this.trigger);
		return c;
	}

}
