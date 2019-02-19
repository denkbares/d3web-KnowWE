/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.kdom.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.denkbares.strings.Strings;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.NoAnswerException;
import de.d3web.core.inference.condition.UnknownAnswerException;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.kdom.action.ContraIndicationAction;
import de.d3web.we.kdom.action.InstantIndication;
import de.d3web.we.kdom.action.QASetIndicationAction;
import de.d3web.we.kdom.action.RepeatedIndication;
import de.d3web.we.kdom.action.SetQNumFormulaAction;
import de.d3web.we.kdom.action.SetQuestionNumValueAction;
import de.d3web.we.kdom.action.SetQuestionValue;
import de.d3web.we.kdom.action.SolutionValueAssignment;
import de.d3web.we.kdom.condition.CondKnown;
import de.d3web.we.kdom.condition.CondKnownUnknown;
import de.d3web.we.kdom.condition.CondRegularExpression;
import de.d3web.we.kdom.condition.CondUnknown;
import de.d3web.we.kdom.condition.Finding;
import de.d3web.we.kdom.condition.NumericalFinding;
import de.d3web.we.kdom.condition.NumericalIntervallFinding;
import de.d3web.we.kdom.condition.SolutionStateCond;
import de.d3web.we.kdom.condition.UserRatingConditionType;
import de.d3web.we.kdom.rules.action.ElseActionContainer;
import de.d3web.we.kdom.rules.action.ThenActionContainer;
import de.d3web.we.kdom.rules.action.UnknownActionContainer;
import de.d3web.we.kdom.rules.condition.ExceptionConditionContainer;
import de.d3web.we.kdom.rules.condition.IfConditionContainer;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.EndLineComment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.renderer.ReRenderSectionMarkerRenderer;

/**
 * A default rule in KnowWE. Starts with an IF and ends at an empty line or the next IF or the endTokens of the
 * section.
 *
 * @author Jochen Reutelshöfer, Albrecht Striffler (denkbares GmbH)
 */
public class RuleType extends AbstractType {

	public static final String[] IF_TOKENS = new String[] { "IF", "WENN" };
	public static final String[] THEN_TOKENS = new String[] { "THEN", "DANN" };
	public static final String[] ELSE_TOKENS = new String[] { "ELSE", "ANSONSTEN" };
	public static final String[] EXCEPT_TOKENS = new String[] { "EXCEPT", "AUSSER" };
	public static final String[] UNKNOWN_TOKENS = new String[] { "UNKNOWN", "UNBEKANNT" };

	public static final String[] INNER_TOKENS =
			(String[]) ArrayUtils.addAll(ArrayUtils.addAll(ArrayUtils.addAll(
					THEN_TOKENS, ELSE_TOKENS), EXCEPT_TOKENS), UNKNOWN_TOKENS);

	public RuleType() {

		setSectionFinder(new RuleFinder());
		setRenderer(new ReRenderSectionMarkerRenderer(new RuleHighlightingRenderer()));

		this.addChildType(new IfConditionContainer());

		this.addChildType(new ThenActionContainer());
		this.addChildType(new ElseActionContainer());
		this.addChildType(new UnknownActionContainer());

		this.addChildType(new ExceptionConditionContainer());

		this.addChildType(new EndLineComment());

		this.addCompileScript(Priority.LOW, new RuleCompileScript());
	}

	/**
	 * Highlights Rules according to state.
	 *
	 * @author Albrecht Striffler
	 */
	private static class RuleHighlightingRenderer implements Renderer {

		@Override
		public void render(Section<?> sec, UserContext user, RenderResult string) {

			D3webCompiler compiler = Compilers.getCompiler(sec, D3webCompiler.class);
			Session session = D3webUtils.getExistingSession(compiler, user);

			List<String> classes = new ArrayList<>();
			classes.add("d3webRule");
			if (session != null) {
				Section<RuleType> ruleSection = Sections.cast(sec, RuleType.class);

				Collection<Rule> defaultRules = RuleCompileScript.getDefaultRules(compiler, ruleSection);
				if (!defaultRules.isEmpty()) {
					Rule defaultRule = defaultRules.iterator().next();
					if (defaultRule.hasFired(session)) classes.add("defaultFired");

					Condition condition = defaultRule.getCondition();
					Condition exception = defaultRule.getException();
					try {
						if (condition.eval(session)) {
							classes.add("conditionTrue");
						}
						else {
							classes.add("conditionFalse");
						}

					}
					catch (UnknownAnswerException e) {
						classes.add("conditionUnknown");
					}
					catch (NoAnswerException ignore) {
					}
					if (exception != null) {
						try {
							if (exception.eval(session)) {
								classes.add("exceptTrue");
							} else {
								//classes.add("exceptFalse");
							}
						}
						catch (UnknownAnswerException | NoAnswerException e) {
							//classes.add("exceptUnknown");
						}
					}
				}

				Collection<Rule> elseRules = RuleCompileScript.getElseRules(compiler, ruleSection);
				if (!elseRules.isEmpty()) {
					Rule elseRule = elseRules.iterator().next();
					if (elseRule.hasFired(session)) {
						classes.add("elseFired");
					}
				}

				Collection<Rule> unknownRules = RuleCompileScript.getUnknownRules(compiler, ruleSection);
				if (!unknownRules.isEmpty()) {
					Rule unknownRule = unknownRules.iterator().next();
					if (unknownRule.hasFired(session)) classes.add("unknownFired");
				}
			}
			string.appendHtml("<span id='" + sec.getID() + "' class='" + Strings.concat(" ", classes) + "'>");
			DelegateRenderer.getInstance().render(sec, user, string);
			string.appendHtml("</span>");

		}

	}

	public static List<Type> getTerminalConditions() {
		List<Type> termConditions = new ArrayList<>();
		// add all the various allowed TerminalConditions here
		termConditions.add(new SolutionStateCond());
		termConditions.add(new UserRatingConditionType());
		termConditions.add(new CondKnownUnknown());
		termConditions.add(new CondRegularExpression());
		termConditions.add(new Finding());
		termConditions.add(new CondUnknown());
		termConditions.add(new CondKnown());
		termConditions.add(new NumericalFinding());
		termConditions.add(new NumericalIntervallFinding());
		return termConditions;
	}

	public static List<Type> getActions() {
		List<Type> actions = new ArrayList<>();
		// add all the various allowed Actions here
		actions.add(new SolutionValueAssignment());
		actions.add(new SetQuestionNumValueAction());
		actions.add(new SetQNumFormulaAction());
		actions.add(new SetQuestionValue());
		actions.add(new ContraIndicationAction());
		actions.add(new InstantIndication());
		actions.add(new RepeatedIndication());
		actions.add(new QASetIndicationAction());
		return actions;
	}

	private static class RuleFinder extends RuleContainerFinder {

		public RuleFinder() {
			super(RuleType.IF_TOKENS, RuleType.IF_TOKENS);
		}

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			// we need the indent/whitespaces in front of the first IF to be part of the RuleType
			// this way, it can also be shown in previews
			List<SectionFinderResult> sectionFinderResults = super.lookForSections(text, father, type);
			for (SectionFinderResult sectionFinderResult : sectionFinderResults) {
				int start = sectionFinderResult.getStart();
				while (start > 0) {
					start--;
					if (text.charAt(start) == '\n') {
						start++;
						break;
					}
				}
				sectionFinderResult.setStart(start);
			}
			return sectionFinderResults;
		}
	}
}
