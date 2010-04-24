package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectCreationError;



public abstract class AnswerDef extends D3webObjectDef<Answer> {


	/**
	 * returns the section of the corresponding question-reference for this
	 * answer
	 *
	 * @param s
	 * @return
	 */
	public abstract <T extends QuestionDef> Section<? extends QuestionDef> getQuestionSection(Section<? extends AnswerDef> s);

	public AnswerDef() {
		super("ANSWER_STORE_KEY");
		this.addReviseSubtreeHandler(new createAnswerHandler());
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR1));

	}

	static class createAnswerHandler implements ReviseSubTreeHandler {

		@SuppressWarnings("unchecked")
		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {

			if (s.getObjectType() instanceof QuestionTreeAnswerDef) {

				KnowledgeBaseManagement mgn = D3webModule
						.getKnowledgeRepresentationHandler(article.getWeb())
						.getKBM(article, s);

				if (mgn == null) return null;

				// "safe unsafe cast"
				Section<QuestionTreeAnswerDef> answer = s;
				String name = answer.get().getID(answer);
				Section<? extends QuestionDef> questionID = answer.get().getQuestionSection(
						answer);

				// Section<QuestionID> questionID =
				// ((QuestionTreeAnswerID)answer.getObjectType()).getQuestionSection(answer);
				Question q = questionID.get().getObject(questionID);

				if (q instanceof QuestionChoice) {
					Answer a = mgn.addChoiceAnswer((QuestionChoice) q, name);
					answer.get().storeObject(answer, a);
					return new NewObjectCreated(a.getClass().getSimpleName() + "  "
							+ a.getName());
				}
				return new ObjectCreationError("no choice question - " + name,
						this.getClass());
			}
			return new ObjectCreationError(null, this.getClass());
		}
	}




}
