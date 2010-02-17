package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.KnowWEObjectType;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.utils.KnowWEObjectTypeUtils;

public class QuestionTreeAnswerID extends AnswerID {

	@Override
	public<T extends QuestionID> Section<? extends QuestionID> getQuestionSection(Section<? extends AnswerID> s) {

		Section<DashTreeElement> localDashTreeElement = KnowWEObjectTypeUtils.getAncestorOfType(s, new DashTreeElement());
		
		Section<? extends DashTreeElement> dashTreeFather = DashTreeElement.getDashTreeFather(localDashTreeElement);
		
		Section<QuestionID> qid = dashTreeFather.findSuccessor(QuestionID.class);
		
		return qid;
	}



}
