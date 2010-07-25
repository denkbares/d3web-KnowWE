package de.d3web.we.kdom.objects;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.IncrementalConstraints;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedError;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

public abstract class AnswerDefinition
		extends D3webTermDefinition<Choice>
		implements IncrementalConstraints, NotUniqueKnowWETerm<Choice> {

	private static final String QUESTION_FOR_ANSWER_KEY = "QUESTION_FOR_ANSWER_KEY";

	public AnswerDefinition() {
		super("ANSWER_STORE_KEY");
		this.addSubtreeHandler(Priority.HIGH, new CreateAnswerHandler());
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR1));
		this.setOrderSensitive(true);
	}
	
	public abstract int getPosition(Section<? extends AnswerDefinition> s);
	
	public abstract Section<? extends QuestionDefinition> getQuestionSection(Section<? extends AnswerDefinition> s);

	@Override
	public boolean hasViolatedConstraints(KnowWEArticle article, Section<?> s) {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String getUniqueTermIdentifier(KnowWEArticle article, Section<? extends KnowWETerm<Choice>> s) {

		String answer = s.get().getTermName(s);

		Section<? extends QuestionDefinition> qdef = (Section<? extends QuestionDefinition>)
				KnowWEUtils.getStoredObject(article, s, QUESTION_FOR_ANSWER_KEY);
		if (qdef == null) {
			qdef = getQuestionSection((Section<AnswerDefinition>) s);
		}

		String question = KnowWEUtils.trimQuotes(qdef.getOriginalText());

		return question + " " + answer;
	}

	static class CreateAnswerHandler extends D3webSubtreeHandler<AnswerDefinition> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article,
				Section<AnswerDefinition> s) {

			KnowledgeBaseManagement mgn = getKBM(article);

			String name = s.get().getTermName(s);

			Section<? extends QuestionDefinition> qDef = s
					.get().getQuestionSection(s);

			if (qDef.hasErrorInSubtree()) {
				return Arrays.asList((KDOMReportMessage) new ObjectCreationError(
						"no valid question - " + name,
						this.getClass()));
			}

			KnowWEUtils.storeSectionInfo(article, s, AnswerDefinition.QUESTION_FOR_ANSWER_KEY, qDef);

			Question q = qDef.get().getTermObject(article, qDef);

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
					Choice a = mgn.addChoiceAnswer((QuestionChoice) q, name,
							s.get().getPosition(s));

					KnowWEUtils.getTerminologyHandler(article.getWeb()).registerTermDefinition(
							article, s);

					s.get().storeTermObject(article, s, a);

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
		public void destroy(KnowWEArticle article, Section<AnswerDefinition> s) {

			KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
					article, s);
			// why does this work?
			// explanation:
			// the answer is (un)registered using the uniqueTermIdentifier,
			// which uses either the stored father question or, if there
			// is no stored father question, retrieves the father question
			// again... both variants work correctly:
			//
			// 1) the answer is not reused in the new KDOM... in this case
			// the uniqueTermIdentifier will not find a stores question
			// section, but since the answer wasn't reused it is still
			// hooked in the same father question -> new retrieval of the
			// father Question still returns the correct one
			//
			// 2) the answer section is reused but needs to be destroyed
			// anyway, because for example the position has changed -> since
			// the answer section is reused, uniqueTermIdentifier will find
			// the stored question which is still the one the answer was
			// hooked in in the last KDOM.
		}
	}


}
