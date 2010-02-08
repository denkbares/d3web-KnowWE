package de.d3web.we.kdom.questionTreeNew;

import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;

public class QuestionTreeRootType extends QuestionTree{
	
	private static DefaultMarkup m = null; 
	
	static {
		m = new DefaultMarkup("QuestionTree");
		m.addContentType(new QuestionDashTree());
	}
	
	public QuestionTreeRootType() {
		super(m);
	}
	

}
