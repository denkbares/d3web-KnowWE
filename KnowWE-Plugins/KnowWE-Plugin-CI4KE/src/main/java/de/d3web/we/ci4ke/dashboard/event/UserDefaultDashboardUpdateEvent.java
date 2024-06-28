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

package de.d3web.we.ci4ke.dashboard.event;

import java.util.Set;

import com.denkbares.events.Event;

/**
 * Update notifying about changes in the default/preferred dashboards of a user
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 28.06.2024
 */
public class UserDefaultDashboardUpdateEvent implements Event {

	private final Set<String> dashboardNames;
	private final String userName;

	public UserDefaultDashboardUpdateEvent(Set<String> dashboardNames, String userName) {

		this.dashboardNames = dashboardNames;
		this.userName = userName;
	}

	public Set<String> getDashboardNames() {
		return dashboardNames;
	}

	public String getUserName() {
		return userName;
	}
}
