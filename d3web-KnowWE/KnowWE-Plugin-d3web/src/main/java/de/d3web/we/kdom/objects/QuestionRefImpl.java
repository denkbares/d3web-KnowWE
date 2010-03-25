package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.Section;


public class QuestionRefImpl<Question> extends QuestionRef<Question> {


	@Override
	public boolean objectExisting(Section<?> s) {
		Section<QuestionRef> qidSection = (Section<QuestionRef>) s;

		String name = qidSection.get().getID(qidSection);

		KnowledgeBaseManagement mgn =
				D3webModule.getKnowledgeRepresentationHandler(s.getArticle().getWeb())
				.getKBM(s.getArticle(), s);

		IDObject o = mgn.findQuestion(name);
		return o != null;
	}



}
