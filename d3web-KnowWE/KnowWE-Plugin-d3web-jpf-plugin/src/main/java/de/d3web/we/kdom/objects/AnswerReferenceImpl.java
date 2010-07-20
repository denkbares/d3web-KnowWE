package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class AnswerReferenceImpl extends AnswerReference {

	@Override
	protected void init() {
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR5));
	}

	@Override
	public Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s) {
		return s.getFather().findSuccessor(QuestionReference.class);
	}

}
