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

package de.d3web.we.kdom.rules;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.CondKnown;
import de.d3web.we.kdom.condition.CondKnownUnknown;
import de.d3web.we.kdom.condition.CondRegularExpression;
import de.d3web.we.kdom.condition.CondUnknown;
import de.d3web.we.kdom.condition.Finding;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.kdom.condition.NumericalFinding;
import de.d3web.we.kdom.condition.NumericalIntervallFinding;
import de.d3web.we.kdom.condition.SolutionStateCond;
import de.d3web.we.kdom.condition.UserRatingConditionType;
import de.d3web.we.kdom.rule.ConditionActionRule;
import de.d3web.we.kdom.rule.ConditionActionRuleContent;
import de.d3web.we.kdom.rule.ExceptionConditionArea;
import de.d3web.we.kdom.rules.action.D3webRuleAction;
import de.d3web.we.kdom.rules.action.RuleAction;
import de.d3web.we.knowledgebase.D3webCompileScript;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.CommentLineType;
import de.knowwe.core.kdom.basicType.UnrecognizedSyntaxType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.renderer.ReRenderSectionMarkerRenderer;
import de.knowwe.kdom.renderer.StyleRenderer;

/**
 * @author Jochen
 * 
 *         A type for the content of the RuleMarkup-block. It allocates all the
 *         text tries to create Rules from the content
 * 
 */
public class RuleContentType extends AbstractType {

	public static final String RULE_STORE_KEY = "RULE_STORE_KEY";

	/**
	 * Here the type is configured. It takes (mostly) all the text it gets. A
	 * ConditionActionRule-type is initialized and inserted as child-type (which
	 * itself gets a child-type: ConditionActionRuleContent).
	 * 
	 */
	public RuleContentType() {
		// take nearly all the text that is passed (kind of trimmed)
		this.setSectionFinder(new AllTextFinderTrimmed());

		// configure the rule
		ConditionActionRule rule = new ConditionActionRule();
		RuleAction ruleAction = new RuleAction();
		ConditionActionRuleContent ruleContent = new ConditionActionRuleContent(
				ruleAction);

		// add handler to create the rules in the d3web knowledge base
		ruleAction.addCompileScript(Priority.LOW, new RuleCompiler());

		ruleContent.setRenderer(new ReRenderSectionMarkerRenderer(
				new RuleHighlightingRenderer()));
		List<Type> termConds = getTerminalConditions();
		ruleContent.setTerminalConditions(termConds);

		// register the configured rule-content-type as child
		rule.addChildType(ruleContent);

		// register the configured rule-type as child
		this.addChildType(new CommentLineType());
		this.addChildType(rule);
		this.addChildType(new UnrecognizedSyntaxType());
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

	/**
	 * This handler compiles a parsed rule into the d3web knowledge base (if it
	 * doesn't have errors)
	 * 
	 * @author Jochen
	 */
	class RuleCompiler extends D3webCompileScript<RuleAction> {

		@Override
		public void compile(D3webCompiler compiler, Section<RuleAction> section) throws CompilerMessage {

			Section<ConditionActionRuleContent> ruleSection = Sections
					.findAncestorOfType(section,
							ConditionActionRuleContent.class);

			if (ruleSection.hasErrorInSubtree(compiler)) {
				throw new CompilerMessage(
						Messages.creationFailedWarning(Rule.class.getSimpleName()));
			}

			// create condition
			Section<CompositeCondition> cond = Sections.findSuccessor(ruleSection,
					CompositeCondition.class);
			Condition d3Cond = KDOMConditionFactory.createCondition(compiler,
					cond);

			// create action
			@SuppressWarnings("rawtypes")
			Section<D3webRuleAction> action = Sections.findSuccessor(section,
					D3webRuleAction.class);
			if (action == null) {
				throw new CompilerMessage(
						Messages.creationFailedWarning(
								D3webUtils.getD3webBundle().getString(
										"KnowWE.rulesNew.notcreated")
										+ " : no valid action found"));
			}
			@SuppressWarnings("unchecked")
			PSAction d3action = action.get().getAction(compiler, action);

			// create exception (if exists)
			Section<ExceptionConditionArea> exceptionCondSec = Sections
					.findSuccessor(ruleSection, ExceptionConditionArea.class);
			Condition exceptionCond = null;
			if (exceptionCondSec != null) {
				Section<CompositeCondition> exceptionCompCondSec = Sections
						.findSuccessor(exceptionCondSec,
								CompositeCondition.class);
				if (exceptionCompCondSec != null) {
					exceptionCond = KDOMConditionFactory.createCondition(
							compiler, exceptionCompCondSec);
				}
			}

			// create actual rule
			if (d3action != null && d3Cond != null) {
				@SuppressWarnings("unchecked")
				Rule rule = RuleFactory.createRule(d3action, d3Cond,
						exceptionCond, action.get().getActionPSContext());
				if (rule != null) {
					KnowWEUtils.storeObject(compiler, section, RULE_STORE_KEY, rule);
					throw new CompilerMessage(
							Messages.objectCreatedNotice(
									"Rule"));
				}

			}

			// should not happen
			throw new CompilerMessage(Messages.creationFailedWarning(
					D3webUtils.getD3webBundle().getString("KnowWE.rulesNew.notcreated")));
		}

		@Override
		public void destroy(D3webCompiler compiler, Section<RuleAction> section) {
			Rule kbr = (Rule) section.getSectionStore().getObject(compiler,
					RULE_STORE_KEY);
			if (kbr != null) {
				kbr.remove();
			}
		}

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
						RuleContentType.RULE_STORE_KEY);
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
		 * 
		 * @param sec
		 * @param rc
		 * @param session
		 * @return
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
}
