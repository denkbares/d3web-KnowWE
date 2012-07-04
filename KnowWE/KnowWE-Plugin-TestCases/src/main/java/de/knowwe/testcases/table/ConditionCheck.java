/*
 * Copyright (C) 2012 denkbares GmbH, Germany
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
package de.knowwe.testcases.table;

import de.d3web.core.inference.condition.Condition;
import de.d3web.core.inference.condition.Conditions;
import de.d3web.core.session.Session;
import de.d3web.testcase.model.Check;

/**
 * Class to implement a checker that test a condition to be true.
 * 
 * @author volker_belli
 * @created 22.04.2012
 */
public class ConditionCheck implements Check {

	private final Condition condition;
	private final String verbalization;

	/**
	 * Creates a new ConditionCheck instance for a specified condition and a
	 * given text verbalization.
	 * 
	 * @param condition the condition to be checked
	 * @param verbalization the text to be displayed
	 */
	public ConditionCheck(Condition condition, String verbalization) {
		this.condition = condition;
		this.verbalization = verbalization;
	}

	@Override
	public boolean check(Session session) {
		return Conditions.isTrue(condition, session);
	}

	@Override
	public String getCondition() {
		return verbalization;
	}

}
