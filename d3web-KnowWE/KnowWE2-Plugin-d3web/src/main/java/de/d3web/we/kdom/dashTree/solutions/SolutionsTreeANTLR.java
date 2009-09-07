package de.d3web.we.kdom.dashTree.solutions;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.kopic.renderer.DefaultLineNumberDeligateRenderer;

public class SolutionsTreeANTLR extends DefaultAbstractKnowWEObjectType{

	@Override
	protected void init() {
		this.sectionFinder = new SolutionsKDOMANTLRFinder(this);
		childrenTypes.add(new RootSolution());
		childrenTypes.add(new SolutionLine());
		childrenTypes.add(new SolutionID());
		this.setCustomRenderer(new DefaultLineNumberDeligateRenderer());
	}

}
