package de.d3web.we.ci4ke.diff;

public interface DiffInterface {

	/**
	 * Computes a diff between the old and the new wiki text and returns
	 * that diff in valid XHTML.
	 * @param oldWikiText
	 * @param newWikiText
	 * @return
	 */
	public String makeDiffHtml(String oldWikiText, String newWikiText);
	
}
