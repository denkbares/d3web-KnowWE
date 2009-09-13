/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.knowledgeExporter;

public class KnowledgeExporterStatus {
	
	private int numberOfJobs = 0;
	
	private int completedJobs = 0;
	
	private String statusName = "";
	
	private static final String start = "start";
	
	private static final String done = "done";
	
	public int getNumberOfJobs() {
		return this.numberOfJobs;
	}
	
	public void setNumberOfJobs(int i) {
		this.numberOfJobs = i;
	}
	
	public void setCompletedJobsCount(int i) {
		this.completedJobs = i;
	}
	
	public void setStatusName(String name) {
		this.statusName = name;
	}
	
	public String getStatusName() {
		if (completedJobs == 0 && numberOfJobs != 0) {
			return this.start;
		} else if (completedJobs >= numberOfJobs) {
			return this.done;
		} else {
			return this.statusName;
		}
	}
	
	
	public int getCompletedJobsCount() {
		return this.completedJobs;
	}
	
	public void reset() {
		numberOfJobs = 0;
		completedJobs = 0;
		statusName = "";
	}

}
