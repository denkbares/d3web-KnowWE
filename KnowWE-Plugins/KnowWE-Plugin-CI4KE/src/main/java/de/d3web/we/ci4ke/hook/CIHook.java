package de.d3web.we.ci4ke.hook;

import java.util.Collection;

import de.d3web.we.ci4ke.dashboard.CIDashboard;

public class CIHook {

	private final Collection<String> monitoredArticles;
	private final CIDashboard dashboard;

	public static final String CI_HOOK_STORE_KEY = "CIHook_Section_Store";

	private int lastTrigger = - 1;

	public Collection<String> getMonitoredArticles() {
		return monitoredArticles;
	}

	public CIDashboard getDashboard() {
		return this.dashboard;
	}

	public CIHook(CIDashboard dashboard, Collection<String> monitoredArticles) {

		this.dashboard = dashboard;
		this.monitoredArticles = monitoredArticles;
	}

	public int getLastTrigger() {
		return lastTrigger;
	}

	public void setLastTrigger(int lastTrigger) {
		this.lastTrigger = lastTrigger;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " (" + dashboard + "): " + monitoredArticles;
	}
}
