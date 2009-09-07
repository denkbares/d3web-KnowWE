package de.d3web.we.d3webModule;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.report.Report;
import de.d3web.we.javaEnv.KnowWEParseResult;

public class KopicParseResult extends KnowWEParseResult{
	
	//private KnowledgeBase kb;
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

	public KopicParseResult(Map<String,Report>  reportMap, Collection generatedItems, KnowledgeBaseManagement kbm, String topicName, String text) {
		super(null,topicName,text);
		//this.kb = kbm.getKnowledgeBase();
		this.reportMap = reportMap;
		this.generatedItems = generatedItems;
		this. kbm = kbm;
	}

	public KnowledgeBase getKb() {
		return kbm.getKnowledgeBase();
	}

	public Map<String,Report> getReportMap() {
		return reportMap;
	}
	
	@Override
	public boolean hasErrors() {
		for (Iterator iter = reportMap.values().iterator(); iter.hasNext();) {
			Report element = (Report) iter.next();
			if(element.getErrorCount() > 0) {
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
		for(Report report : reportMap.values()) {
			if(report.getErrorCount() > 0) {
				firstError = "<span class=\"red\">"+report.getErrors().get(0).getMessageText()+"</span>";
				break;
			}
		}
		if(firstError == null) {
			return "<span class=\"green\">no errors</span>";
		}
		return firstError;
	}

}
