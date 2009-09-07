package de.d3web.we.kdom.visitor;

import java.util.List;

import de.d3web.we.kdom.PlainText;
import de.d3web.we.kdom.Section;

public class CreateTextVisitor implements Visitor {
	
	private static CreateTextVisitor instance;
	
	public static synchronized  CreateTextVisitor getInstance() {
		if (instance == null) {
			instance = new CreateTextVisitor();
			
		}

		return instance;
	}
	
	/**
	 * prevent cloning
	 */
	 @Override
	public Object clone()
		throws CloneNotSupportedException
	  {
	    throw new CloneNotSupportedException(); 	   
	  }
	
	private StringBuffer buffi;

	@Override
	public void visit(Section s) {
		buffi = new StringBuffer();
		renderSubtree(s, buffi);

	}

	public String getText() {
		return buffi.toString();
	}

	private void renderSubtree(Section s, StringBuffer buffi) {
		if (s.getObjectType() instanceof PlainText) {
			buffi.append(s.getOriginalText());
		}
		List<Section> children = s.getChildren();

		for (Section section : children) {
			renderSubtree(section, buffi);
		}

	}

}
