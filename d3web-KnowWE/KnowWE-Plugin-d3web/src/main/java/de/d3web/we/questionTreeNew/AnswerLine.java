package de.d3web.we.questionTreeNew;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;

public class AnswerLine extends DefaultAbstractKnowWEObjectType{

	@Override
	protected void init() {
		this.sectionFinder = new ConditionalAllTextFinder() {
			
			@Override
			protected boolean condition(String text, Section father) {
				
				Section dashTreeElement = father.getFather();
				if(dashTreeElement.getObjectType() instanceof DashTreeElement) {
					Section<? extends DashTreeElement> dashFather = DashTreeElement.getDashTreeFather((Section<DashTreeElement>)dashTreeElement);
					if(dashFather != null && dashFather.findSuccessor(new QuestionLine()) != null) {
						return true;
					}
				}
				
				return false;
			}
		};
		
		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR6));
	}
	
}
