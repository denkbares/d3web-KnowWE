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

package de.d3web.we.d3webModule;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.report.Report;
import de.d3web.we.core.KnowWEParseResult;

public class KopicParseResult extends KnowWEParseResult {

	// private KnowledgeBase kb;
	private Map<String, Report> reportMap;
	private Collection generatedItems;
	private KnowledgeBaseManagement kbm;
	private String clusterID;

	public String getClusterID() {
		return clusterID;
	}

	public void setClusterID(String clusterID) {
		this.clusterID = clusterID;
	}

	public KopicParseResult(Map<String, Report> reportMap, Collection generatedItems, KnowledgeBaseManagement kbm, String topicName, String text) {
		super(null, topicName, text);
		// this.kb = kbm.getKnowledgeBase();
		this.reportMap = reportMap;
		this.generatedItems = generatedItems;
		this.kbm = kbm;
	}

	public KnowledgeBase getKb() {
		return kbm.getKnowledgeBase();
	}

	public Map<String, Report> getReportMap() {
		return reportMap;
	}

	@Override
	public boolean hasErrors() {
		for (Iterator iter = reportMap.values().iterator(); iter.hasNext();) {
			Report element = (Report) iter.next();
			if (element.getErrorCount() > 0) {
				return true;
			}

		}
		return false;
	}

	public int errorCnt() {
		int cnt = 0;
		for (Iterator iter = reportMap.values().iterator(); iter.hasNext();) {
			Report element = (Report) iter.next();
			cnt += element.getErrorCount();

		}
		return cnt;
	}

	public Collection getGeneratedItems() {
		return generatedItems;
	}

	public KnowledgeBaseManagement getKbm() {
		return kbm;
	}

	public String generateShortStatus() {

		String firstError = null;
		for (Report report : reportMap.values()) {
			if (report.getErrorCount() > 0) {
				firstError = "<span class=\"red\">" + report.getErrors().get(0).getMessageText()
						+ "</span>";
				break;
			}
		}
		if (firstError == null) {
			return "<span class=\"green\">no errors</span>";
		}
		return firstError;
	}

}
