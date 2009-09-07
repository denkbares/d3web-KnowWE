package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.javaEnv.KnowWEParseResult;

public class KnowWEDomParseReport {
	
	private KnowWEArticle article;
	private List<KnowWEParseResult> reports;
	
	private String html = null;
	
	public KnowWEDomParseReport(KnowWEArticle a) {
		reports = new ArrayList<KnowWEParseResult>();
		this.article = a;
	}
	
	public String getShortStatus() {
		return null;
	}


	public void addReport(KnowWEParseResult r) {
		reports.add(r);
		
	}

	public KnowWEArticle getArticle() {
		return article;
	}

	public List<KnowWEParseResult> getReports() {
		return reports;
	}

	public boolean hasErrors() {
		//TODO : implement or remove
		return false;
	}
	
	public String getHTML() {
		if(html == null) {
			html = "TODO: implement";
		}
		return html;
	}

}
