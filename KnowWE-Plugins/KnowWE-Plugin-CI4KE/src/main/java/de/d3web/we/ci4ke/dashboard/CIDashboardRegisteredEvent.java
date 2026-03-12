package de.d3web.we.ci4ke.dashboard;

import com.denkbares.events.Event;

/**
 * Fired after a {@link CIDashboard} has been fully created and registered
 * in the {@link CIDashboardManager}.
 */
public class CIDashboardRegisteredEvent implements Event {

	private final CIDashboard dashboard;

	public CIDashboardRegisteredEvent(CIDashboard dashboard) {
		this.dashboard = dashboard;
	}

	public CIDashboard getDashboard() {
		return dashboard;
	}
}
