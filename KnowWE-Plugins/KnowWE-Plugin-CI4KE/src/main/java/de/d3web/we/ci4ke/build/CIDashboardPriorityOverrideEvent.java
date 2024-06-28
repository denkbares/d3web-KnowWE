/*
 * Copyright (C) 2024 denkbares GmbH, Germany
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

package de.d3web.we.ci4ke.build;

import com.denkbares.events.Event;
import de.d3web.we.ci4ke.dashboard.CIDashboard;

/**
 * Event to temporarily change priority for the given dashboard for the next CI build
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 28.06.2024
 */
public class CIDashboardPriorityOverrideEvent implements Event {

	private final CIDashboard dashboard;
	private final double priority;

	public CIDashboardPriorityOverrideEvent(CIDashboard dashboard, double priority) {
		this.dashboard = dashboard;
		this.priority = priority;
	}

	public CIDashboard getDashboard() {
		return dashboard;
	}

	public double getPriority() {
		return priority;
	}
}
