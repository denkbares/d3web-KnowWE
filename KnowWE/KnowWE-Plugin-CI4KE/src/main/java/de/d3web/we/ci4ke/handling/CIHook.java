package de.d3web.we.ci4ke.handling;

import java.util.Collection;

public class CIHook {

	private final String web;
	private final String dashboardArticleTitle;
	private final String dashboardID;
	private final Collection<String> monitoredArticles;

	public static final String CIHOOK_STORE_KEY = "CIHook_Section_Store";

	public Collection<String> getMonitoredArticles() {
		return monitoredArticles;
	}

	public String getWeb() {
		return web;
	}

	public String getDashboardArticleTitle() {
		return dashboardArticleTitle;
	}

	public String getDashboardName() {
		return dashboardID;
	}

	public CIHook(String web, String dashboardArticleTitle,
			String dashboardName, Collection<String> monitoredArticles) {

		if (web == null || web.isEmpty()) {
			throw new IllegalArgumentException("web is null or empty!");
		}
		if (dashboardArticleTitle == null || dashboardArticleTitle.isEmpty()) {
			throw new IllegalArgumentException("dashboardArticleTitle is null or empty!");
		}
		if (dashboardName == null || dashboardName.isEmpty()) {
			throw new IllegalArgumentException("dashboardID is null or empty!");
		}
		if (monitoredArticles == null || monitoredArticles.isEmpty()) {
			throw new IllegalArgumentException("monitoredArticles are null or empty!");
		}

		this.web = web;
		this.dashboardArticleTitle = dashboardArticleTitle;
		this.dashboardID = dashboardName;
		this.monitoredArticles = monitoredArticles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((dashboardArticleTitle == null) ? 0
						: dashboardArticleTitle.hashCode());
		result = prime * result
				+ ((dashboardID == null) ? 0 : dashboardID.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		CIHook other = (CIHook) obj;
		if (dashboardArticleTitle == null) {
			if (other.dashboardArticleTitle != null) return false;
		}
		else if (!dashboardArticleTitle
				.equals(other.dashboardArticleTitle)) return false;
		if (dashboardID == null) {
			if (other.dashboardID != null) return false;
		}
		else if (!dashboardID.equals(other.dashboardID)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "CIHook [dashboardArticleTitle=" + dashboardArticleTitle
				+ ", dashboardID=" + dashboardID + "]";
	}
}
