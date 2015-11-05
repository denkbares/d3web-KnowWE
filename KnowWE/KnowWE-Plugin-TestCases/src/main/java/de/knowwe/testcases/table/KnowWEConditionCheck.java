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
import de.d3web.testcase.model.ConditionCheck;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;

/**
 * Class to implement a checker that test a condition to be true.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 22.04.2012
 */
public class KnowWEConditionCheck extends ConditionCheck {

	private final Section<?> section;

	/**
	 * Creates a new ConditionCheck instance for a specified condition and the
	 * condition-defining section.
	 * 
	 * @param condition the condition to be checked
	 * @param section the section defining the condition
	 */
	public KnowWEConditionCheck(Condition condition, Section<?> section) {
		super(condition);
		this.section = section;
	}

	@Override
	public String getCondition() {
		return section.getText();
	}

	public void render(UserContext context, RenderResult result) {
		result.append(section, context);
	}
}
