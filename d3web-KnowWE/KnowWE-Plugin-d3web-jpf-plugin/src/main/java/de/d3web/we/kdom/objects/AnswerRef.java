package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.utils.KnowWEUtils;




public abstract class AnswerRef extends D3webObjectRef<Choice> {

	public AnswerRef() {
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR1));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Choice getObjectFallback(KnowWEArticle article, Section<? extends
			ObjectRef<Choice>> s) {

		if (s.get() instanceof AnswerRef) {
			Section<AnswerRef> sec = (Section<AnswerRef>) s;

			Section<QuestionRef> ref = sec.get().getQuestionSection(sec);
			String questionName = ref.get().getTermName(ref);

			String answerName = KnowWEUtils.trimAndRemoveQuotes(sec.getOriginalText());

			KnowledgeBaseManagement mgn =
					D3webModule.getKnowledgeRepresentationHandler(
							s.getArticle().getWeb())
							.getKBM(article.getTitle());

			Question question = mgn.findQuestion(questionName);
			if (question != null && question instanceof QuestionChoice) {
				return mgn.findChoice((QuestionChoice) question,
						answerName);

			}

		}

		return null;

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
	@SuppressWarnings("unchecked")
	public String getTermName(Section<? extends TermReference<Choice>> s) {

		Section<? extends AnswerRef> sa;

		if (s.get() instanceof AnswerRef) {
			sa = (Section<? extends AnswerRef>) s;
		}
		else {
			return super.getTermName(s);
		}

		String answer = s.getOriginalText().trim();

		if (answer.startsWith("\"") && answer.endsWith("\"")) {
			answer = answer.substring(1, answer.length() - 1).trim();
		}

		String question = getQuestionSection(sa).getOriginalText().trim();

		if (question.startsWith("\"") && question.endsWith("\"")) {
			question = question.substring(1, question.length() - 1).trim();
		}

		return question + " " + answer;
	}

	// @Override
	// public Choice getObject(KnowWEArticle article, Section<? extends
	// ObjectRef<Choice>> s) {
	//
	// // new lookup method using Terminology Manager
	// Section<? extends ObjectDef<Choice>> objectDefinition =
	// TerminologyManager.getInstance()
	// .getObjectDefinition(article, s);
	// if (objectDefinition != null) {
	// Choice c = objectDefinition.get().getObject(objectDefinition);
	// if (c != null &&
	// c.getName().equals(objectDefinition.get().getTermName(s))) {
	// return c;
	// }
	// }
	//
	// // old lookup method using knowledge base - evil slow!!
	// Section<AnswerRef> sec = (Section<AnswerRef>) s;
	// String answerName = sec.get().getTermName(sec);
	// Section<QuestionRef> ref = sec.get().getQuestionSection(sec);
	// String questionName = ref.get().getTermName(ref);
	//
	// KnowledgeBaseManagement mgn =
	// D3webModule.getKnowledgeRepresentationHandler(
	// article.getWeb())
	// .getKBM(article.getTitle());
	//
	// Question question = mgn.findQuestion(questionName);
	// if (question != null && question instanceof QuestionChoice) {
	// return mgn.findChoice((QuestionChoice) question,
	// answerName);
	//
	// }
	//
	//
	// return null;
	//
	// }

}
