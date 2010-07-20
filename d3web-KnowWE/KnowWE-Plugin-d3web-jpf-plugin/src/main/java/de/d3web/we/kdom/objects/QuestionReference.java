package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class QuestionReference extends D3webTermReference<Question> {

	public QuestionReference() {
		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR3));
	}

	@Override
	@SuppressWarnings("unchecked")
	public Question getTermObjectFallback(KnowWEArticle article, Section<?
			extends TermReference<Question>> s) {

		if (s.get() instanceof QuestionReference) {
			Section<QuestionReference> sec = (Section<QuestionReference>) s;
			String questionName = sec.get().getTermName(sec);

			KnowledgeBaseManagement mgn =
					D3webModule.getKnowledgeRepresentationHandler(
							article.getWeb()).getKBM(article.getTitle());

			Question question = mgn.findQuestion(questionName);
			return question;
		}
		return null;
	}


}
