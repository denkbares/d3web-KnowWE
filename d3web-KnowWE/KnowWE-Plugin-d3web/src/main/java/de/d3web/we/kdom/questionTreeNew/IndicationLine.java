package de.d3web.we.kdom.questionTreeNew;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.error.KDOMError;
import de.d3web.we.kdom.error.NoSuchObjectError;
import de.d3web.we.kdom.error.ObjectAlreadyDefinedError;
import de.d3web.we.kdom.objects.QuestionnaireID;
import de.d3web.we.kdom.questionTreeNew.QClassLine.CreateQuestionnaireHandler;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;

public class IndicationLine extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = AllTextFinderTrimmed.getInstance();

		QuestionnaireID qc = new QuestionnaireID();
		qc.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));
		qc.setSectionFinder(AllTextFinderTrimmed.getInstance());
		qc.addReviseSubtreeHandler(new CreateIndication());
		this.childrenTypes.add(qc);
	}

	static class CreateIndication implements ReviseSubTreeHandler {

		@Override
		public void reviseSubtree(KnowWEArticle article, Section s) {
			KnowledgeBaseManagement mgn = D3webModule
					.getKnowledgeRepresentationHandler(article.getWeb())
					.getKBM(article, s);

			String name = s.getOriginalText();

			IDObject o = mgn.findQContainer(name);
			
			if (o != null) {
				//TODO
			} else {
				
				KDOMError.storeError(s, new NoSuchObjectError(name));
			}
		}

	}
}
