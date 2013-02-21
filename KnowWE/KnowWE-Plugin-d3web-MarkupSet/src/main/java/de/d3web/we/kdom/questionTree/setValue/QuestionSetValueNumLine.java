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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.abstraction.ActionSetQuestion;
import de.d3web.abstraction.formula.FormulaNumber;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.kdom.questionTree.QuestionDashTreeUtils;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Strings;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;

public class QuestionSetValueNumLine extends AbstractType {

	private static final String SETVALUE_ARGUMENT = "SetValueNumArgument";
	private static final String OPEN = "(";
	private static final String CLOSE = ")";

	public QuestionSetValueNumLine() {
		this.sectionFinder = new ConditionalSectionFinder(new AllTextSectionFinder()) {

			@Override
			protected boolean condition(String text, Section<?> father) {
				int open = Strings.indexOfUnquoted(text, (OPEN));
				if (open == -1) return false;

				int close = Strings.findIndexOfClosingBracket(text, open, OPEN.charAt(0),
						CLOSE.charAt(0));

				if (close == -1) return false;

				String content = text.substring(open + 1, close).trim();

				try {
					Double.parseDouble(content);
					return true;
				}
				catch (NumberFormatException e) {

				}

				return false;

			}
		};

		AnonymousType argumentType = createArgumentType();
		this.childrenTypes.add(argumentType);
		this.childrenTypes.add(createObjectRefTypeBefore(argumentType));

	}

	private AnonymousType createArgumentType() {
		AnonymousType typeDef = new AnonymousType(SETVALUE_ARGUMENT);
		SectionFinder typeFinder = new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text,
					Section<?> father, Type type) {

				return SectionFinderResult
						.createSingleItemList(new SectionFinderResult(
								Strings.indexOfUnquoted(text, OPEN),
								Strings.indexOfUnquoted(text, CLOSE) + 1));
			}
		};
		typeDef.setSectionFinder(typeFinder);
		typeDef.setRenderer(new ArgumentRenderer());
		return typeDef;
	}

	private AbstractType createObjectRefTypeBefore(
			AbstractType typeAfter) {
		QuestionReference qid = new QuestionReference();
		qid.setSectionFinder(new AllBeforeTypeSectionFinder(typeAfter));
		qid.addSubtreeHandler(new CreateSetValueNumRuleHandler());
		return qid;
	}

	static class ArgumentRenderer implements Renderer {

		@Override
		public void render(Section<?> sec, UserContext user,
				RenderResult string) {
			String embracedContent = sec.getText().substring(1,
					sec.getText().length() - 1);
			string.appendHTML(" <img height='10' src='KnowWEExtension/images/arrow_right_s.png'>");
			string.appendHTML("<b>(");
			string.append(embracedContent);
			string.appendHTML(")</b>");

		}

	}

	static class CreateSetValueNumRuleHandler extends D3webSubtreeHandler<QuestionReference> {

		@Override
		public void destroy(Article article, Section<QuestionReference> s) {
			Rule kbr = (Rule) s.getSectionStore().getObject(article,
					SETVALUE_ARGUMENT);
			if (kbr != null) kbr.remove();
		}

		@Override
		public Collection<Message> create(Article article, Section<QuestionReference> s) {

			Question q = s.get().getTermObject(article, s);

			String argument = getArgumentString(s);

			if (q != null) {
				Condition cond = QuestionDashTreeUtils.createCondition(article,
						DashTreeUtils.getAncestorDashTreeElements(s));

				if (cond != null) {
					Double d = Double.parseDouble(argument);
					ActionSetQuestion action = new ActionSetQuestion();
					action.setQuestion(q);
					action.setValue(new FormulaNumber(d));

					Rule r = RuleFactory.createRule(action, cond, null, PSMethodAbstraction.class);
					if (r != null) {
						KnowWEUtils.storeObject(article, s, SETVALUE_ARGUMENT, r);
						return Messages.asList(Messages.objectCreatedNotice(
								r.getClass().toString()));
					}
				}
			}

			return Messages.asList(Messages.creationFailedWarning(
					Rule.class.getSimpleName()));

		}

		private static String getArgumentString(Section<QuestionReference> s) {
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
