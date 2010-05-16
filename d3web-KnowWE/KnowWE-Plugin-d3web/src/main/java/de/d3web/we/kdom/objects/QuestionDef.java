package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.D3webUtils;


public abstract class QuestionDef extends D3webObjectDef<Question> {

	public static enum QuestionType {
		OC, MC, YN, NUM, DATE, TEXT;
	}

	public QuestionDef() {
		super("QUESTION_STORE_KEY");
		this.addSubtreeHandler(new CreateQuestionHandler());
		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR3));

	}

	public abstract QuestionType getQuestionType(Section<QuestionDef> s);



	static class CreateQuestionHandler implements SubtreeHandler<QuestionDef> {

	@Override
	public KDOMReportMessage reviseSubtree(KnowWEArticle article,
				Section<QuestionDef> sec) {

		Section<QuestionDef> qidSection = (sec);

		String name = qidSection.get().getTermName(qidSection);

		KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(
				article.getWeb())
				.getKBM(article, this, sec);
		if (mgn == null) return null;

		IDObject o = mgn.findQuestion(name);

			// if (o != null) {
			// // TODO: quick fix - remove when Question is exceptionally
			// // created in this handler
			// if (o instanceof Question) {
			// qidSection.get().storeObject(qidSection, (Question) o);
			// }
			//
			// return new ObjectAlreadyDefinedWarning(o.getClass()
			// .getSimpleName());
			// }
			// else
			{
				QASet parent = D3webUtils.findParent(qidSection, mgn);

			de.d3web.we.kdom.objects.QuestionDef.QuestionType questionType = qidSection.get().getQuestionType(
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

			}
			if (q != null) {
				qidSection.get().storeObject(qidSection, q);
				return new NewObjectCreated(q.getClass().getSimpleName()
						+ " " + q.getName());
			}
			else {
				return new ObjectCreationError(name, this.getClass());
			}

		}

	}



}
}
