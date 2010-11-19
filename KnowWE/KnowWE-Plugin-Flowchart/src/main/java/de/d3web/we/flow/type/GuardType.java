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
package de.d3web.we.flow.type;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.condition.CondKnown;
import de.d3web.we.kdom.condition.Finding;
import de.d3web.we.kdom.condition.NumericalFinding;
import de.d3web.we.kdom.condition.NumericalIntervallFinding;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;

/**
 *
 * @author Reinhard Hatko Created on: 08.10.2009
 */
public class GuardType extends AbstractXMLObjectType {


	private static GuardType instance;


	private GuardType() {
		super("guard");
		CompositeCondition condition = new CompositeCondition();


		List<KnowWEObjectType> types = new ArrayList<KnowWEObjectType>();


		try {
			// TODO remove this evil workaround
			// when updating KnowWE architecture
			types.add((KnowWEObjectType) Class.forName(
					"cc.knowwe.tdb.EvalConditionType").newInstance());
		}
		catch (Throwable e) {
			Logger.getLogger("KnowWE").log(Level.INFO,
					"cc.knowwe.tdb.EvalConditionType is not attached at GuardType");
		}

		types.add(new NodeActiveConditionType());
		types.add(new FlowchartProcessedConditionType());
		types.add(new Finding());
		types.add(new NumericalFinding());
		types.add(new CondKnown());
		types.add(new NumericalIntervallFinding());

		condition.setAllowedTerminalConditions(types);

		addChildType(condition);
	}

	public static GuardType getInstance() {
		if (instance == null)
			instance = new GuardType();

		return instance;
	}



}
