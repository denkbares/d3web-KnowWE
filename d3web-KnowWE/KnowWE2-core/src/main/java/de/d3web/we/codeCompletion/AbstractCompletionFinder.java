package de.d3web.we.codeCompletion;


public abstract class AbstractCompletionFinder {

	public abstract CompletionFinding find(String termName, String data);
	
	/**
	 * Aufpassen of die groß-/klein-Differenzen bei der vervollständigung auch mit eingesetzt werden!
	 * Wenn nicht hat man nicht korrekte Terminologie nach vervollständigung!
	 *
	 */	
	protected boolean startsWithIgnoreCase(String str1, String str2) {
		if(str2.length() > str1.length()) return false;
		String prefix = str1.substring(0, str2.length());
		return str2.equalsIgnoreCase(prefix);
		
	}
}
