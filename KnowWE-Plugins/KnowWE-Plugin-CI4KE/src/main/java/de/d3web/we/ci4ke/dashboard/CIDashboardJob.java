/*
 * Copyright (C) 2023 denkbares GmbH, Germany
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

package de.d3web.we.ci4ke.dashboard;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.d3web.we.ci4ke.build.CIBuildManager;

/**
 * @author Stefan Olbrecht (Service Mate GmbH)
 * @created 13.11.2023
 */
public class CIDashboardJob implements Job {
	private static final Logger LOGGER = LoggerFactory.getLogger(CIDashboardJob.class);

	public static final String DASHBOARD_KEY = "dashboard";

	@SuppressWarnings("unused")
	public CIDashboardJob() {
		// job interface needs public no-argument constructor to work
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Object object = context.getJobDetail().getJobDataMap().get(DASHBOARD_KEY);
		if (object instanceof CIDashboard dashboard) {
			LOGGER.info("Starting scheduled build of dashboard: {}", dashboard.getDashboardName());
			CIBuildManager.getInstance().startBuild(dashboard);
		}
	}
}
