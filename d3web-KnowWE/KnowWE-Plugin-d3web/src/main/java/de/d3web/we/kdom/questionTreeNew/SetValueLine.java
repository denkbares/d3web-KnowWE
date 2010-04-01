package de.d3web.we.kdom.questionTreeNew;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Diagnosis;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.values.AnswerChoice;
import de.d3web.scoring.Score;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionDef;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.CreateRelationFailed;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.ObjectCreatedMessage;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SetValueLine extends DefaultAbstractKnowWEObjectType {

	private static final String SETVALUE_ARGUMENT = "SetValueArgument";

	@Override
	protected void init() {
		this.sectionFinder = new ConditionalAllTextFinder() {

			@Override
			protected boolean condition(String text, Section father) {
				return SplitUtility.containsUnquoted(text, "(")
						&& SplitUtility.containsUnquoted(text, ")");

			}
		};

		AnonymousType argumentType = createArgumentType();
		this.childrenTypes.add(argumentType);
		this.childrenTypes.add(createObjectRefTypeBefore(argumentType));

	}

	private KnowWEObjectType createObjectRefTypeBefore(
			KnowWEObjectType typeAfter) {
		QuestionDef qid = new QuestionDef();
		qid.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR1));
		qid.setSectionFinder(new AllBeforeTypeSectionFinder(typeAfter));
		qid.addReviseSubtreeHandler(new CreateSetValueRuleHandler());
		return qid;
	}

	private AnonymousType createArgumentType() {
		AnonymousType typeDef = new AnonymousType(SETVALUE_ARGUMENT);
		SectionFinder typeFinder = new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text,
					Section father) {

				return SectionFinderResult
						.createSingleItemList(new SectionFinderResult(
								SplitUtility.indexOfUnquoted(text, "("),
								SplitUtility.indexOfUnquoted(text, ")") + 1));
			}
		};
		typeDef.setSectionFinder(typeFinder);
		typeDef.setCustomRenderer(new ArgumentRenderer());
		return typeDef;
	}

	static class ArgumentRenderer extends KnowWEDomRenderer {

		@Override
		public void render(KnowWEArticle article, Section sec,
				KnowWEUserContext user, StringBuilder string) {
			String embracedContent = sec.getOriginalText().substring(1,
					sec.getOriginalText().length() - 1);
			string
					.append(KnowWEUtils
							.maskHTML(" <img height='10' src='KnowWEExtension/images/arrow_right_s.png'>"));
			string.append(KnowWEUtils
					.maskHTML("<b>" + embracedContent + "</b>"));

		}

	}

	static class CreateSetValueRuleHandler implements ReviseSubTreeHandler {

		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {

			// current DashTreeElement
			Section<DashTreeElement> element = KnowWEObjectTypeUtils
					.getAncestorOfType(s, new DashTreeElement());
			// get dashTree-father
			
			KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(article.getWeb())
					.getKBM(article, s);
			
			Question q = mgn.findQuestion(trimQuotes(s));
			
			String argument = getArgumentString(s);
			
			
			
			if(q != null) {
				AnswerChoice a = null;
				if(q instanceof QuestionChoice) {
					QuestionChoice qc = (QuestionChoice )q;
					List<AnswerChoice> allAlternatives = qc.getAllAlternatives();
					for (AnswerChoice answerChoice : allAlternatives) {
						if(answerChoice.getName().equals(argument)) {
							a = answerChoice;
						}
					}
					if(a != null) {
						String newRuleID = mgn.createRuleID();

						Condition cond = Utils.createCondition(DashTreeElement.getDashTreeAncestors(element));
						
						Rule r = RuleFactory.createSetValueRule(newRuleID, qc, new Object[]{a}, cond, null);
						if (r != null) {
							return new ObjectCreatedMessage(r.getClass() + " : "
									+ r.getId());
						}
						
					}
				}
				if(q instanceof QuestionNum) {
					Double d = null;
					try {
						d = Double.parseDouble(argument);
					} catch (NumberFormatException e) {
						return new InvalidNumberError(argument);
					}
					
					if(d != null) {
						String newRuleID = mgn.createRuleID();
						Condition cond = Utils.createCondition(DashTreeElement.getDashTreeAncestors(element));
						Rule r  = RuleFactory.createAddValueRule(newRuleID, q, new Object[]{d},cond);
						if (r != null) {
							return new ObjectCreatedMessage(r.getClass() + " : "
									+ r.getId());
						}
					}
					
				}
			}
			
			Diagnosis d = mgn.findDiagnosis(s.getOriginalText());
			if( d != null) {
				Score score = null;
				List<Score> allScores = Score.getAllScores();
				for (Score sc : allScores) {
					if(sc.getSymbol().equals(argument)) {
						score = sc;
						break;
					}
				}
				
				if(score != null) {
					String newRuleID = mgn.createRuleID();

					Condition cond = Utils.createCondition(DashTreeElement.getDashTreeAncestors(element));
					
					Rule r = RuleFactory.createHeuristicPSRule(newRuleID, d, score, cond);
					if (r != null) {
						return new ObjectCreatedMessage(r.getClass() + " : "
								+ r.getId());
					}
				}
			}
			
			return new CreateRelationFailed(Rule.class.getSimpleName());
			
		}

		private String getArgumentString(Section s) {
			String argument = null;
			List<Section<AnonymousType>> children = new ArrayList<Section<AnonymousType>>();
			s.getFather().findSuccessorsOfType(AnonymousType.class, children);
			for (Section<AnonymousType> section : children) {
				if (section.get().getName().equals(SETVALUE_ARGUMENT)) {
					argument = section.getOriginalText().substring(1,
							section.getOriginalText().length() - 1).trim();
					break;
				}
			}
			return argument;
		}

	}
	
	public static String trimQuotes(Section s) {
		String content = s.getOriginalText();
		
		String trimmed = content.trim();
		
		if(trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
			return trimmed.substring(1, trimmed.length()-1).trim();
		}
		
		return trimmed;
	}
}
