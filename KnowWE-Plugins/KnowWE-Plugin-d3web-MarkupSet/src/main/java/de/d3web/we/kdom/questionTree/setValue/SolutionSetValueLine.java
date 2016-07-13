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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.RuleFactory;
import de.d3web.scoring.Score;
import com.denkbares.strings.Strings;
import de.d3web.we.kdom.questionTree.QuestionDashTreeUtils;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.SolutionReference;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
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
		this.setSectionFinder(new SolutionSetValueFinder());
		AnonymousType argumentType = createArgumentType();
		this.addChildType(argumentType);
		this.addChildType(createObjectRefTypeBefore(argumentType));

	}

	class SolutionSetValueFinder extends ConditionalSectionFinder {

		public SolutionSetValueFinder() {
			super(AllTextFinder.getInstance());
		}

		@Override
		protected boolean condition(String text, Section<?> father) {
			int open = Strings.indexOfUnquoted(text, (OPEN));
			if (open == -1) return false;

			int close = Strings.indexOfClosingBracket(text, open, OPEN.charAt(0),
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

	private Type createObjectRefTypeBefore(
			AbstractType typeAfter) {
		SolutionReference sid = new SolutionReference();
		sid.setSectionFinder(new AllBeforeTypeSectionFinder(typeAfter));
		sid.addCompileScript(new CreateScoringRuleHandler());
		return sid;
	}

	private AnonymousType createArgumentType() {
		AnonymousType typeDef = new AnonymousType(SETVALUE_ARGUMENT);
		SectionFinder typeFinder = new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text,
					Section<?> father, Type type) {

				return SectionFinderResult
						.singleItemList(new SectionFinderResult(
								Strings.indexOfUnquoted(text, OPEN),
								Strings.indexOfUnquoted(text, CLOSE) + 1));
			}
		};
		typeDef.setSectionFinder(typeFinder);
		typeDef.setRenderer(new ArgumentRenderer());
		return typeDef;
	}

	static class ArgumentRenderer implements Renderer {

		@Override
		public void render(Section<?> sec, UserContext user,
				RenderResult string) {
			String embracedContent = sec.getText().substring(1,
					sec.getText().length() - 1);
			string.appendHtml(" <img height='10' src='KnowWEExtension/images/arrow_right_s.png'>");
			string.appendHtml("<b>(");
			string.append(embracedContent);
			string.appendHtml(")</b>");

		}

	}

	static class CreateScoringRuleHandler implements D3webHandler<SolutionReference> {

		@Override
		public void destroy(D3webCompiler compiler, Section<SolutionReference> s) {
			Rule kbr = (Rule) s.getObject(compiler,
					SETVALUE_ARGUMENT);
			if (kbr != null) kbr.remove();
		}

		@Override
		public Collection<Message> create(D3webCompiler compiler, Section<SolutionReference> s) {

			Solution sol = s.get().getTermObject(compiler, s);

			String argument = getArgumentString(s);

			if (sol != null) {
				Score score = D3webUtils.getScoreForString(argument);

				if (score != null) {

					Condition cond = QuestionDashTreeUtils.createCondition(compiler,
							DashTreeUtils.getAncestorDashTreeElements(s));
					if (cond != null) {
						Rule r = RuleFactory.createHeuristicPSRule(sol, score, cond);
						if (r != null) {
							KnowWEUtils.storeObject(compiler, s, SETVALUE_ARGUMENT, r);
							return Messages.noMessage();
						}
					}
				}
			}

			return Collections.singletonList(Messages.creationFailedWarning(
					Rule.class.getSimpleName()));

		}

		private String getArgumentString(Section<SolutionReference> s) {
			String argument = null;
			List<Section<AnonymousType>> children = new ArrayList<>();
			Sections.successors(s.getParent(), AnonymousType.class, children);
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
