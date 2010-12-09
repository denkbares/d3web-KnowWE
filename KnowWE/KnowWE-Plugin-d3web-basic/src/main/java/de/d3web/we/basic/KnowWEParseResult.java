/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.report.Report;

public class KnowWEParseResult {

	private List<Report> reportList = new ArrayList<Report>();
	private String topic;
	private String modifiedText;
	private String user;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public KnowWEParseResult(Report r, String topic, String modified) {
		this.topic = topic;
		this.modifiedText = modified;
		if (r != null) reportList.add(r);
	}

	public Collection<Report> getReportList() {
		return reportList;

	}

	public String getTopic() {
		return topic;
	}

	public void addReport(String keyName, Report r) {
		reportList.add(r);

	}

	public boolean hasErrors() {
		for (Report r : this.reportList) {
			if (r.getErrorCount() > 0) {
				return true;
			}
		}

		return false;
	}

	public String getModifiedText() {
		return modifiedText;
	}

	public void setModifiedText(String modifiedText) {
		this.modifiedText = modifiedText;
	}

}
