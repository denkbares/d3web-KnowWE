package de.d3web.we.kdom.objects;

import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.we.kdom.Section;

public abstract class AnswerID extends ObjectRef {
	
	public abstract Section<QuestionID> getQuestionSection(Section<? extends AnswerID> s);

	@Override
	public QContainer getObject(Section<? extends ObjectRef> s) {
		// TODO Auto-generated method stub
		return null;
	}
}
