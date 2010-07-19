package de.d3web.we.kdom.objects;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.questionTreeNew.QuestionTreeElementDef;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedError;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.subtreeHandler.Priority;
import de.d3web.we.terminology.TerminologyManager;

public abstract class AnswerDef extends QuestionTreeElementDef<Choice> {

	public AnswerDef() {
		super("ANSWER_STORE_KEY");
		this.addSubtreeHandler(Priority.HIGH, new CreateAnswerHandler());
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR1));
		this.setOrderSensitive(true);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Section<? extends QuestionDef> retrieveAndStoreParentQASetSection(
			Section<? extends QuestionTreeElementDef<?>> s) {
		return (Section<? extends QuestionDef>) super.retrieveAndStoreParentQASetSection(s);
	}


	@Override
	@SuppressWarnings("unchecked")
	public String getTermName(Section<? extends TermReference<Choice>> s) {

		Section<? extends AnswerDef> sa;

		if (s.get() instanceof AnswerDef) {
			sa = (Section<? extends AnswerDef>) s;
		}
		else {
			return super.getTermName(s);
		}

		String answer = s.getOriginalText().trim();

		if (answer.startsWith("\"") && answer.endsWith("\"")) {
			answer = answer.substring(1, answer.length() - 1).trim();
		}

		String question = getStoredParentQASetSection(sa).getOriginalText().trim();

		if (question.startsWith("\"") && question.endsWith("\"")) {
			question = question.substring(1, question.length() - 1).trim();
		}

		return question + " " + answer;
	}

	static class CreateAnswerHandler extends QuestionTreeElementDefSubtreeHandler<AnswerDef> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<AnswerDef> answerSection) {

			KnowledgeBaseManagement mgn = getKBM(article);

			String name = answerSection.getOriginalText().trim();
			if (name.startsWith("\"") && name.endsWith("\"")) {
				name = name.substring(1, name.length() - 1).trim();
			}

			Section<? extends QuestionDef> questionID = answerSection
					.get().retrieveAndStoreParentQASetSection(answerSection);

			Question q = questionID.get().getObject(article, questionID);

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

					TerminologyManager.getInstance().registerTermDef(
							article, answerSection);

					answerSection.get().storeObject(article, answerSection, a);

					return Arrays.asList((KDOMReportMessage) new NewObjectCreated(
							a.getClass().getSimpleName() + "  "
									+ a.getName()));
				}
			}
			return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
					"no choice question - " + name,
					this.getClass()));
		}


		@Override
		public void destroy(KnowWEArticle article, Section<AnswerDef> s) {
			// s.get().retrieveAndStoreParentQASetSection(s);
			TerminologyManager.getInstance().unregisterTermDef(article, s);
		}
	}

}
