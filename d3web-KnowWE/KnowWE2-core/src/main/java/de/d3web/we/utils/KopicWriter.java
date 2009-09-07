package de.d3web.we.utils;

public class KopicWriter {
	
	private StringBuffer textBuff = new StringBuffer();
	private static final String newLine = "\n";
	
	public KopicWriter() {
		textBuff.append(newLine);
		textBuff.append("<Kopic>");
		textBuff.append(newLine);
		textBuff.append(newLine);
	}
	
	public void appendSolutions(String text) {
		// TODO factor String out
		String tag = "Solutions-section";
		
		addContent(text,tag);
		
		
	}
	
	private void addContent(String text, String tag) {
		if(text == null || text.length() == 0) return;
		appendStartTag(tag);
		textBuff.append(text);
		appendEndTag(tag);
	}
	
	public void appendDecisionTable(String text) {
		// TODO factor String out
		String tag = "DecisionTableClassic-section";
		
		addContent(text,tag);
	}
	
	public void appendScoreTable(String text) {
		// TODO factor String out
		String tag = "DiagnosisScoreTable-section";
		
		addContent(text,tag);
	}
	
	public void appendCoveringTable(String text) {
		// TODO factor String out
		String tag = "SetCoveringTable-section";
		
		addContent(text,tag);
	}

	public void appendQuestions(String text) {
		// TODO factor String out
		String tag = "Questions-section";
		
		addContent(text,tag);
	}
	
	public void appendQuestionnaires(String text ) {
		// TODO factor String out
		String tag = "Questionnaires-section";
		
		addContent(text,tag);
	}
	
	public void appendCoveringLists(String text) {
		// TODO factor String out
		String tag = "SetCoveringList-section";
		
		
		addContent(text,tag);
	}

	public void appendRules(String text) {
		// TODO factor String out
		String tag = "Rules-section";
		
		addContent(text,tag);
	}
	
	public void appendConfig(String text) {
		// TODO factor String out
		String tag = "KBconfig-section";
		
		addContent(text,tag);
	}
	
	private void appendStartTag(String tagName) {
		textBuff.append("<");
		textBuff.append(tagName);
		textBuff.append(">");
		textBuff.append(newLine);
		textBuff.append(newLine);
	}
	
	private void appendEndTag(String tagName) {
		textBuff.append(newLine);
		textBuff.append("</");
		textBuff.append(tagName);
		textBuff.append(">");
		textBuff.append(newLine);
		textBuff.append(newLine);
	}
	
	public String getKopicText() {
		textBuff.append(newLine);
		textBuff.append(newLine);
		textBuff.append("</Kopic>");
		textBuff.append(newLine);
		textBuff.append(newLine);
		
		return textBuff.toString();
	}

}
