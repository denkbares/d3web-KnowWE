package de.d3web.we.kdom.questionTreeNew;

import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.questionTreeNew.dialog.QuestionTreeRootTypeDefaultRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;

public class QuestionTreeRootType extends QuestionTree{
	
	private static DefaultMarkup m = null; 
	
	static {
		m = new DefaultMarkup("QuestionTree");
		m.addContentType(new QuestionDashTree());
		m.addAnnotation("dialog", false);
	}
	
	public QuestionTreeRootType() {
		super(m);
	}
	@Override
	protected KnowWEDomRenderer<?> getDefaultRenderer() {
		return new QuestionTreeRootTypeDefaultRenderer();
	}
	

}
