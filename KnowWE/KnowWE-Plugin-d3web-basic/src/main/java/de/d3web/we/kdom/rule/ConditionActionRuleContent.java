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

import de.d3web.we.kdom.condition.CompositeCondition;
import de.knowwe.core.compile.IncrementalMarker;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.EndLineComment;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.kdom.sectionFinder.AllBeforeTypeSectionFinder;

/**
 * Markup for a simple condition-action-rule using the CompositeCondition @see
 * {@link CompositeCondition} The action type is given to the constructor. The
 * TerminalConditions can be set via setTerminalConditions()
 * 
 * @author Max Diez (mostly copied from old ConditionActionRule)
 * @created 12.08.2010
 */
public class ConditionActionRuleContent extends AbstractType implements IncrementalMarker {

	ConditionArea condArea = new ConditionArea();
	ExceptionConditionArea exceptionCond = new ExceptionConditionArea();

	public ConditionActionRuleContent(AbstractType action) {
		this.sectionFinder = new AllTextFinderTrimmed();
		this.addChildType(new If());
		Then then = new Then();
		this.addChildType(then);
		Except except = new Except();
		this.addChildType(except);
		condArea.setSectionFinder(new AllBeforeTypeSectionFinder(then));
		this.addChildType(condArea);

		EndLineComment endLineComment = new EndLineComment();
		endLineComment.setCustomRenderer(StyleRenderer.COMMENT);
		this.addChildType(endLineComment);

		ActionArea ae = new ActionArea(action);
		ConstraintSectionFinder constraintFinderAction = new ConstraintSectionFinder(
				new AllTextFinderTrimmed());
		constraintFinderAction.addConstraint(SingleChildConstraint.getInstance());
		ae.setSectionFinder(constraintFinderAction);
		this.addChildType(ae);

		ConstraintSectionFinder constraintFinderExCond = new ConstraintSectionFinder(
				new AllTextFinderTrimmed());
		constraintFinderExCond.addConstraint(SingleChildConstraint.getInstance());
		exceptionCond.setSectionFinder(constraintFinderExCond);
		this.addChildType(exceptionCond);
	}

	/**
	 * Add the list of TerminalConditions for the Condition here
	 * 
	 * @param termConds
	 */
	public void setTerminalConditions(List<Type> termConds) {
		condArea.compCond.setAllowedTerminalConditions(termConds);
		exceptionCond.compCond.setAllowedTerminalConditions(termConds);
	}

}
