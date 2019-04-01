package de.d3web.we.object;

import java.util.Collection;
import java.util.Collections;

import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.QuestionYN;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.ValueUtils;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

public class AnswerReferenceRegistrationHandler extends SimpleReferenceRegistrationScript<D3webCompiler, AnswerReference> {

	public AnswerReferenceRegistrationHandler() {
		super(D3webCompiler.class);
	}

	@Override
	public Collection<Message> validateReference(D3webCompiler compiler, Section<AnswerReference> section) {
		Section<QuestionReference> ref = section.get().getQuestionSection(section);
		Question question = QuestionReference.getObject(compiler, ref);
		if (question instanceof QuestionYN) {
			String choiceName = section.get().getTermName(section);
			Choice choice = KnowledgeBaseUtils.findChoice((QuestionYN) question, choiceName, false);
			if (choice != null) {
				return Messages.noMessage();
			}
		}
		else if (question instanceof QuestionNum) {
			NumericalInterval range = question.getInfoStore().getValue(
					BasicProperties.QUESTION_NUM_RANGE);
			try {
				double value = Double.parseDouble(section.getText());
				if (range == null || range.contains(value)) {
					return Messages.noMessage();
				}
				else {
					return Collections.singletonList(Messages.error("The value '" + value
							+ "' is not in the defined range " + range
							+ " of question '" + question.getName() + "'."));
				}
			}
			catch (NumberFormatException e) {
				return Collections.singletonList(Messages.error("The value "
						+ section.getText() + " is not a numeric answer"));
			}
		}
		else if (question instanceof QuestionText) {
			return Messages.noMessage();
		}
		else if (question instanceof QuestionDate) {
			String value = Strings.trimQuotes(section.getText());
			try {
				ValueUtils.createDateValue((QuestionDate) question, value);
				return Messages.noMessage();
			}
			catch (IllegalArgumentException e) {
				return Messages.asList(Messages.error("The value '"
						+ value + "' does not apply to any supported date format"));
			}
		}
		return super.validateReference(compiler, section);
	}
}
