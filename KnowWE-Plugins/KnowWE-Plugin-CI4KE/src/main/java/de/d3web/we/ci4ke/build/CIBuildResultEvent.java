package de.d3web.we.ci4ke.build;

import com.denkbares.events.Event;
import de.d3web.testing.BuildResult;
import de.d3web.we.ci4ke.dashboard.CIDashboard;

/**
 * This event is fired after a {@link CIDashboard} finishes a {@link BuildResult}.
 */
public class CIBuildResultEvent implements Event {

	private final CIDashboard dashboard;
	private final BuildResult result;

	CIBuildResultEvent(CIDashboard dashboard, BuildResult result) {
		this.dashboard = dashboard;
		this.result = result;
	}

	public CIDashboard getDashboard() {
		return dashboard;
	}

	public BuildResult getResult() {
		return result;
	}
}
