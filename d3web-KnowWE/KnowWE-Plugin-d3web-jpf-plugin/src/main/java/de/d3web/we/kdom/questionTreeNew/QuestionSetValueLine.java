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
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.AnonymousType;
import de.d3web.we.kdom.dashTree.DashTreeUtils;
import de.d3web.we.kdom.objects.AnswerReference;
import de.d3web.we.kdom.objects.QuestionReference;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class QuestionSetValueLine extends DefaultAbstractKnowWEObjectType {

	private static final String SETVALUE_ARGUMENT = "SetValueArgument";

	@Override
	protected void init() {
		this.sectionFinder = new ConditionalAllTextFinder() {

			@Override
			protected boolean condition(String text, Section<?> father) {
				return SplitUtility.containsUnquoted(text, "(")
						&& SplitUtility.containsUnquoted(text, ")");

			}
		};

		AnswerReferenceInBrackets argumentType = new AnswerReferenceInBrackets();
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



			Question q = s.get().getTermObject(article, s);

			Section<AnswerReference> answerSec = s.getFather().findSuccessor(AnswerReference.class);
			
			String answerTermName = answerSec.get().getTermName(answerSec);

			// TODO: shouldnt be necessary
			String answerName = answerTermName.substring(answerTermName.indexOf(' ')).trim();
			
			if(q != null) {
				Choice a = null;
				if(q instanceof QuestionChoice) {
					QuestionChoice qc = (QuestionChoice )q;
					List<Choice> allAlternatives = qc.getAllAlternatives();
					for (Choice answerChoice : allAlternatives) {
						if(answerChoice.getName().equals(answerName)) {
							a = answerChoice;
							break;
						}
					}
					if(a != null) {
						String newRuleID = getKBM(article).createRuleID();

						Condition cond = Utils.createCondition(article,
								DashTreeUtils.getAncestorDashTreeElements(s));
						if(cond == null) {
							return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(Rule.class.getSimpleName()));
						}
						
						Rule r = RuleFactory.createSetValueRule(newRuleID, qc, new Object[]{a}, cond, null);
						if (r != null) {
							return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(r.getClass() + " : "
									+ r.getId()));
						}

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
