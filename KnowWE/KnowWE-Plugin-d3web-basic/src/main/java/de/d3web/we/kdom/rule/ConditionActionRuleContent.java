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
package de.d3web.we.kdom.rule;

import java.util.List;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.basic.EndLineComment;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.objects.KnowWETermMarker;
import de.d3web.we.kdom.sectionFinder.AllBeforeTypeSectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.renderer.CommentRenderer;

/**
 * Markup for a simple condition-action-rule using the CompositeCondition @see
 * {@link CompositeCondition} The action type is given to the constructor. The
 * TerminalConditions can be set via setTerminalConditions()
 * 
 * @author Max Diez (mostly copied from old ConditionActionRule)
 * @created 12.08.2010
 */
public class ConditionActionRuleContent extends DefaultAbstractKnowWEObjectType implements KnowWETermMarker {

	ConditionArea condArea = new ConditionArea();

	public ConditionActionRuleContent(AbstractKnowWEObjectType action) {
		this.sectionFinder = new AllTextFinderTrimmed();
		this.addChildType(new If());
		Then then = new Then();
		this.addChildType(then);

		condArea.setSectionFinder(AllBeforeTypeSectionFinder.createFinder(then));
		this.addChildType(condArea);

		EndLineComment endLineComment = new EndLineComment();
		endLineComment.setCustomRenderer(new CommentRenderer());
		this.addChildType(endLineComment);

		action.setSectionFinder(new AllTextFinderTrimmed());
		this.addChildType(action);
	}

	/**
	 * Add the list of TerminalConditions for the Condition here
	 * 
	 * @param termConds
	 */
	public void setTerminalConditions(List<KnowWEObjectType> termConds) {
		condArea.compCond.setAllowedTerminalConditions(termConds);
	}

	/**
	 * ConditionArea of the Condition-Action-Rule, instanciates the condition
	 * composite
	 * 
	 * 
	 * @author Jochen
	 * 
	 */
	class ConditionArea extends DefaultAbstractKnowWEObjectType {

		CompositeCondition compCond = null;

		public ConditionArea() {
			compCond = new CompositeCondition();
			this.addChildType(compCond);
		}
	}
}
