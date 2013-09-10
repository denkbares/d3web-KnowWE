package de.d3web.we.object;

import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.knowwe.core.compile.terminology.TermRegistrationScope;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.SimpleTermReferenceRegistrationHandler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

public class AnswerReferenceRegistrationHandler extends SimpleTermReferenceRegistrationHandler {

	public AnswerReferenceRegistrationHandler() {
		super(TermRegistrationScope.LOCAL);
	}

	@Override
	public Collection<Message> validateReference(Article article, Section<Term> simpleTermSection) {
		Section<AnswerReference> section = Sections.cast(simpleTermSection,
				AnswerReference.class);
		Section<QuestionReference> ref = section.get().getQuestionSection(section);
		Question question = QuestionReference.getObject(article, ref);
		if (question != null) {
			if (question instanceof QuestionYN) {
				Choice choice = KnowledgeBaseUtils.findChoice((QuestionYN) question,
						section.get().getTermName(section), false);
				if (choice != null) return Messages.noMessage();

			}
			else if (question instanceof QuestionNum) {
				NumericalInterval range = question.getInfoStore().getValue(
						BasicProperties.QUESTION_NUM_RANGE);
				try {
					Double value = Double.parseDouble(section.get().getTermName(section).trim());
					if (range == null || range.contains(value)) {
						return Messages.noMessage();
					}
					else {
						return Arrays.asList(Messages.error("The value '" + value
								+ "' is not in the defined range " + range.toString()
								+ " of question '" + question.getName() + "'."));
					}
				}
				catch (NumberFormatException e) {
					return Arrays.asList(Messages.error("The value "
							+ section.get().getTermName(section) + " is not a numeric answer"));
				}
			}
		}
		return super.validateReference(article, simpleTermSection);
	}
}
