package de.d3web.we.kdom.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NoSuchObjectError;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.utils.KnowWEUtils;

public class QuestionNumReference extends QuestionReference {

	public QuestionNumReference() {
		super();
		this.subtreeHandler.clear();
		this.addSubtreeHandler(Priority.HIGH, new QuestionNumRegistrationHandler());

	}

	class QuestionNumRegistrationHandler extends SubtreeHandler<QuestionNumReference> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<QuestionNumReference> s) {

			KnowWEUtils.getTerminologyHandler(article.getWeb())
					.registerTermReference(article, s);

			Question question = s.get().getTermObject(article, s);

			if (question == null) {
				return Arrays.asList((KDOMReportMessage) new NoSuchObjectError(
						s.get().getName()
								+ ": " + s.get().getTermName(s)));
			}

			// check for QuestionNum
			if (!(question instanceof QuestionNum)) {
				return Arrays.asList((KDOMReportMessage) new NoSuchObjectError(
						s.get().getName()
								+ " QuestionNum expected:  " + s.get().getTermName(s)));
			}

			return new ArrayList<KDOMReportMessage>(0);
		}

		@Override
		public void destroy(KnowWEArticle article, Section<QuestionNumReference> s) {
			KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermReference(
					article, s);
		}

	}
}
