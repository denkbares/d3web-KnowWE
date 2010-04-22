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
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.rule.ConditionActionRule;
import de.d3web.we.kdom.rules.RuleAction;
import de.d3web.we.kdom.rulesNew.terminalCondition.CondKnown;
import de.d3web.we.kdom.rulesNew.terminalCondition.Finding;
import de.d3web.we.kdom.rulesNew.terminalCondition.NumericalFinding;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;

public class RuleContentType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new AllTextSectionFinder();

		// configure the rule
		ConditionActionRule rule = new ConditionActionRule(new RuleAction());
		List<KnowWEObjectType> termConds = new ArrayList<KnowWEObjectType>();
		// add all the various allowed TerminalConditions here
		termConds.add(new NumericalFinding());
		termConds.add(new Finding());
		termConds.add(new CondKnown());
		rule.setTerminalConditions(termConds);

		this.addChildType(rule);

	}


}


