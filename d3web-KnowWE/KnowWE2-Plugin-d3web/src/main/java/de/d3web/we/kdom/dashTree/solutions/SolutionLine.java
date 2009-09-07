package de.d3web.we.kdom.dashTree.solutions;

import de.d3web.we.kdom.TextLine;
import de.d3web.we.kdom.dashTree.Tilde;
import de.d3web.we.kdom.decisionTree.SolutionID;

public class SolutionLine extends TextLine {

	@Override
	protected void init() {
		childrenTypes.add(new SolutionID());
		childrenTypes.add(new RootSolution());
		childrenTypes.add(new Tilde());
		childrenTypes.add(new SolutionDescription());
	}
	
}