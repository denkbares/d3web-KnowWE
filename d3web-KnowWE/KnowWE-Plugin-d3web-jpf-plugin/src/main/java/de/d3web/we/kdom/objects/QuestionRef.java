package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.terminology.TerminologyManager;

public class QuestionRef extends D3webObjectRef<Question> {

	public QuestionRef() {
		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR3));
	}

	@Override
	public Question getObject(Section<? extends ObjectRef<Question>> s) {
		// new lookup method using Terminology Manager
		Section<? extends ObjectDef<Question>> objectDefinition = TerminologyManager.getInstance().getObjectDefinition(
				s);
		if (objectDefinition != null) {
			Question q = objectDefinition.get().getObject(objectDefinition);
			if (q != null) {
				return q;
			}
		}

		// old lookup method using knowledge base - evil slow!!
		if (s.get() instanceof QuestionRef) {
			Section<QuestionRef> sec = (Section<QuestionRef>) s;
			String questionName = sec.get().getTermName(sec);

			KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(
					s.getArticle().getWeb())
					.getKBM(s.getArticle(), null, sec);

			Question question = mgn.findQuestion(questionName);
			return question;
		}
		return null;
	}


}
