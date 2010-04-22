package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.we.kdom.Section;




public abstract class AnswerRef extends D3webObjectRef<Answer> {

	/**
	 * returns the section of the corresponding question-reference for this
	 * answer
	 *
	 * @param s
	 * @return
	 */
	public abstract Section<QuestionRef> getQuestionSection(Section<? extends AnswerRef> s);


}
