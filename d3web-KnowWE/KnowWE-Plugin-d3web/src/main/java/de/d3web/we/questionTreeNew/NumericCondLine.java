package de.d3web.we.questionTreeNew;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;

public class NumericCondLine extends DefaultAbstractKnowWEObjectType {

	
	@Override
	protected void init() {
		this.sectionFinder = new ConditionalAllTextFinder() {
			
			@Override
			protected boolean condition(String text, Section father) {
				return (text.startsWith("[") && text.endsWith("]")) || text.startsWith("<") || text.startsWith(">") || text.startsWith("=") ;
			}
		};
		
		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));
	}
}
