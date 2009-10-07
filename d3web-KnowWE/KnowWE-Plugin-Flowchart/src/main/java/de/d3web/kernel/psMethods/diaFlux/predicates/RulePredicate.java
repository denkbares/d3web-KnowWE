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

package de.d3web.kernel.psMethods.diaFlux.predicates;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.NoAnswerException;
import de.d3web.kernel.domainModel.ruleCondition.UnknownAnswerException;

/**
 * 
 * @author hatko
 *
 */
public class RulePredicate implements IPredicate {
	
	private final AbstractCondition condition;

	public RulePredicate(AbstractCondition condition) {

		if (condition == null)
			throw new IllegalArgumentException();
		
		this.condition = condition;
	}
	
	
	
	public AbstractCondition getCondition() {
		return condition;
	}



	@Override
	public boolean evaluate(XPSCase theCase) {
		try {
			return getCondition().eval(theCase);
		} catch (NoAnswerException e) {
			return false;
		} catch (UnknownAnswerException e) {
			return false;
		}
	}
	
	
	
	

}
