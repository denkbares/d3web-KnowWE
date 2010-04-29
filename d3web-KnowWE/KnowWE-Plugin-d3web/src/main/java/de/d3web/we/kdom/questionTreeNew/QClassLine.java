package de.d3web.we.kdom.questionTreeNew;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionnaireDef;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedWarning;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.utils.KnowWEObjectTypeUtils;

public class QClassLine extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {

		initSectionFinder();

		QuestionnaireDef qc = new QuestionnaireDef();
		qc.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR5));
		qc.setSectionFinder(new AllTextFinderTrimmed());
		qc.addReviseSubtreeHandler(new CreateQuestionnaireHandler());
		this.childrenTypes.add(qc);
	}

	private void initSectionFinder() {
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
					if (dashTreeFather.findSuccessor(QClassLine.class) != null) {
						return true;
					}
				}

				return false;
			}
		};
	}

	static class CreateQuestionnaireHandler implements ReviseSubTreeHandler {

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
			
			Section<QuestionnaireDef> qcSec = (s);
			
			KnowledgeBaseManagement mgn = D3webModule
					.getKnowledgeRepresentationHandler(article.getWeb())
					.getKBM(article, s);
			//ReviseSubtreeHandler will be called again with a correct mgn
			if (mgn == null) return null;
			
			String name = s.getOriginalText();
			
			IDObject o = mgn.findQContainer(name);

			if (o != null) {
				return new ObjectAlreadyDefinedWarning(o.getClass()
						.getSimpleName());
			} else {
				Section<DashTreeElement> element = KnowWEObjectTypeUtils
						.getAncestorOfType(s, new DashTreeElement());
				Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
						.getDashTreeFather(element);
				QASet parent = mgn.getKnowledgeBase().getRootQASet();
				if (dashTreeFather != null) {
					// is child of a QClass declaration => also declaration
					Section<QClassLine> parentQclass = dashTreeFather
							.findSuccessor(QClassLine.class);
					if (parentQclass != null) {
						QASet localParent = mgn.findQContainer(parentQclass
								.getOriginalText());
						if (localParent != null) {
							parent = localParent;
						}
					}
				}

				QContainer qc = mgn.createQContainer(name, parent);
				if (qc != null) {
					qcSec.get().storeObject(qcSec, qc);
					return new NewObjectCreated(qc.getClass().getSimpleName()
							+ " " + qc.getName());
				} else {
					return new ObjectCreationError(name, this.getClass());
				}
			}
		}

	}

}
