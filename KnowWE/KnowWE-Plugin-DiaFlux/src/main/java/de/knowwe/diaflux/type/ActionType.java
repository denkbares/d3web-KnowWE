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

import de.d3web.we.kdom.rules.action.RuleAction;
import de.knowwe.core.kdom.Type;
import de.knowwe.kdom.xml.AbstractXMLType;

/**
 * 
 * 
 * @author Reinhard Hatko Created on: 09.10.2009
 */
public class ActionType extends AbstractXMLType {

	private static ActionType instance;

	private ActionType() {
		super("action");
		// get the CFAction higher up in the list
		RuleAction ruleAction = new RuleAction();
		ArrayList<Type> list = new ArrayList<Type>(ruleAction.getAllowedChildrenTypes());

		for (int i = list.size() - 1; i >= 0; i--) {
			ruleAction.removeChild(i);
		}
		ruleAction.addChildType(new CallFlowActionType());

		for (Type Type : list) {
			ruleAction.addChildType(Type);
		}

		addChildType(ruleAction);
	}

	public static ActionType getInstance() {
		if (instance == null) instance = new ActionType();

		return instance;
	}

}
