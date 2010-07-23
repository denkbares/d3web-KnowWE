package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;




public abstract class AnswerReference extends D3webTermReference<Choice> {

	String fontcolor;
	
	public AnswerReference() {
		this.setCustomRenderer(new ReferenceRenderer());
		this.fontcolor = FontColorRenderer.COLOR1;
	}
	
	class ReferenceRenderer extends KnowWEDomRenderer<AnswerReference>  {

		@Override
		public void render(KnowWEArticle article, Section<AnswerReference> sec, KnowWEUserContext user, StringBuilder string) {
			String refText = sec.get().getTermName(sec);
			String originalText = sec.getOriginalText();
			int index = originalText.indexOf(refText);
			
			new FontColorRenderer(fontcolor).render(article, sec, user, string);
			
//			if(index < 0) {
//				string.append("error: KnowWETermname not contained in text");
//			}else {
//				string.append(originalText.substring(0, index));
//				string.append(KnowWEUtils.maskHTML("<span")); 
//				string.append(" style='").append(fontcolor).append("'");
//				string.append(KnowWEUtils.maskHTML(">"));
//				string.append(KnowWEUtils.maskHTML("</span>"));
//				string.append(originalText.substring(index+refText.length(), originalText.length()));
//			}
			
		}
		
	}

	@Override
	@SuppressWarnings("unchecked")
	public Choice getTermObjectFallback(KnowWEArticle article, Section<? extends
			TermReference<Choice>> s) {

		if (s.get() instanceof AnswerReference) {
			Section<AnswerReference> sec = (Section<AnswerReference>) s;

			Section<QuestionReference> ref = sec.get().getQuestionSection(sec);
			String questionName = ref.get().getTermName(ref);

			String answerName = KnowWEUtils.trimQuotes(sec.getOriginalText());

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
	public abstract Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s);

	@Override
	@SuppressWarnings("unchecked")
	public String getTermName(Section<? extends KnowWETerm<Choice>> s) {

		Section<? extends AnswerReference> sa;

		if (s.get() instanceof AnswerReference) {
			sa = (Section<? extends AnswerReference>) s;
		}
		else {
			return super.getTermName(s);
		}

String answer = KnowWEUtils.trimQuotes(s.getOriginalText());

		//TODO: question prefix should be removed here!
		String question = KnowWEUtils.trimQuotes(getQuestionSection(sa).getOriginalText());
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
