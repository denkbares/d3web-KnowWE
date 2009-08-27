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
