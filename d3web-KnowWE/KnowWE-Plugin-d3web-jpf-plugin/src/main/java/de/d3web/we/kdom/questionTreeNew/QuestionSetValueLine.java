/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.questionTreeNew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionNum;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.scoring.Score;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.QuestionReference;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class QuestionSetValueLine extends DefaultAbstractKnowWEObjectType {

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
		QuestionReference qid = new QuestionReference();
		qid.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR1));
		qid.setSectionFinder(AllBeforeTypeSectionFinder.createFinder(typeAfter));
		qid.addSubtreeHandler(new CreateSetValueRuleHandler());
		return qid;
	}

	private AnonymousType createArgumentType() {
		AnonymousType typeDef = new AnonymousType(SETVALUE_ARGUMENT);
		SectionFinder typeFinder = new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text,
					Section father, KnowWEObjectType type) {

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
					.maskHTML("<b>(" + embracedContent + ")</b>"));

		}

	}

	static class CreateSetValueRuleHandler extends D3webSubtreeHandler<QuestionReference> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<QuestionReference> s) {

			// current DashTreeElement
			Section<DashTreeElement> element = KnowWEObjectTypeUtils
					.getAncestorOfType(s, DashTreeElement.class);
			// get dashTree-father

			KnowledgeBaseManagement mgn = getKBM(article);

			Question q = mgn.findQuestion(trimQuotes(s));

			String argument = getArgumentString(s);



			if(q != null) {
				Choice a = null;
				if(q instanceof QuestionChoice) {
					QuestionChoice qc = (QuestionChoice )q;
					List<Choice> allAlternatives = qc.getAllAlternatives();
					for (Choice answerChoice : allAlternatives) {
						if(answerChoice.getName().equals(argument)) {
							a = answerChoice;
						}
					}
					if(a != null) {
						String newRuleID = mgn.createRuleID();

						Condition cond = Utils.createCondition(article, DashTreeElement.getDashTreeAncestors(element));

						Rule r = RuleFactory.createSetValueRule(newRuleID, qc, new Object[]{a}, cond, null);
						if (r != null) {
							return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(r.getClass() + " : "
									+ r.getId()));
						}

					}
				}
				if(q instanceof QuestionNum) {
					Double d = null;
					try {
						d = Double.parseDouble(argument);
					} catch (NumberFormatException e) {
						return Arrays.asList((KDOMReportMessage) new de.d3web.we.kdom.report.message.InvalidNumberError(
								argument));
					}

					if(d != null) {
						String newRuleID = mgn.createRuleID();
						Condition cond = Utils.createCondition(article, DashTreeElement.getDashTreeAncestors(element));
						Rule r  = RuleFactory.createAddValueRule(newRuleID, q, new Object[]{d},cond);
						if (r != null) {
							return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(r.getClass()
									+ " : " + r.getId()));
						}
					}

				}
			}

			Solution d = mgn.findSolution(s.getOriginalText());
			if( d != null) {
				Score score = D3webUtils.getScoreForString(argument);

				if(score != null) {
					String newRuleID = mgn.createRuleID();

					Condition cond = Utils.createCondition(article, DashTreeElement.getDashTreeAncestors(element));

					Rule r = RuleFactory.createHeuristicPSRule(newRuleID, d, score, cond);
					if (r != null) {
						return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(r.getClass()
								+ " : " + r.getId()));
					}
				}
			}

			return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(Rule.class.getSimpleName()));

		}

		private String getArgumentString(Section<QuestionReference> s) {
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

	public static String trimQuotes(Section<?> s) {
		String content = s.getOriginalText();

		String trimmed = content.trim();

		if(trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
			return trimmed.substring(1, trimmed.length()-1).trim();
		}

		return trimmed;
	}
}
