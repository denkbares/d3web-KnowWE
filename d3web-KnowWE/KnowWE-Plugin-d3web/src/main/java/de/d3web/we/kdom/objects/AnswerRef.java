package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.values.Choice;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;




public abstract class AnswerRef extends D3webObjectRef<Choice> {

	public AnswerRef() {
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR1));
	}

	/**
	 * returns the section of the corresponding question-reference for this
	 * answer
	 *
	 * @param s
	 * @return
	 */
	public abstract Section<QuestionRef> getQuestionSection(Section<? extends AnswerRef> s);


	@Override
	public Choice getObject(Section<? extends ObjectRef<Choice>> s) {

		if(s.get() instanceof AnswerRef) {
			Section<AnswerRef> sec = (Section<AnswerRef>) s;
			String answerName = sec.get().getTermName(sec);
			Section<QuestionRef> ref = sec.get().getQuestionSection(sec);
			String questionName = ref.get().getTermName(ref);

			KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(
					s.getArticle().getWeb())
					.getKBM(s.getArticle(), null, sec);

			Question question = mgn.findQuestion(questionName);
			if (question != null && question instanceof QuestionChoice) {
				return (Choice) mgn.findAnswerChoice((QuestionChoice) question,
						answerName);

			}

		}

		return null;

	}

}
