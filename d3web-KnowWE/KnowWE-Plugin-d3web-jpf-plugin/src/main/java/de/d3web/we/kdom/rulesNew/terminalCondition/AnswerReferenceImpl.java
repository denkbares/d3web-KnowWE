package de.d3web.we.kdom.rulesNew.terminalCondition;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.AnswerReference;
import de.d3web.we.kdom.objects.QuestionReference;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class AnswerReferenceImpl extends AnswerReference {


	@Override
	public Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s) {
		return s.getFather().findSuccessor(QuestionReference.class);
	}

}
