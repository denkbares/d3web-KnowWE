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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.we.ci4ke.handling.CIDashboardType.CIBuildTriggers;

public final class CIConfig implements Cloneable {

	public static final String CICONFIG_STORE_KEY = "CIConfig_Section_Store";

	public static final CIConfig DUMMY_CONFIG = new CIConfig("", "",
			new HashMap<String, List<String>>(), CIBuildTriggers.onDemand);

	private final String dashboardName;
	private final String dashboardArticleTitle;

	private final Map<String, List<String>> tests;
	private final CIBuildTriggers trigger;

	public CIConfig(String dashboardName, String dashboardArticle,
			Map<String, List<String>> tests, CIBuildTriggers trigger) {
		super();
		this.dashboardName = dashboardName;
		this.dashboardArticleTitle = dashboardArticle;
		this.tests = Collections.unmodifiableMap(tests);
		this.trigger = trigger;
	}

	public String getDashboardName() {
		return dashboardName;
	}

	public String getDashboardNameEscaped() {
		try {
			return URLEncoder.encode(dashboardName, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return "";
		}
	}

	public String getDashboardArticleTitle() {
		return dashboardArticleTitle;
	}

	public Map<String, List<String>> getTests() {
		return tests;
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
