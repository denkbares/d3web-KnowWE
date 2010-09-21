/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.kdom.questionTreeNew.setValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.abstraction.ActionSetValue;
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
import de.d3web.we.kdom.dashTree.DashTreeUtils;
import de.d3web.we.kdom.objects.AnswerReference;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.objects.QuestionReference;
import de.d3web.we.kdom.questionTreeNew.QuestionDashTreeUtils;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.sectionFinder.ConditionalSectionFinder;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SplitUtility;

public class QuestionSetValueLine extends DefaultAbstractKnowWEObjectType {

	private static final String SETVALUE_ARGUMENT = "SetValueArgument";
	private static final String OPEN = "(";
	private static final String CLOSE = ")";

	/**
	 *
	 */
	@Override
	protected void init() {
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section<?> father) {
				return SplitUtility.containsUnquoted(text, OPEN)
						&& SplitUtility.containsUnquoted(text, CLOSE);

			}
		};

		AnswerReferenceInBrackets argumentType = new AnswerReferenceInBrackets();
		this.childrenTypes.add(argumentType);
		this.childrenTypes.add(createObjectRefTypeBefore(argumentType));

	}

	private KnowWEObjectType createObjectRefTypeBefore(
			KnowWEObjectType typeAfter) {
		QuestionReference qid = new QuestionReference();
		qid.setSectionFinder(AllBeforeTypeSectionFinder.createFinder(typeAfter));
		qid.addSubtreeHandler(new CreateSetValueRuleHandler());
		return qid;
	}

	static class CreateSetValueRuleHandler extends D3webSubtreeHandler<QuestionReference> {

		@Override
		public boolean needsToCreate(KnowWEArticle article, Section<QuestionReference> s) {
			return super.needsToCreate(article, s)
					|| QuestionDashTreeUtils.isChangeInRootQuestionSubtree(article, s);
		}

		@Override
		public boolean needsToDestroy(KnowWEArticle article, Section<QuestionReference> s) {
			return super.needsToDestroy(article, s)
					|| QuestionDashTreeUtils.isChangeInRootQuestionSubtree(article, s);
		}

		@Override
		public void destroy(KnowWEArticle article, Section<QuestionReference> s) {
			Rule kbr = (Rule) KnowWEUtils.getObjectFromLastVersion(article, s,
					SETVALUE_ARGUMENT);
			if (kbr != null) kbr.remove();
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<QuestionReference> s) {

			Question q = s.get().getTermObject(article, s);

			Section<AnswerReference> answerSec = s.getFather().findSuccessor(
					AnswerReference.class);

			String answerName = answerSec.get().getTermName(answerSec);

			if (q != null) {
				Choice a = null;
				if (q instanceof QuestionChoice) {
					QuestionChoice qc = (QuestionChoice) q;
					List<Choice> allAlternatives = qc.getAllAlternatives();
					for (Choice answerChoice : allAlternatives) {
						if (answerChoice.getName().equals(answerName)) {
							a = answerChoice;
							break;
						}
					}

					if (a != null) {
						String newRuleID = getKBM(article).createRuleID();

						Condition cond = QuestionDashTreeUtils.createCondition(article,
								DashTreeUtils.getAncestorDashTreeElements(s));
						if (cond == null) {
							return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
									Rule.class.getSimpleName() + ": check condition"));
						}

						ActionSetValue ac = null;
						if (q != null && a != null) {
							ac = new ActionSetValue();
							ac.setQuestion(q);
							ac.setValue(a);
						}

						Rule r = null;
						if (ac != null) {
							r = RuleFactory.createRule(newRuleID, ac, cond, null, null);
						}

						if (r != null) {
							KnowWEUtils.storeSectionInfo(article, s, SETVALUE_ARGUMENT, r);
							return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage(
									r.getClass() + " : "
											+ r.getId()));
						}

					}
				}
			}

			return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
					Rule.class.getSimpleName()));

		}

	}

	/**
	 * 
	 * A type for an AnswerReference in brackets like '(AnswerXY)'
	 * 
	 * @author Jochen
	 * @created 26.07.2010
	 */
	class AnswerReferenceInBrackets extends AnswerReference {

		public AnswerReferenceInBrackets() {
			this.sectionFinder = new ISectionFinder() {

				@Override
				public List<SectionFinderResult> lookForSections(String text,
						Section father, KnowWEObjectType type) {

					return SectionFinderResult
							.createSingleItemList(new SectionFinderResult(
									SplitUtility.indexOfUnquoted(text, OPEN),
									SplitUtility.indexOfUnquoted(text, CLOSE) + 1));
				}
			};
		}

		@Override
		public Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s) {
			return s.getFather().findSuccessor(QuestionReference.class);
		}

		@Override
		public String getTermName(Section<? extends KnowWETerm<Choice>> s) {
			String text = s.getOriginalText().trim();
			String answer = "";
			if (text.indexOf(OPEN) == 0 && text.lastIndexOf(CLOSE) == text.length() - 1) {
				answer = text.substring(1, text.length() - 1).trim();
			}

			return KnowWEUtils.trimQuotes(answer);
		}

	}

}
