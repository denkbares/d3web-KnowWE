package de.d3web.we.kdom.questionTreeNew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionRef;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEObjectTypeUtils;

public class QuestionRefLine extends DefaultAbstractKnowWEObjectType {

	public static final String REF_KEYWORD = "&REF";

	public QuestionRefLine() {

	// every line containing [...] (unquoted) is recognized as QuestionLine
	this.sectionFinder = new ConditionalAllTextFinder() {
		@Override
		protected boolean condition(String text, Section father) {
				return text.trim().startsWith(REF_KEYWORD);
		}
	};

		// take the keyword
		AnonymousType key = new AnonymousType("ref-key");
		key.setSectionFinder(new StringSectionFinderUnquoted(REF_KEYWORD));
		this.childrenTypes.add(key);

		// the rest for the name of the question
		QuestionRef questionRef = new QuestionRef();
		questionRef.setSectionFinder(new AllTextFinderTrimmed());
		this.childrenTypes
				.add(questionRef);

		this.addSubtreeHandler(new CreateIndicationHandler());

	}

	/**
	 * This handler creates an indication rule if a question if son of an answer
	 * if a preceeding question
	 *
	 * @author Jochen
	 *
	 */
	static class CreateIndicationHandler extends SubtreeHandler<QuestionRefLine> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<QuestionRefLine> qRefLine) {

			if (qRefLine.hasErrorInSubtree()) {
				return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
						"indication rule"));
			}
			Section<QuestionRef> qrefSection = qRefLine.findSuccessor(QuestionRef.class);

			// current DashTreeElement
			Section<DashTreeElement> element = KnowWEObjectTypeUtils
					.getAncestorOfType(qRefLine, new DashTreeElement());
			// get dashTree-father
			Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
					.getDashTreeFather(element);

			Section<QuestionTreeAnswerDef> answerSec = dashTreeFather
					.findSuccessor(QuestionTreeAnswerDef.class);
			Section<NumericCondLine> numCondSec = dashTreeFather
					.findSuccessor(NumericCondLine.class);

			if (answerSec != null || numCondSec != null) {

				KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(
						article.getWeb())
						.getKBM(article, this, qRefLine);

				String newRuleID = mgn.createRuleID();

				Condition cond = Utils.createCondition(DashTreeElement.getDashTreeAncestors(element));

				Rule r = RuleFactory.createIndicationRule(newRuleID, qrefSection
						.get().getObject(qrefSection), cond);
				if (r != null) {
					return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
							r.getClass() + " : "
									+ r.getId()));
				}
				else {
					return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
							Rule.class.getSimpleName()));
				}
			}

			return new ArrayList<KDOMReportMessage>(0);
		}

	}
}
