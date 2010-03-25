package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.utils.KnowWEObjectTypeUtils;

public class QuestionTreeAnswerDef extends AnswerDef {


	@Override
	public <T extends QuestionDef> Section<? extends QuestionDef> getQuestionSection(Section<? extends AnswerDef> s) {
		Section<DashTreeElement> localDashTreeElement = KnowWEObjectTypeUtils.getAncestorOfType(s, new DashTreeElement());

		Section<? extends DashTreeElement> dashTreeFather = DashTreeElement.getDashTreeFather(localDashTreeElement);

		Section<QuestionDef> qid = dashTreeFather.findSuccessor(QuestionDef.class);

		return qid;
	}



}
