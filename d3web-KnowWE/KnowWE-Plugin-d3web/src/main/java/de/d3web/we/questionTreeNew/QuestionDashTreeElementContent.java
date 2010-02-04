package de.d3web.we.questionTreeNew;

import de.d3web.we.kdom.dashTree.DashTreeElementContent;

public class QuestionDashTreeElementContent extends DashTreeElementContent {
	
	public QuestionDashTreeElementContent() {
		this.childrenTypes.add(new QuestionLine());
		this.childrenTypes.add(new NumericCondLine());
		this.childrenTypes.add(new SetValueLine());
		this.childrenTypes.add(new AnswerLine());
		this.childrenTypes.add(new IndicationLine());
	}

}
