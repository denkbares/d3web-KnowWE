package de.d3web.we.kdom.dashTree.questionnaires;

import de.d3web.we.kdom.kopic.renderer.DefaultLineNumberDeligateRenderer;



public class QuestionnairesTreeANTLR extends de.d3web.we.kdom.DefaultAbstractKnowWEObjectType{

	@Override
	protected void init() {
		this.sectionFinder = new QuestionnairesKDOMANTLRFinder(this);
		childrenTypes.add(new QuestionnaireLine());
		this.setCustomRenderer(new DefaultLineNumberDeligateRenderer());
	}

	
}
