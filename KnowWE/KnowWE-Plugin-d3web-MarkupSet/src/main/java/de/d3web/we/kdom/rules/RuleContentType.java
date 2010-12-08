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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.Session;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.CondKnown;
import de.d3web.we.kdom.condition.CondKnownUnknown;
import de.d3web.we.kdom.condition.Finding;
import de.d3web.we.kdom.condition.KDOMConditionFactory;
import de.d3web.we.kdom.condition.NumericalFinding;
import de.d3web.we.kdom.condition.NumericalIntervallFinding;
import de.d3web.we.kdom.condition.SolutionStateCond;
import de.d3web.we.kdom.condition.UserRatingConditionType;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.rule.ConditionActionRule;
import de.d3web.we.kdom.rule.ConditionActionRuleContent;
import de.d3web.we.kdom.rules.action.D3webRuleAction;
import de.d3web.we.kdom.rules.action.RuleAction;
import de.d3web.we.kdom.sectionFinder.AllTextFinderDivCorrectTrimmed;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.knowwe.core.renderer.FontColorBackgroundRenderer;
import de.knowwe.core.renderer.ReRenderSectionMarkerRenderer;

/**
 * @author Jochen
 * 
 *         A type for the content of the RuleMarkup-block. It allocates all the
 *         text tries to create Rules from the content
 * 
 */
public class RuleContentType extends DefaultAbstractKnowWEObjectType {

	public static final String ruleStoreKey = "RULE_STORE_KEY";

	/**
	 * Here the type is configured. It takes (mostly) all the text it gets. A
	 * ConditionActionRule-type is initialized and inserted as child-type (which
	 * itself gets a child-type: ConditionActionRuleContent).
	 * 
	 */
	public RuleContentType() {
		// take nearly all the text that is passed (kind of trimmed)
		this.sectionFinder = new AllTextFinderDivCorrectTrimmed();

		// configure the rule
		ConditionActionRule rule = new ConditionActionRule();
		ConditionActionRuleContent ruleContent = new ConditionActionRuleContent(new RuleAction());
		ruleContent.setCustomRenderer(new ReRenderSectionMarkerRenderer(
				new RuleHighlightingRenderer()));
		List<KnowWEObjectType> termConds = new ArrayList<KnowWEObjectType>();

		// add all the various allowed TerminalConditions here
		try {
			// TODO remove this evil workaround
			// when updating KnowWE architecture
			termConds.add((KnowWEObjectType) Class.forName("cc.knowwe.tdb.EvalConditionType").newInstance());
		}
		catch (Throwable e) {
			Logger.getLogger("KnowWE").log(Level.INFO,
					"cc.knowwe.tdb.EvalConditionType is not attached");
		}
		termConds.add(new SolutionStateCond());
		termConds.add(new UserRatingConditionType());
		termConds.add(new CondKnownUnknown());
		termConds.add(new Finding());
		termConds.add(new CondKnown());
		termConds.add(new NumericalFinding());
		termConds.add(new NumericalIntervallFinding());
		ruleContent.setTerminalConditions(termConds);

		// add handler to create the rules in the d3web knowledge base
		ruleContent.addSubtreeHandler(new RuleCompiler());

		// register the configured rule-content-type as child
		rule.addChildType(ruleContent);

		// register the configured rule-type as child
		this.addChildType(rule);

	}

	/**
	 * @author Jochen
	 * 
	 *         This handler compiles a parsed rule into the d3web knowledge base
	 *         (if it doesn't have errors)
	 * 
	 */
	class RuleCompiler extends D3webSubtreeHandler<ConditionActionRuleContent> {

		@Override
		public boolean needsToCreate(KnowWEArticle article, Section<ConditionActionRuleContent> s) {
			return super.needsToCreate(article, s)
					|| s.isOrHasSuccessorNotReusedBy(article.getTitle());
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<ConditionActionRuleContent> s) {

			if (s.hasErrorInSubtree(article)) {
				return Arrays.asList((KDOMReportMessage) new CreateRelationFailed("Rule"));
			}

			KnowledgeBaseManagement mgn = getKBM(article);

			Section<CompositeCondition> cond = s.findSuccessor(CompositeCondition.class);

			Condition d3Cond = KDOMConditionFactory.createCondition(article, cond);

			Section<D3webRuleAction> action = s.findSuccessor(D3webRuleAction.class);
			if (action == null) {
				return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
						D3webModule.getKwikiBundle_d3web().
								getString("KnowWE.rulesNew.notcreated")
								+ " : no valid action found"
						));
			}

			PSAction d3action = action.get().getAction(article, action);

			if (d3action != null && d3Cond != null) {
				Rule r = RuleFactory.createRule(mgn.createRuleID(), d3action, d3Cond,
						null, null, action.get().getActionPSContext());
				if (r != null) {
					KnowWEUtils.storeObject(article, s, ruleStoreKey, r);
					return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage("Rule"));
				}

			}

			// should not happen
			return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
					D3webModule.getKwikiBundle_d3web().
							getString("KnowWE.rulesNew.notcreated")
					));
		}

		@Override
		public boolean needsToDestroy(KnowWEArticle article, Section<ConditionActionRuleContent> s) {
			return super.needsToDestroy(article, s)
					|| s.isOrHasSuccessorNotReusedBy(article.getTitle());
		}

		@Override
		public void destroy(KnowWEArticle article, Section<ConditionActionRuleContent> rule) {
			Rule kbr = (Rule) KnowWEUtils.getObjectFromLastVersion(article, rule, ruleStoreKey);
			if (kbr != null) {
				kbr.remove();
				KnowWEUtils.storeObject(article, rule, ruleStoreKey, null);
			}
		}

	}

	/**
	 * @author Johannes Dienst
	 * 
	 *         Highlights Rules according to state.
	 * 
	 */
	class RuleHighlightingRenderer extends KnowWEDomRenderer<ConditionActionRuleContent> {

		@Override
		public void render(KnowWEArticle article,
				Section<ConditionActionRuleContent> sec, KnowWEUserContext user,
				StringBuilder string) {

			Session session = D3webUtils.getSession(article.getTitle(), user,
					article.getWeb());
			Rule rule = (Rule) KnowWEUtils.getStoredObject(sec.getWeb(), sec
					.getTitle(), sec.getID(),
					RuleContentType.ruleStoreKey);

			string.append(KnowWEUtils.maskHTML("<span id='" + sec.getID() + "'>"));
			this.highlightRule(article, sec, rule, session, user, string);
			string.append(KnowWEUtils.maskHTML("</span>"));
		}

		private static final String highlightMarker = "HIGHLIGHT_MARKER";

		/**
		 * Stores the Renderer used in <b>highlightRule<b>
		 */
		KnowWEDomRenderer greenRenderer = FontColorBackgroundRenderer.getRenderer(
						highlightMarker, "", "#CFFFCF");

		KnowWEDomRenderer redRenderer = FontColorBackgroundRenderer.getRenderer(highlightMarker,
				"", "#FF9900");

		KnowWEDomRenderer exceptionRenderer = FontColorBackgroundRenderer.getRenderer(
				highlightMarker, "", null);

		/**
		 * Renders the Rule with highlighting.
		 * 
		 * @param sec
		 * @param rc
		 * @param session
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private void highlightRule(KnowWEArticle article,
				Section<ConditionActionRuleContent> sec, Rule r, Session session,
				KnowWEUserContext user, StringBuilder string) {

			StringBuilder newContent = new StringBuilder();
			if (r == null || session == null) {
				DelegateRenderer.getInstance().
						render(article, sec, user, newContent);
			}
			else {
				try {
					if (r.hasFired(session)) {
						this.greenRenderer.render(article, sec, user, newContent);
					}
					else {
						DelegateRenderer.getInstance().render(article, sec, user, newContent);
					}
				}
				catch (Exception e) {
					this.exceptionRenderer.render(article, sec, user, newContent);
				}
			}
			string.append(newContent.toString());
		}

	}
}
