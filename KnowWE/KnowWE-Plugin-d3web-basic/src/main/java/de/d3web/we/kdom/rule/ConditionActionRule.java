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

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.objects.KnowWETermMarker;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;

/**
 * @author Jochen
 *
 *         Container for ConditionActionRule
 *
 */
public class ConditionActionRule extends DefaultAbstractKnowWEObjectType implements KnowWETermMarker {

	public ConditionActionRule() {
		sectionFinder = new RegexSectionFinder(
				"(IF|WENN).*?(?=(\\s*?(?m)^\\s*?$\\s*|\\s*IF|\\s*WENN"
						+ "|\\s*\\z))\\p{Space}*",
				Pattern.DOTALL);
	}

	public ConditionActionRule(AbstractKnowWEObjectType action) {
		sectionFinder = new RegexSectionFinder(
				"(IF|WENN).*?(?=(\\s*?(?m)^\\s*?$\\s*|\\s*IF|\\s*WENN"
						+ "|\\s*\\z))",
				Pattern.DOTALL);

		this.addChildType(new ConditionActionRuleContent(action));
	}

}
