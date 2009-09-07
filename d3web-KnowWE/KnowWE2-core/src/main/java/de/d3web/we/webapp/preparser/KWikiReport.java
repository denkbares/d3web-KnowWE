package de.d3web.we.webapp.preparser;

import de.d3web.report.Report;

public class KWikiReport extends Report {
	private String clusterID = null;
	
	private boolean safeMode = false;
	private boolean solutionSafeMode = false;
	
	

	public boolean isSolutionSafeMode() {
		return solutionSafeMode;
	}

	public void setSolutionSafeMode(boolean solutionSafeMode) {
		this.solutionSafeMode = solutionSafeMode;
	}

	public boolean isSafeMode() {
		return safeMode;
	}

	public void setSafeMode(boolean safeMode) {
		this.safeMode = safeMode;
	}

	public String getClusterID() {
		return clusterID;
	}

	public void setClusterID(String clusterID) {
		this.clusterID = clusterID;
	}
	
	
	
	
}
