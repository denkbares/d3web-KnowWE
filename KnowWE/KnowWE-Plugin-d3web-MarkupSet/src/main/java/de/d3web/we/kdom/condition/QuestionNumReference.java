package de.d3web.we.kdom.condition;

import java.util.ArrayList;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;

public class QuestionNumReference extends QuestionReference {

	public QuestionNumReference() {
		super();
		this.clearSubtreeHandlers();
		this.addSubtreeHandler(Priority.HIGH, new QuestionNumRegistrationHandler());

	}

	class QuestionNumRegistrationHandler extends SubtreeHandler<QuestionNumReference> {

		@Override
		public Collection<Message> create(KnowWEArticle article, Section<QuestionNumReference> s) {

			KnowWEUtils.getTerminologyHandler(article.getWeb())
					.registerTermReference(article, s);

			Question question = s.get().getTermObject(article, s);

			if (question == null) {
				return Messages.asList(Messages.noSuchObjectError(
						s.get().getName()
								+ ": " + s.get().getTermIdentifier(s)));
			}

			// check for QuestionNum
			if (!(question instanceof QuestionNum)) {
				return Messages.asList(Messages.noSuchObjectError(
						s.get().getName()
								+ " QuestionNum expected:  " + s.get().getTermIdentifier(s)));
			}

			return new ArrayList<Message>(0);
		}

		@Override
		public void destroy(KnowWEArticle article, Section<QuestionNumReference> s) {
			KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermReference(
					article, s);
		}

	}
}
