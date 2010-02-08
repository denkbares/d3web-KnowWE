package de.d3web.we.kdom.questionTreeNew;

import java.util.List;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.Rule;
import de.d3web.kernel.domainModel.RuleFactory;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondEqual;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.objects.QuestionID;
import de.d3web.we.kdom.objects.QuestionTreeAnswerID;
import de.d3web.we.kdom.objects.QuestionnaireID;
import de.d3web.we.kdom.questionTreeNew.QuestionLine.QuestionTypeDeclaration.QuestionType;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.CreateRelationFailed;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.NewObjectCreated;
import de.d3web.we.kdom.report.ObjectAlreadyDefinedWarning;
import de.d3web.we.kdom.report.ObjectCreatedMessage;
import de.d3web.we.kdom.report.ObjectCreationError;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.sectionFinder.StringEnumChecker;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class QuestionLine extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {

		// every line containing [...] (unquoted) is recognized as QuestionLine
		this.sectionFinder = new ConditionalAllTextFinder() {
			@Override
			protected boolean condition(String text, Section father) {
				return SplitUtility.containsUnquoted(text, "[")
						&& SplitUtility.containsUnquoted(text, "]")
						&& !text.startsWith("[");
			}
		};

		QuestionTypeDeclaration typeDeclarationType = new QuestionTypeDeclaration();
		this.childrenTypes.add(typeDeclarationType);
		this.childrenTypes
				.add(createQuestionDefTypeBefore(typeDeclarationType));
	}

	private KnowWEObjectType createQuestionDefTypeBefore(
			KnowWEObjectType typeAfter) {
		QuestionID qid = new QuestionID();
		qid.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR3));
		qid.setSectionFinder(new AllBeforeTypeSectionFinder(typeAfter));
		qid.addReviseSubtreeHandler(new CreateQuestionHandler());
		qid.addReviseSubtreeHandler(new CreateIndicationHandler());
		return qid;
	}

	static class CreateIndicationHandler implements ReviseSubTreeHandler {

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
			Section<QuestionID> qidSection = ((Section<QuestionID>) s);

			String name = qidSection.get().getID(qidSection);

			// current DashTreeElement
			Section<DashTreeElement> element = KnowWEObjectTypeUtils
					.getAncestorOfType(s, new DashTreeElement());
			// get dashTree-father
			Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
					.getDashTreeFather(element);

			Section<QuestionTreeAnswerID> answerSec = dashTreeFather
					.findSuccessor(new QuestionTreeAnswerID());
			Section<NumericCondLine> numCondSec = dashTreeFather
			.findSuccessor(new NumericCondLine());

			
			if (answerSec != null || numCondSec != null) {

				KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(article.getWeb())
						.getKBM(article, qidSection);

				String newRuleID = mgn.findNewIDFor(Rule.class);

				AbstractCondition cond = Utils.createCondition(element);

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

	static class CreateQuestionHandler implements ReviseSubTreeHandler {

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article,
				Section sec) {

			Section<QuestionID> qidSection = ((Section<QuestionID>) sec);

			String name = qidSection.get().getID(qidSection);


			KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(article.getWeb())
					.getKBM(article, sec);

			IDObject o = mgn.findQuestion(name);

			if (o != null) {
				return new ObjectAlreadyDefinedWarning(o.getClass()
						.getSimpleName());
			} else {
				QASet parent = findParent(sec, mgn);

				QuestionType questionType = QuestionTypeDeclaration
						.getQuestionType(qidSection);
				Question q = null;
				if (questionType.equals(QuestionType.OC)) {
					q = mgn.createQuestionOC(name, parent, new String[] {});
				} else if (questionType.equals(QuestionType.MC)) {
					q = mgn.createQuestionMC(name, parent, new String[] {});
				} else if (questionType.equals(QuestionType.NUM)) {
					q = mgn.createQuestionNum(name, parent);
				} else if (questionType.equals(QuestionType.YN)) {
					q = mgn.createQuestionYN(name, parent);
				} else if (questionType.equals(QuestionType.DATE)) {
					q = mgn.createQuestionDate(name, parent);
				} else if (questionType.equals(QuestionType.TEXT)) {
					q = mgn.createQuestionText(name, parent);
				} else {
					// no valid type...
				}

				if (q != null) {

				}
				if (q != null) {
					qidSection.get().storeObject(qidSection, q);
					return new NewObjectCreated(q.getClass().getSimpleName()
							+ " " + q.getText());
				} else {
					return new ObjectCreationError(name, this.getClass());
				}

			}

		}

		private QASet findParent(Section s, KnowledgeBaseManagement mgn) {

			// current DashTreeElement
			Section<DashTreeElement> element = KnowWEObjectTypeUtils
					.getAncestorOfType(s, new DashTreeElement());
			// get dashTree-father
			Section<? extends DashTreeElement> dashTreeFather = DashTreeElement
					.getDashTreeFather(element);

			// climb up tree and look for QASet
			QASet foundAncestorQASet = null;
			while (foundAncestorQASet == null) {
				foundAncestorQASet = findParentObject(dashTreeFather, mgn);
				dashTreeFather = DashTreeElement
						.getDashTreeFather(dashTreeFather);
				if (dashTreeFather == null)
					break;

			}

			if (foundAncestorQASet == null) {
				// root QASet as default parent
				foundAncestorQASet = mgn.getKnowledgeBase().getRootQASet();
			}

			return foundAncestorQASet;
		}

		private QASet findParentObject(
				Section<? extends DashTreeElement> dashTreeElement,
				KnowledgeBaseManagement mgn) {

			if (dashTreeElement.findSuccessor(new QuestionnaireID()) != null) {
				String qClassName = dashTreeElement.findSuccessor(
						new QuestionnaireID()).getOriginalText();
				QASet parent = mgn.findQContainer(qClassName);
				if (parent != null)
					return parent;
			}

			if (dashTreeElement.findSuccessor(new QuestionID()) != null) {
				String qName = dashTreeElement.findSuccessor(new QuestionID())
						.getOriginalText();
				QASet parent = mgn.findQuestion(qName);
				if (parent != null)
					return parent;
			}

			return null;
		}

	}

	static class TypeDeclarationRenderer extends KnowWEDomRenderer {

		@Override
		public void render(KnowWEArticle article, Section sec,
				KnowWEUserContext user, StringBuilder string) {
			String imgTagPrefix = "<img src='KnowWEExtension/images/";
			QuestionType questionType = QuestionTypeDeclaration
					.getQuestionType(sec);
			if (questionType.equals(QuestionType.OC)) {
				string.append(KnowWEUtils.maskHTML(imgTagPrefix
						+ "questionChoice.gif'>"));
			} else if (questionType.equals(QuestionType.MC)) {
				string.append(KnowWEUtils.maskHTML(imgTagPrefix
						+ "questionMC.gif'>"));
			} else if (questionType.equals(QuestionType.NUM)) {
				string.append(KnowWEUtils.maskHTML(imgTagPrefix
						+ "questionNum.gif'>"));
			} else if (questionType.equals(QuestionType.YN)) {
				string.append(KnowWEUtils.maskHTML(imgTagPrefix
						+ "questionYesNo.gif'>"));
			} else if (questionType.equals(QuestionType.DATE)) {
				string.append(KnowWEUtils.maskHTML(imgTagPrefix
						+ "questionDate.gif'>"));
			} else if (questionType.equals(QuestionType.TEXT)) {
				string.append(KnowWEUtils.maskHTML(imgTagPrefix
						+ "questionText.gif'>"));
			} else {
				string.append(sec.getOriginalText());
			}

		}

	}

	static class QuestionTypeDeclaration extends
			DefaultAbstractKnowWEObjectType {

		public static enum QuestionType {
			OC, MC, YN, NUM, DATE, TEXT;
		}

		public static QuestionType getQuestionType(Section<QuestionID> s) {
			Section<QuestionTypeDeclaration> typeSection = s.getFather()
					.findSuccessor(new QuestionTypeDeclaration());
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
			this.setCustomRenderer(new TypeDeclarationRenderer());
			this.addReviseSubtreeHandler(new StringEnumChecker(
					QUESTION_DECLARATIONS, new SimpleMessageError(
							"Invalid Question type - allowing only: "
									+ QUESTION_DECLARATIONS.toString())));
		}
	}

}
