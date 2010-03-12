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

package de.d3web.we.action;

import java.util.List;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEObjectType;

/**
 * Changes the Activation-State of
 * an KnowWEObjectType at Runtime.
 * See KnowWeObjectTypeActivationHandler.
 * 
 * @author Johannes Dienst
 *
 */
public class KnowWEObjectTypeActivationAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap map) {
		
		// get the one needed and change its Activation state.
		List<KnowWEObjectType> types = KnowWEEnvironment.getInstance()
				.getAllKnowWEObjectTypes();
		int index = this.findIndexOfType(map.get("KnowWeObjectType"),
				types);
		
		if (index != -1) {
			KnowWEObjectType typ = types.get(index);
			if (typ != null) {
				if (!typ.getActivationStatus()) {
					typ.activateType();
				} else {
					typ.deactivateType();
				}
			}
		}
		
		return "nothing";
	}
	
	/**
	 * Searches for the Index of a type in a given List.
	 * 
	 * @param typeName
	 * @param types
	 * @return
	 */
	private int findIndexOfType(String typeName, List<KnowWEObjectType> types) {
		String shortTypeName = typeName.substring(typeName.lastIndexOf(".")+1);
		for(KnowWEObjectType typ : types) {			
			if(typ.getName().equals(shortTypeName)) {
				return types.indexOf(typ);
			}
		}
		return -1;
	}

}
