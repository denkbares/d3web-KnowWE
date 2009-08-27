package de.d3web.knowledgeExporter.testutils;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.report.Report;

public class KnowledgeBaseAndReport {

	private Report report  = new Report(); 
	private KnowledgeBase kb; 
	
	public KnowledgeBaseAndReport(KnowledgeBase kb, Report report) {
		this.kb = kb; 
		this.report = report; 
	}
	
	public KnowledgeBase getKnowledgeBase(){
		return kb; 
	}
	
	public Report getReport(){
		return report; 
	}
}
