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
import java.util.List;
import java.util.regex.Pattern;

import de.d3web.core.inference.Rule;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.kdom.condition.CondKnown;
import de.d3web.we.kdom.condition.CondKnownUnknown;
import de.d3web.we.kdom.condition.CondRegularExpression;
import de.d3web.we.kdom.condition.CondUnknown;
import de.d3web.we.kdom.condition.Finding;
import de.d3web.we.kdom.condition.NumericalFinding;
import de.d3web.we.kdom.condition.NumericalIntervallFinding;
import de.d3web.we.kdom.condition.SolutionStateCond;
import de.d3web.we.kdom.condition.UserRatingConditionType;
import de.d3web.we.kdom.rules.action.RuleAction;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.ReRenderSectionMarkerRenderer;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * A default rule in KnowWE. Starts with an IF and ends at an empty line or the
 * next IF or the end of the section.
 * 
 * @author Jochen Reutelshöfer, Albrecht Striffler (denkbares GmbH)
 */
public class RuleType extends AbstractType {

	public static final String RULE_START = "^\\s*(?:IF|WENN)";

	public RuleType() {
		// from the beginning of the rule until before the beginning of the next
		// rule, an empty line or the end of the parent section
		setSectionFinder(new RegexSectionFinder(
				RULE_START + ".*?(?=\\s*?(" + RULE_START + "|^\\s*?$|\\z))",
				Pattern.DOTALL + Pattern.MULTILINE));

		ConditionActionRuleContent ruleContent = new ConditionActionRuleContent();

		ruleContent.setRenderer(new ReRenderSectionMarkerRenderer(
				new RuleHighlightingRenderer()));
		List<Type> termConds = RuleType.getTerminalConditions();
		ruleContent.setTerminalConditions(termConds);

		// register the configured rule-content-type as child
		this.addChildType(ruleContent);

	}

	/**
	 * Highlights Rules according to state.
	 * 
	 * @author Johannes Dienst
	 * 
	 */
	class RuleHighlightingRenderer implements Renderer {

		@Override
		public void render(Section<?> sec,
				UserContext user, RenderResult string) {

			D3webCompiler compiler = Compilers.getCompiler(sec, D3webCompiler.class);
			Rule rule = null;
			Session session = null;

			Section<RuleAction> ruleAction = Sections.findSuccessor(sec,
					RuleAction.class);
			if (ruleAction != null) {
				rule = (Rule) KnowWEUtils.getStoredObject(compiler, ruleAction,
						RuleCompileScript.RULE_STORE_KEY);
			}

			string.appendHtml("<span id='" + sec.getID() + "'>");

			if (compiler != null) {
				KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
				session = SessionProvider.getSession(user, kb);
			}

			highlightRule(sec, rule, session, user, string);
			string.appendHtml("</span>");

		}

		private static final String highlightMarker = "HIGHLIGHT_MARKER";

		/**
		 * Stores the Renderer used in <b>highlightRule<b>
		 */
		StyleRenderer firedRenderer = StyleRenderer.getRenderer(
				highlightMarker, "", StyleRenderer.CONDITION_FULLFILLED);

		StyleRenderer exceptionRenderer = StyleRenderer.getRenderer(
				highlightMarker, "", null);

		/**
		 * Renders the Rule with highlighting.
		 */
		private void highlightRule(Section<?> sec, Rule rule,
				Session session, UserContext user, RenderResult string) {

			RenderResult newContent = new RenderResult(string);
			if (rule == null || session == null) {
				DelegateRenderer.getInstance().render(sec, user, newContent);
			}
			else {
				try {
					if (rule.hasFired(session)) {
						this.firedRenderer.render(sec, user, newContent);
					}
					else {
						DelegateRenderer.getInstance().render(sec, user,
								newContent);
					}
				}
				catch (Exception e) {
					this.exceptionRenderer.render(sec, user, newContent);
				}
			}
			string.append(newContent.toStringRaw());
		}

	}

	public static List<Type> getTerminalConditions() {
		List<Type> termConds = new ArrayList<Type>();

		// add all the various allowed TerminalConditions here
		termConds.add(new SolutionStateCond());
		termConds.add(new UserRatingConditionType());
		termConds.add(new CondKnownUnknown());
		termConds.add(new CondRegularExpression());
		termConds.add(new Finding());
		termConds.add(new CondUnknown());
		termConds.add(new CondKnown());
		termConds.add(new NumericalFinding());
		termConds.add(new NumericalIntervallFinding());
		return termConds;
	}

}
