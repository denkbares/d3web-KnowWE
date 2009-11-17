package de.d3web.we.search;

public class GenericSearchResult {
	private String pagename;
	private String[] contexts;
	
	public GenericSearchResult(String page,String[] contexts,int score){
		this.pagename=page;
		this.contexts=contexts;
		this.score=score;
	}
	
	public String getPagename() {
		return pagename;
	}
	public void setPagename(String pagename) {
		this.pagename = pagename;
	}
	public String[] getContexts() {
		return contexts;
	}
	public void setContexts(String[] contexts) {
		this.contexts = contexts;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	private int score;

}
