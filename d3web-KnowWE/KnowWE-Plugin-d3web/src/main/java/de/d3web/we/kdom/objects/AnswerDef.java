package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.we.kdom.Section;



public abstract class AnswerDef extends D3webObjectDef<Answer> {

	/**
	 * returns the section of the corresponding question-reference for this
	 * answer
	 * 
	 * @param s
	 * @return
	 */
	public abstract <T extends QuestionDef> Section<? extends QuestionDef> getQuestionSection(Section<? extends AnswerDef> s);

	public AnswerDef() {
		super("ANSWER_STORE_KEY");
	}




}
