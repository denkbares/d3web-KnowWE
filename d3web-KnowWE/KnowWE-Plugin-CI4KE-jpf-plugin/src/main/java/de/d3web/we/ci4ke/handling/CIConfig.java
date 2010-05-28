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

import java.util.Arrays;
import java.util.Collection;

import de.d3web.we.ci4ke.build.CIBuilder.CIBuildTriggers;

public class CIConfig {

	public static String CICONFIG_STORE_KEY = "CIConfig_Section_Store";

	private final String dashboardID;
	private final String monitoredArticleTitle;
	private final String dashboardArticleTitle;
	private final Collection<String> testNames;
	private final CIBuildTriggers trigger;

	public CIConfig(String dashboardID, String monitoredArticle, String dashboardArticle,
			Collection<String> testNames, CIBuildTriggers trigger) {
		super();
		this.dashboardID = dashboardID;
		this.monitoredArticleTitle = monitoredArticle;
		this.dashboardArticleTitle = dashboardArticle;
		this.testNames = testNames;
		this.trigger = trigger;
	}

	public CIConfig(String dashboardID, String monitoredArticle, String dashboardArticle,
			String testNames, CIBuildTriggers trigger) {
		super();
		this.dashboardID = dashboardID;
		this.monitoredArticleTitle = monitoredArticle;
		this.dashboardArticleTitle = dashboardArticle;
		this.testNames = Arrays.asList(testNames.split(":"));
		this.trigger = trigger;
	}

	public String getDashboardID() {
		return dashboardID;
	}

	public String getMonitoredArticleTitle() {
		return monitoredArticleTitle;
	}

	public String getDashboardArticleTitle() {
		return dashboardArticleTitle;
	}

	public Collection<String> getTestNames() {
		return testNames;
	}

	public CIBuildTriggers getTrigger() {
		return trigger;
	}
}
