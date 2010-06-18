package de.d3web.we.kdom.questionTreeNew;

import de.d3web.we.kdom.dashTree.DashTree;

public class QuestionDashTree extends DashTree {

	public QuestionDashTree() {
		super();
		this.setCustomRenderer(null);
		this.replaceDashTreeElementContentType(this, new QuestionDashTreeElementContent());
	}
}
