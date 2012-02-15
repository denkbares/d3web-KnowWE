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

package de.d3web.we.kdom.questionTree.setValue;

import java.util.Collection;
import java.util.List;

import de.d3web.abstraction.ActionSetValue;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.kdom.questionTree.QuestionDashTreeUtils;
import de.d3web.we.object.AnswerReference;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.SplitUtility;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;

public class QuestionSetValueLine extends AbstractType {

	private static final String SETVALUE_ARGUMENT = "SetValueArgument";
	private static final String OPEN = "(";
	private static final String CLOSE = ")";

	public QuestionSetValueLine() {
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section<?> father) {
				return SplitUtility.containsUnquoted(text, OPEN)
						&& SplitUtility.containsUnquoted(text, CLOSE);

			}
		};

		AnswerPart argumentType = new AnswerPart();
		this.childrenTypes.add(argumentType);
		this.childrenTypes.add(createObjectRefTypeBefore(argumentType));

	}

	private AbstractType createObjectRefTypeBefore(
			AbstractType typeAfter) {
		QuestionReference qid = new QuestionReference();
		qid.setSectionFinder(new AllBeforeTypeSectionFinder(typeAfter));
		qid.addSubtreeHandler(new CreateSetValueRuleHandler());
		return qid;
	}

	static class CreateSetValueRuleHandler extends D3webSubtreeHandler<QuestionReference> {

		@Override
		public void destroy(KnowWEArticle article, Section<QuestionReference> s) {
			Rule kbr = (Rule) s.getSectionStore().getObject(article, SETVALUE_ARGUMENT);
			if (kbr != null) kbr.remove();
		}

		@Override
		public Collection<Message> create(KnowWEArticle article, Section<QuestionReference> s) {

			Question q = s.get().getTermObject(article, s);

			Section<AnswerReference> answerSec = Sections.findSuccessor(
					s.getFather(), AnswerReference.class);

			String answerName = answerSec.get().getAnswerName(answerSec);

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
						Condition cond = QuestionDashTreeUtils.createCondition(article,
								DashTreeUtils.getAncestorDashTreeElements(s));
						if (cond == null) {
							return Messages.asList(Messages.creationFailedWarning(
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
							r = RuleFactory.createRule(ac, cond, null,
									PSMethodAbstraction.class);
						}

						if (r != null) {
							KnowWEUtils.storeObject(article, s, SETVALUE_ARGUMENT, r);
							return Messages.asList(Messages.objectCreatedNotice(
									r.getClass().toString()));
						}

					}
				}
			}

			return Messages.asList(Messages.creationFailedWarning(
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
	class AnswerPart extends AbstractType {

		public AnswerPart() {
			this.sectionFinder = new SectionFinder() {

				@Override
				public List<SectionFinderResult> lookForSections(String text,
						Section<?> father, Type type) {

					return SectionFinderResult
							.createSingleItemList(new SectionFinderResult(
									SplitUtility.indexOfUnquoted(text, OPEN),
									SplitUtility.indexOfUnquoted(text, CLOSE) + 1));
				}
			};

			AnswerReferenceInsideBracket answerReferenceInsideBracket = new AnswerReferenceInsideBracket();
			answerReferenceInsideBracket.setSectionFinder(new SectionFinder() {

				@Override
				public List<SectionFinderResult> lookForSections(String text,
						Section<?> father, Type type) {

					return SectionFinderResult
							.createSingleItemList(new SectionFinderResult(
									1,
									text.length() - 1));
				}
			});
			this.addChildType(answerReferenceInsideBracket);
		}

		class AnswerReferenceInsideBracket extends AnswerReference {

			@Override
			public Section<QuestionReference> getQuestionSection(Section<? extends AnswerReference> s) {
				return Sections.findSuccessor(s.getFather().getFather(),
						QuestionReference.class);
			}

		}

	}

}
