package de.d3web.we.kdom.objects;

import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.dashTree.DashTreeUtils;
import de.d3web.we.kdom.questionTreeNew.QClassLine;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedWarning;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public abstract class QuestionnaireDefinition extends QASetDefinition<QContainer> {

	public QuestionnaireDefinition() {
		super("QUESTIONAIRE_STORE_KEY");
		addSubtreeHandler(Priority.HIGHEST, new CreateQuestionnaireHandler());
		setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR5));
		setOrderSensitive(true);
	}

	public abstract int getPosition(Section<QuestionnaireDefinition> s);

	static class CreateQuestionnaireHandler
			extends D3webSubtreeHandler<QuestionnaireDefinition> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<QuestionnaireDefinition> qcSec) {

			KnowledgeBaseManagement mgn = getKBM(article);
			// ReviseSubtreeHandler will be called again with a correct mgn
			if (mgn == null) return null;

			String name = qcSec.getOriginalText();

			IDObject o = mgn.findQContainer(name);

			if (o != null) {
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedWarning(
						o.getClass()
								.getSimpleName()));
			}
			else {
				Section<? extends DashTreeElement> dashTreeFather = DashTreeUtils
						.getFatherDashTreeElement(qcSec);
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
					if (!article.isFullParse()) parent.moveChildToPosition(qc,
							qcSec.get().getPosition(qcSec));
					qcSec.get().storeTermObject(article, qcSec, qc);
					KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
							article, qcSec);
					return Arrays.asList((KDOMReportMessage) new NewObjectCreated(
							qc.getClass().getSimpleName()
									+ " " + qc.getName()));
				}
				else {
					return Arrays.asList((KDOMReportMessage) new ObjectCreationError(name,
							this.getClass()));
				}
			}
		}

		@Override
		public void destroy(KnowWEArticle article, Section<QuestionnaireDefinition> s) {

			QContainer q = s.get().getTermObjectFromLastVersion(article, s);
			try {
				if (q != null) {
					q.getKnowledgeBase().remove(q);
					KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
							article, s);
				}
			}
			catch (IllegalAccessException e) {
				article.setFullParse(true, this);
				// e.printStackTrace();
			}
		}

	}

}
