package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.values.Choice;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class AnswerRefImpl extends AnswerRef {
	@Override
	protected void init() {
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR5));
	}



	@Override
	public Section<QuestionRef> getQuestionSection(Section<? extends AnswerRef> s) {
		return s.getFather().findSuccessor(QuestionRef.class);
	}



	@Override
	public boolean objectExisting(Section<? extends ObjectRef<Choice>> s) {
		Section<AnswerRef> a = (Section<AnswerRef>) s;

		Section<? extends QuestionRef> qidSection =
				this.getQuestionSection(a);

		if (qidSection != null) {

			String name = qidSection.get().getTermName(qidSection);

			KnowledgeBaseManagement mgn =
					D3webModule.getKnowledgeRepresentationHandler(s.getArticle().getWeb())
							.getKBM(s.getArticle(), null, s);

			Question o = mgn.findQuestion(name);

			de.d3web.core.knowledge.terminology.Answer answer = mgn.findAnswer(o,
					a.get().getTermName(a));

			return answer != null;
		}

		return false;
	}

}
