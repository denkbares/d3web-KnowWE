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
import java.util.List;

import de.d3web.abstraction.ActionSetQuestion;
import de.d3web.abstraction.formula.FormulaNumber;
import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.manage.RuleFactory;
import de.d3web.strings.Strings;
import de.d3web.we.kdom.questionTree.QuestionDashTreeUtils;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.QuestionReference;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.dashtree.DashTreeUtils;
import de.knowwe.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.knowwe.kdom.sectionFinder.ConditionalSectionFinder;

public class QuestionSetValueNumLine extends AbstractType {

	private static final String SETVALUE_ARGUMENT = "SetValueNumArgument";
	private static final String OPEN = "(";
	private static final String CLOSE = ")";

	public QuestionSetValueNumLine() {
		this.setSectionFinder(new ConditionalSectionFinder(AllTextFinder.getInstance()) {

			@Override
			protected boolean condition(String text, Section<?> father) {
				int open = Strings.indexOfUnquoted(text, (OPEN));
				if (open == -1) return false;

				int close = Strings.indexOfClosingBracket(text, open, OPEN.charAt(0),
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
		});

		AnonymousType argumentType = createArgumentType();
		this.addChildType(argumentType);
		this.addChildType(createObjectRefTypeBefore(argumentType));

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

	private Type createObjectRefTypeBefore(
			AbstractType typeAfter) {
		QuestionReference qid = new QuestionReference();
		qid.setSectionFinder(new AllBeforeTypeSectionFinder(typeAfter));
		qid.addCompileScript(new CreateSetValueNumRuleHandler());
		return qid;
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

	static class CreateSetValueNumRuleHandler implements D3webCompileScript<QuestionReference> {

		@Override
		public void destroy(D3webCompiler compiler, Section<QuestionReference> section) {
			Rule kbr = (Rule) section.getObject(compiler,
					SETVALUE_ARGUMENT);
			if (kbr != null) kbr.remove();
		}

		@Override
		public void compile(D3webCompiler compiler, Section<QuestionReference> section) throws CompilerMessage {

			Question q = section.get().getTermObject(compiler, section);

			String argument = getArgumentString(section);

			if (q != null) {
				Condition cond = QuestionDashTreeUtils.createCondition(compiler,
						DashTreeUtils.getAncestorDashTreeElements(section));

				if (cond != null) {
					Double d = Double.parseDouble(argument);
					ActionSetQuestion action = new ActionSetQuestion();
					action.setQuestion(q);
					action.setValue(new FormulaNumber(d));

					Rule r = RuleFactory.createRule(action, cond, null, PSMethodAbstraction.class);
					if (r != null) {
						KnowWEUtils.storeObject(compiler, section, SETVALUE_ARGUMENT, r);
						return;
					}
				}
			}
			throw new CompilerMessage(
					Messages.creationFailedWarning(
							Rule.class.getSimpleName()));
		}

		private static String getArgumentString(Section<QuestionReference> s) {
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
