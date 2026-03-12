package de.d3web.we.ci4ke.dashboard;

import com.denkbares.events.Event;

/**
 * Fired after a {@link CIDashboard} has been unregistered
 * from the {@link CIDashboardManager}.
 */
public class CIDashboardUnregisteredEvent implements Event {

	private final CIDashboard dashboard;

	public CIDashboardUnregisteredEvent(CIDashboard dashboard) {
		this.dashboard = dashboard;
	}

	public CIDashboard getDashboard() {
		return dashboard;
	}
}
