package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class QuestionnaireReference extends D3webTermReference<QContainer> {

	public QuestionnaireReference() {
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR5));
		this.setOrderSensitive(true);
	}

	@Override
	@SuppressWarnings("unchecked")
	public QContainer getTermObjectFallback(KnowWEArticle article, Section<? extends
			TermReference<QContainer>> s) {

		if (s.get() instanceof QuestionnaireReference) {
			Section<QuestionnaireReference> sec = (Section<QuestionnaireReference>) s;
			String qcName = sec.get().getTermName(sec);

			KnowledgeBaseManagement mgn =
					D3webModule.getKnowledgeRepresentationHandler(
							article.getWeb())
							.getKBM(article.getTitle());

			QContainer q = mgn.findQContainer(qcName);
			return q;
		}
		return null;
	}

}
