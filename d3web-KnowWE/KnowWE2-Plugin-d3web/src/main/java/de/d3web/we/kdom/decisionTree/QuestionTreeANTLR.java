package de.d3web.we.kdom.decisionTree;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.kopic.QuestionTreeKDOMANTLRFinder;
import de.d3web.we.kdom.kopic.renderer.DefaultLineNumberDeligateRenderer;

public class QuestionTreeANTLR extends DefaultAbstractKnowWEObjectType{

	@Override
	protected void init() {
		this.sectionFinder = new QuestionTreeKDOMANTLRFinder(this);
		this.setCustomRenderer(new DefaultLineNumberDeligateRenderer());
	}

}
