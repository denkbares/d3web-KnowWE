package de.d3web.we.ci4ke.handling;

import java.util.Arrays;
import java.util.Collection;

import de.d3web.we.ci4ke.handling.CIDashboardType.CIBuildTriggers;

public class CIConfig {
	
//	public enum BUILD_TRIGGER_MODE {
//		ON_DEMAND,
//		ON_SAVE,
//		ON_TIME
//	}

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
