package de.d3web.we.kdom.objects;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.values.Choice;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedError;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.subtreeHandler.Priority;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

public abstract class AnswerDef extends D3webObjectDef<Choice> {

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
		this.addSubtreeHandler(Priority.HIGHER, new createAnswerHandler());
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR1));

	}

	static class createAnswerHandler extends SubtreeHandler<AnswerDef> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<AnswerDef> s) {

			KnowledgeBaseManagement mgn = D3webModule
					.getKnowledgeRepresentationHandler(article.getWeb())
					.getKBM(article, this, s);

			if (mgn == null) return null;

			// "safe unsafe cast"
			Section<AnswerDef> answer = s;
			String name = answer.get().getTermName(answer);
			Section<? extends QuestionDef> questionID = answer.get().getQuestionSection(
					answer);

			// Section<QuestionID> questionID =
			// ((QuestionTreeAnswerID)answer.getObjectType()).getQuestionSection(answer);
			Question q = questionID.get().getObject(questionID);

			if (q instanceof QuestionChoice) {

				// at first check if Answer already defined
				boolean alreadyExisting = false;
				List<Choice> allAlternatives = ((QuestionChoice) q).getAllAlternatives();
				for (Choice choice : allAlternatives) {
					if (choice.getName().equals(name)) {
						alreadyExisting = true;
					}
				}

				if (alreadyExisting) {
					return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedError(
							"Answer already existing - " + name));
				}
				else {

					Choice a = mgn.addChoiceAnswer((QuestionChoice) q, name);
					answer.get().storeObject(answer, a);
					return Arrays.asList((KDOMReportMessage) new NewObjectCreated(
							a.getClass().getSimpleName() + "  "
									+ a.getName()));
				}
			}
			return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
					"no choice question - " + name,
					this.getClass()));
		}
	}

}
