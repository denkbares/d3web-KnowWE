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

package de.d3web.we.kdom.rule;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

/**
 * @author Jochen
 * 
 *         Container for ConditionActionRule
 * 
 */
public class ConditionActionRule extends AbstractType {

	public static final String RULE_START = "^\\s*(?:IF|WENN)";

	public ConditionActionRule() {
		this(null);
	}

	public ConditionActionRule(AbstractType action) {
		// from the beginning of the rule until before the beginning of the next
		// rule, an empty line or the end of the parent section
		setSectionFinder(new RegexSectionFinder(
				RULE_START + ".*?(?=\\s*?(" + RULE_START + "|^\\s*?$|\\z))",
				Pattern.DOTALL + Pattern.MULTILINE));
		if (action != null) {
			this.addChildType(new ConditionActionRuleContent(action));
		}
	}

}
