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
package de.knowwe.diaflux.type;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.CondKnown;
import de.d3web.we.kdom.condition.CondKnownUnknown;
import de.d3web.we.kdom.condition.Finding;
import de.d3web.we.kdom.condition.NumericalFinding;
import de.d3web.we.kdom.condition.NumericalIntervallFinding;
import de.d3web.we.kdom.condition.SolutionStateCond;
import de.d3web.we.kdom.condition.UserRatingConditionType;
import de.knowwe.core.kdom.Type;
import de.knowwe.kdom.xml.AbstractXMLType;
import de.knowwe.kdom.xml.XMLContent;

/**
 * 
 * @author Reinhard Hatko Created on: 08.10.2009
 */
public class GuardType extends AbstractXMLType {

	private static GuardType instance;

	private GuardType() {
		super("guard");
		CompositeCondition condition = new CompositeCondition();

		List<Type> types = new ArrayList<Type>();

		types.add(new SolutionStateCond());
		types.add(new NodeActiveConditionType());
		types.add(new UserRatingConditionType());
		types.add(new CondKnownUnknown());
		types.add(new FlowchartProcessedConditionType());
		types.add(new Finding());
		types.add(new NumericalFinding());
		types.add(new CondKnown());
		types.add(new NumericalIntervallFinding());

		condition.setAllowedTerminalConditions(types);

		addChildType(new XMLContent(condition));
	}

	public static GuardType getInstance() {
		if (instance == null) instance = new GuardType();

		return instance;
	}

}
