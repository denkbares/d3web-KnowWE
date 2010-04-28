package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;

public class QuestionnaireRef extends D3webObjectRef<QContainer> {

	public QuestionnaireRef() {
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR5));
	}

	@Override
	public QContainer getObject(Section<? extends ObjectRef<QContainer>> s) {
		if (s.get() instanceof QuestionnaireRef) {
			Section<QuestionnaireRef> sec = (Section<QuestionnaireRef>) s;
			String qcName = sec.get().getTermName(sec);

			KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(
					s.getArticle().getWeb())
					.getKBM(s.getArticle(), sec);

			QContainer q = mgn.findQContainer(qcName);
			return q;
		}
		return null;
	}



}
