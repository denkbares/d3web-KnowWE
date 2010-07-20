package de.d3web.we.kdom.objects;

import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.questionTreeNew.QuestionTreeElementDefinition;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedWarning;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.subtreeHandler.Priority;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;

public abstract class QuestionDefinition extends QuestionTreeElementDefinition<Question> {

	public static enum QuestionType {
		OC, MC, YN, NUM, DATE, TEXT;
	}

	public QuestionDefinition() {
		super("QUESTION_STORE_KEY");
		this.addSubtreeHandler(Priority.HIGHER, new CreateQuestionHandler());
		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR3));
		this.setOrderSensitive(true);
	}

	public abstract QuestionType getQuestionType(Section<QuestionDefinition> s);


	static class CreateQuestionHandler extends QuestionTreeElementDefSubtreeHandler<QuestionDefinition> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<QuestionDefinition> sec) {

			Section<QuestionDefinition> qidSection = (sec);

			String name = qidSection.get().getTermName(qidSection);

			KnowledgeBaseManagement mgn = getKBM(article);

			IDObject o = mgn.findQuestion(name);

			if (o != null) {
				return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedWarning(
						o.getClass().getSimpleName()));
			}

			QASet parent = D3webUtils.findParent(qidSection, mgn);

			QuestionType questionType = qidSection.get().getQuestionType(
					qidSection);

			Question q = null;
			if (questionType.equals(QuestionType.OC)) {
				q = mgn.createQuestionOC(name, parent, new String[] {});
			}
			else if (questionType.equals(QuestionType.MC)) {
				q = mgn.createQuestionMC(name, parent, new String[] {});
			}
			else if (questionType.equals(QuestionType.NUM)) {
				q = mgn.createQuestionNum(name, parent);
			}
			else if (questionType.equals(QuestionType.YN)) {
				q = mgn.createQuestionYN(name, parent);
			}
			else if (questionType.equals(QuestionType.DATE)) {
				q = mgn.createQuestionDate(name, parent);
			}
			else if (questionType.equals(QuestionType.TEXT)) {
				q = mgn.createQuestionText(name, parent);
			}
			else {
				// no valid type...
			}

			if (q != null) {
				// ok everything went well
				// set position right in case this is an incremental update
				if (!article.isFullParse()) {
					parent.moveChildToPosition(q, sec.get().getPosition(sec));
				}
				// register term
				KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
						article, sec);

				// store object in section
				qidSection.get().storeTermObject(article, qidSection, q);

				// return success message
				return Arrays.asList((KDOMReportMessage) new NewObjectCreated(
						q.getClass().getSimpleName()
								+ " " + q.getName()));
			}
			else {
				return Arrays.asList((KDOMReportMessage) new ObjectCreationError(name,
						this.getClass()));
			}

		}

		@Override
		public void destroy(KnowWEArticle article, Section<QuestionDefinition> question) {

			Question q = question.get().getTermObjectFromLastVersion(article, question);
			try {
				if (q != null) q.getKnowledgeBase().remove(q);
			}
			catch (IllegalAccessException e) {
				article.setFullParse(true, this);
				// e.printStackTrace();
			}
			KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(article,
					question);
		}

	}

}
