package de.d3web.we.kdom.condition;

import java.util.Collection;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

public class QuestionNumReference extends QuestionReference {

	public QuestionNumReference() {
		this.addCompileScript(Priority.HIGH, new QuestionNumRegistrationHandler());
	}

	@Override
	public Class<?> getTermObjectClass(Section<? extends Term> section) {
		return QuestionNum.class;
	}

	class QuestionNumRegistrationHandler implements D3webHandler<QuestionNumReference> {

		@Override
		public Collection<Message> create(D3webCompiler compiler, Section<QuestionNumReference> section) {

			Question question = section.get().getTermObject(compiler, section);

			String name = section.get().getTermName(section);
			if (question == null) {
				// there will already be a message from QuestionReference..
				return Messages.noMessage();
			}

			// check for QuestionNum
			if (!(question instanceof QuestionNum)) {
				return Messages.asList(Messages.error("Expected numeric question (QuestionNum), but '"
						+ name + "' is of the type '" + question.getClass().getSimpleName() + "'"));
			}

			return Messages.noMessage();
		}
	}
}
