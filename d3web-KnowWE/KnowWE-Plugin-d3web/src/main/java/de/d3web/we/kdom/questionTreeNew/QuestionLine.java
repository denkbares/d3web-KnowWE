package de.d3web.we.kdom.questionTreeNew;

import java.util.List;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.info.MMInfoSubject;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.SingleChildConstraint;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionDef;
import de.d3web.we.kdom.objects.QuestionDef.QuestionType;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.report.message.ObjectCreationError;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.kdom.sectionFinder.MatchUntilEndFinder;
import de.d3web.we.kdom.sectionFinder.OneOfStringEnumFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.sectionFinder.StringEnumChecker;
import de.d3web.we.kdom.sectionFinder.StringSectionFinderUnquoted;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.utils.SplitUtility;

public class QuestionLine extends DefaultAbstractKnowWEObjectType {

	public QuestionLine() {

		// every line containing [...] (unquoted) is recognized as QuestionLine
		this.sectionFinder = new ConditionalAllTextFinder() {
			@Override
			protected boolean condition(String text, Section father) {
				return SplitUtility.containsUnquoted(text, "[")
						&& SplitUtility.containsUnquoted(text, "]")
						&& !text.startsWith("[");
			}
		};

		//type of the question '[oc]'
		this.childrenTypes.add(new QuestionTypeDeclaration());
		// abstract flag: '<abstract>'
		this.childrenTypes.add(new AbstractFlag());
		 // questiontext - startet by '~'
		this.childrenTypes.add(new QuestionText());

		// finally the name of the question
		this.childrenTypes
				.add(new QuestionDefQTree());
	}


	static class QuestionDefQTree extends QuestionDef {

		@Override
		protected void init() {
			SectionFinder f = new AllTextFinderTrimmed();
			f.addConstraint(SingleChildConstraint.getInstance());
			this.setSectionFinder(f);
			this.addSubtreeHandler(new CreateIndicationHandler());
		}
		@Override
		public de.d3web.we.kdom.objects.QuestionDef.QuestionType getQuestionType(Section<QuestionDef> s) {
			return QuestionTypeDeclaration
					.getQuestionType(s.getFather().findSuccessor(
							QuestionTypeDeclaration.class));
		}

	}


	static class CreateIndicationHandler implements SubtreeHandler<QuestionDefQTree> {

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section<QuestionDefQTree> qidSection) {


			// current DashTreeElement
			Section<DashTreeElement> element = KnowWEObjectTypeUtils
					.getAncestorOfType(qidSection, new DashTreeElement());
			// get dashTree-father
			Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
					.getDashTreeFather(element);

			Section<QuestionTreeAnswerDef> answerSec = dashTreeFather
					.findSuccessor(QuestionTreeAnswerDef.class);
			Section<NumericCondLine> numCondSec = dashTreeFather
			.findSuccessor(NumericCondLine.class);


			if (answerSec != null || numCondSec != null) {

				KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(article.getWeb())
						.getKBM(article, this, qidSection);

				String newRuleID = mgn.createRuleID();

				Condition cond = Utils.createCondition(DashTreeElement.getDashTreeAncestors(element));

				Rule r = RuleFactory.createIndicationRule(newRuleID, qidSection
						.get().getObject(qidSection), cond);
				if (r != null) {
					return new ObjectCreatedMessage(r.getClass() + " : "
							+ r.getId());
				} else {
					return new CreateRelationFailed(Rule.class.getSimpleName());
				}
			}


			return null;
		}

	}


	static class AbstractFlag extends DefaultAbstractKnowWEObjectType {

		public AbstractFlag() {
			this.sectionFinder = new OneOfStringEnumFinder(new String[] {
					"<abstract>", "<abstrakt>" });
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));

			this.addSubtreeHandler(new SubtreeHandler<AbstractFlag>() {

				@Override
				public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section<AbstractFlag> s) {


					Section<QuestionDef> qDef = s.getFather().findSuccessor(
							QuestionDef.class);

					if (qDef != null) {

						Question question = qDef.get().getObject(qDef);
						question.getProperties().setProperty(
								Property.ABSTRACTION_QUESTION, true);
						return new ObjectCreatedMessage("abstract question");

					}
					return new ObjectCreationError("QuestionText",
							this.getClass());
				}
			});
		}
	}

	static class QuestionText extends DefaultAbstractKnowWEObjectType {

		private static final String QTEXT_START_SYMBOL = "~";

		@Override
		protected void init() {
			this.sectionFinder = new MatchUntilEndFinder(new StringSectionFinderUnquoted(
					QTEXT_START_SYMBOL));

			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR8));
			this.addSubtreeHandler(new SubtreeHandler<QuestionText>() {

				@Override
				public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section<QuestionText> sec) {


					Section<QuestionDef> qDef = sec.getFather().findSuccessor(
							QuestionDef.class);

					if (qDef != null) {

						Question question = qDef.get().getObject(qDef);

						if (question != null) {
							D3webUtils.addMMInfo(question, "LT",
									MMInfoSubject.PROMPT.getName(),
									QuestionText.getQuestionText(sec), null);
							return new ObjectCreatedMessage("QuestionText created");
						}
					}
					return new ObjectCreationError("QuestionText",
							this.getClass());
				}
			});
		}

		public static String getQuestionText(Section<QuestionText> s) {
			String text = s.getOriginalText();
			if (text.startsWith(QTEXT_START_SYMBOL)) {
				text = text.substring(1).trim();
			}

			return SplitUtility.unquote(text);
		}
	}

	/**
	 * @author Jochen
	 *
	 *         A KnowWEType for the question-type declaration keys
	 *         "[oc],[mc],[num],..."
	 * 
	 */
	static class QuestionTypeDeclaration extends
			DefaultAbstractKnowWEObjectType {



		public static QuestionType getQuestionType(Section<QuestionTypeDeclaration> typeSection) {

			if (typeSection == null)
				return null;
			String embracedContent = typeSection.getOriginalText();
			if (embracedContent.contains("oc")) {
				return QuestionType.OC;
			} else if (embracedContent.contains("mc")) {
				return QuestionType.MC;
			} else if (embracedContent.contains("num")) {
				return QuestionType.NUM;
			} else if (embracedContent.contains("jn")
					|| embracedContent.contains("yn")) {
				return QuestionType.YN;
			} else if (embracedContent.contains("date")) {
				return QuestionType.DATE;
			} else if (embracedContent.contains("text")) {
				return QuestionType.TEXT;
			} else {
				return null;
			}

		}

		public static final String[] QUESTION_DECLARATIONS = { "oc", "mc",
				"yn", "jn", "num", "date", "text" };

		@Override
		protected void init() {
			SectionFinder typeFinder = new SectionFinder() {

				@Override
				public List<SectionFinderResult> lookForSections(String text,
						Section father) {

					return SectionFinderResult
							.createSingleItemList(new SectionFinderResult(
									SplitUtility.indexOfUnquoted(text, "["),
									SplitUtility.indexOfUnquoted(text, "]") + 1));
				}
			};
			this.setSectionFinder(typeFinder);
			this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));
			this.addSubtreeHandler(new StringEnumChecker<QuestionTypeDeclaration>(
					QUESTION_DECLARATIONS, new SimpleMessageError(
							"Invalid Question type - allowing only: "
									+ QUESTION_DECLARATIONS.toString())));
		}
	}

}
