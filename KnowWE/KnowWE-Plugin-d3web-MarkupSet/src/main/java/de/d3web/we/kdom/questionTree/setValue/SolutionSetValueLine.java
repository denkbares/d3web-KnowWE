/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.RuleFactory;
import de.d3web.scoring.Score;
import de.d3web.we.kdom.questionTree.QuestionDashTreeUtils;
import de.d3web.we.object.SolutionReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.KnowWERenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.SplitUtility;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;

/**
 * 
 * @author Jochen
 * @created 21.07.2010
 */
public class SolutionSetValueLine extends AbstractType {

	private static final String SETVALUE_ARGUMENT = "SolutionScore";
	private static final String OPEN = "(";
	private static final String CLOSE = ")";

	public SolutionSetValueLine() {
		this.sectionFinder = new SolutionSetValueFinder();
		AnonymousType argumentType = createArgumentType();
		this.childrenTypes.add(argumentType);
		this.childrenTypes.add(createObjectRefTypeBefore(argumentType));

	}

	class SolutionSetValueFinder extends ConditionalSectionFinder {

		public SolutionSetValueFinder() {
			super(new AllTextSectionFinder());
		}

		@Override
		protected boolean condition(String text, Section<?> father) {
			int open = SplitUtility.indexOfUnquoted(text, (OPEN));
			if (open == -1) return false;

			int close = SplitUtility.findIndexOfClosingBracket(text, open, OPEN.charAt(0),
					CLOSE.charAt(0));

			if (close == -1) return false;

			String content = text.substring(open + 1, close).trim();

			List<String> scores = D3webUtils.getPossibleScores();
			for (String string : scores) {
				if (content.equals(string)) {
					return true;
				}
			}

			return false;
		}

	}

	private AbstractType createObjectRefTypeBefore(
			AbstractType typeAfter) {
		SolutionReference sid = new SolutionReference();
		sid.setSectionFinder(new AllBeforeTypeSectionFinder(typeAfter));
		sid.addSubtreeHandler(new CreateScoringRuleHandler());
		return sid;
	}

	private AnonymousType createArgumentType() {
		AnonymousType typeDef = new AnonymousType(SETVALUE_ARGUMENT);
		SectionFinder typeFinder = new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text,
					Section<?> father, Type type) {

				return SectionFinderResult
						.createSingleItemList(new SectionFinderResult(
								SplitUtility.indexOfUnquoted(text, OPEN),
								SplitUtility.indexOfUnquoted(text, CLOSE) + 1));
			}
		};
		typeDef.setSectionFinder(typeFinder);
		typeDef.setRenderer(new ArgumentRenderer());
		return typeDef;
	}

	static class ArgumentRenderer implements KnowWERenderer<AnonymousType> {

		@Override
		public void render(Section<AnonymousType> sec, UserContext user,
				StringBuilder string) {
			String embracedContent = sec.getText().substring(1,
					sec.getText().length() - 1);
			string
					.append(KnowWEUtils
							.maskHTML(" <img height='10' src='KnowWEExtension/images/arrow_right_s.png'>"));
			string.append(KnowWEUtils
					.maskHTML("<b>(" + embracedContent + ")</b>"));

		}

	}

	static class CreateScoringRuleHandler extends D3webSubtreeHandler<SolutionReference> {

		@Override
		public void destroy(KnowWEArticle article, Section<SolutionReference> s) {
			Rule kbr = (Rule) s.getSectionStore().getObject(article,
					SETVALUE_ARGUMENT);
			if (kbr != null) kbr.remove();
		}

		@Override
		public Collection<Message> create(KnowWEArticle article, Section<SolutionReference> s) {

			Solution sol = s.get().getTermObject(article, s);

			String argument = getArgumentString(s);

			if (sol != null) {
				Score score = D3webUtils.getScoreForString(argument);

				if (score != null) {

					Condition cond = QuestionDashTreeUtils.createCondition(article,
							DashTreeUtils.getAncestorDashTreeElements(s));
					if (cond != null) {
						Rule r = RuleFactory.createHeuristicPSRule(sol, score, cond);
						if (r != null) {
							KnowWEUtils.storeObject(article, s, SETVALUE_ARGUMENT, r);
							return Messages.asList(Messages.objectCreatedNotice(
									r.getClass().toString()));
						}
					}
				}
			}

			return Arrays.asList(Messages.creationFailedWarning(
					Rule.class.getSimpleName()));

		}

		private String getArgumentString(Section<SolutionReference> s) {
			String argument = null;
			List<Section<AnonymousType>> children = new ArrayList<Section<AnonymousType>>();
			Sections.findSuccessorsOfType(s.getFather(), AnonymousType.class, children);
			for (Section<AnonymousType> section : children) {
				if (section.get().getName().equals(SETVALUE_ARGUMENT)) {
					argument = section.getText().substring(1,
							section.getText().length() - 1).trim();
					break;
				}
			}
			return argument;
		}

	}

}
