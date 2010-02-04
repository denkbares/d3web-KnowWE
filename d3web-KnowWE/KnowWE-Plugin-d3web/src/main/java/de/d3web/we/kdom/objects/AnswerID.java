package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.Section;

public abstract class AnswerID extends ObjectRef {
	
	public abstract Section<QuestionID> getQuestion(Section<AnswerID> s);

}
