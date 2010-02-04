package de.d3web.we.kdom.questionTreeNew;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.qasets.QContainer;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.error.KDOMError;
import de.d3web.we.kdom.error.ObjectAlreadyDefinedError;
import de.d3web.we.kdom.objects.QuestionnaireID;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.utils.KnowWEObjectTypeUtils;

public class QClassLine extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {

		QuestionnaireID qc = new QuestionnaireID();
		qc.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR5));
		this.sectionFinder = new ConditionalAllTextFinder() {

			@Override
			protected boolean condition(String text, Section father) {

				Section<DashTreeElement> s = KnowWEObjectTypeUtils
						.getAncestorOfType(father, new DashTreeElement());
				if (DashTreeElement.getLevel(s) == 0) {
					// is root level
					return true;
				}
				Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
						.getDashTreeFather(s);
				if (dashTreeFather != null) {
					// is child of a QClass declration => also declaration
					if (dashTreeFather.findSuccessor(new QClassLine()) != null) {
						return true;
					}
				}

				return false;
			}
		};
		qc.setSectionFinder(AllTextFinderTrimmed.getInstance());
		qc.addReviseSubtreeHandler(new CreateQuestionnaireHandler());
		this.childrenTypes.add(qc);
	}

	static class CreateQuestionnaireHandler implements ReviseSubTreeHandler {

		@Override
		public void reviseSubtree(KnowWEArticle article, Section s) {
			KnowledgeBaseManagement mgn = D3webModule.getInstance()
					.getKnowledgeRepresentationHandler(article.getWeb())
					.getKBM(article, s);

			String name = s.getOriginalText();

			IDObject o = mgn.findQContainer(name);

			if (o != null) {
				KDOMError.storeError(s, new ObjectAlreadyDefinedError(o
						.getClass().getSimpleName()));
			} else {
				Section<DashTreeElement> element = KnowWEObjectTypeUtils
				.getAncestorOfType(s, new DashTreeElement());
				Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
						.getDashTreeFather(element);
				QASet parent = mgn.getKnowledgeBase()
				.getRootQASet();
				if (dashTreeFather != null) {
					// is child of a QClass declaration => also declaration
					Section<QClassLine> parentQclass = dashTreeFather
							.findSuccessor(new QClassLine());
					if (parentQclass != null) {
						QASet localParent = mgn.findQContainer(parentQclass.getOriginalText());
						if(localParent != null) {
							parent = localParent;
						}
					}
				}
				
				mgn.createQContainer(name, parent);
			}
		}

	}

}
