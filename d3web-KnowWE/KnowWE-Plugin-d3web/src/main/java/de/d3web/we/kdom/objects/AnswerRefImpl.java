package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class AnswerRefImpl<Answer> extends AnswerRef<Answer> {
	@Override
	protected void init() {
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR5));
	}




	@Override
	public boolean objectExisting(Section<?> s) {
		Section<AnswerRef<Answer>> a = (Section<AnswerRef<Answer>>) s;

		Section<? extends QuestionRef> qidSection =
				this.getQuestionSection(a);

		if (qidSection != null) {

			String name = qidSection.get().getID(qidSection);

			KnowledgeBaseManagement mgn =
					D3webModule.getKnowledgeRepresentationHandler(s.getArticle().getWeb())
					.getKBM(s.getArticle(), s);

			Question o = mgn.findQuestion(name);

			de.d3web.core.knowledge.terminology.Answer answer = mgn.findAnswer(o,
					a.get().getID(a));

			return answer != null;
		}

		return false;
	}



	@Override
	public Section<QuestionRef> getQuestionSection(Section<? extends AnswerRef<Answer>> s) {
		return s.getFather().findSuccessor(QuestionRef.class);
	}

}
