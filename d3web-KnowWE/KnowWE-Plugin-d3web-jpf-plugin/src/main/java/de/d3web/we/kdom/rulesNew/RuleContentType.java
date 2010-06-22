/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.rulesNew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.d3web.core.inference.PSAction;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.condition.Condition;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.manage.RuleFactory;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.report.message.ObjectCreatedMessage;
import de.d3web.we.kdom.rule.ConditionActionRule;
import de.d3web.we.kdom.rulesNew.ruleAction.D3webRuleAction;
import de.d3web.we.kdom.rulesNew.ruleAction.RuleAction;
import de.d3web.we.kdom.rulesNew.terminalCondition.CondKnown;
import de.d3web.we.kdom.rulesNew.terminalCondition.Finding;
import de.d3web.we.kdom.rulesNew.terminalCondition.NumericalFinding;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

public class RuleContentType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new AllTextSectionFinder();

		// configure the rule
		ConditionActionRule rule = new ConditionActionRule(new RuleAction());
		List<KnowWEObjectType> termConds = new ArrayList<KnowWEObjectType>();

		// add all the various allowed TerminalConditions here
		termConds.add(new Finding());
		termConds.add(new NumericalFinding());
		termConds.add(new CondKnown());

		rule.setTerminalConditions(termConds);

		rule.addSubtreeHandler(new RuleCompiler());

		this.addChildType(rule);

	}

	class RuleCompiler extends SubtreeHandler<ConditionActionRule> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<ConditionActionRule> s) {

			if (s.hasErrorInSubtree()) {
				return Arrays.asList((KDOMReportMessage) new CreateRelationFailed("Rule"));
			}

			KnowledgeBaseManagement mgn = D3webModule.getKnowledgeRepresentationHandler(
					article.getWeb())
					.getKBM(article, this, s);

			Section<CompositeCondition> cond = s.findSuccessor(CompositeCondition.class);

			Condition d3Cond = KDOMConditionFactory.createCondition(cond);

			Section<D3webRuleAction> action = s.findSuccessor(D3webRuleAction.class);

			PSAction d3action = action.get().getAction(action);
			if (d3action != null) {

				Rule r = RuleFactory.createRule(mgn.createRuleID(), d3action, d3Cond,
						null,
						null);
				if (r != null) {
					return Arrays.asList((KDOMReportMessage) new ObjectCreatedMessage("Rule"));
				}

			}

			// should not happen
			return Arrays.asList((KDOMReportMessage) new CreateRelationFailed(
					"rule not created"));
		}

	}


}


