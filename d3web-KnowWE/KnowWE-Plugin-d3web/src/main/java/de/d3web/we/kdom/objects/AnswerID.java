package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.we.kdom.Section;

public abstract class AnswerID extends D3webID<Answer> {
	
	public AnswerID() {
		super("ANSWER_STORE_KEY");
	}

	//public abstract Section<QuestionID> getQuestionSection(Section<? extends AnswerID> s);

	
	public abstract <T extends QuestionID> Section<? extends QuestionID> getQuestionSection(Section<? extends AnswerID> s) ;

}
