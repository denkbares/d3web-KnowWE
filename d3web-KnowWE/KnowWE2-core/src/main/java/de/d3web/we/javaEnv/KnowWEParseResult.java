package de.d3web.we.javaEnv;

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
		if(r != null) reportList.add(r);
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
			if(r.getErrorCount() > 0) {
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
