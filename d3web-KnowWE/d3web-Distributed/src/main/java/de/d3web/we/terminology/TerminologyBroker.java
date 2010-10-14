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

package de.d3web.we.terminology;

import java.util.HashMap;
import java.util.Map;

import de.d3web.utilities.ISetMap;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.local.LocalTerminologyAccess;
import de.d3web.we.terminology.local.LocalTerminologyStorage;
import de.d3web.we.terminology.term.Term;

public class TerminologyBroker {

	private final Map<TerminologyType, GlobalTerminology> globalTerminologies;

	public TerminologyBroker() {
		super();
		globalTerminologies = new HashMap<TerminologyType, GlobalTerminology>();
	}

	public GlobalTerminology getGlobalTerminology(TerminologyType type) {
		return globalTerminologies.get(type);
	}

	public ISetMap<Object, Term> addTerminology(TerminologyType type,
			LocalTerminologyAccess terminology, String idString,
			LocalTerminologyStorage storage) {
		GlobalTerminology gt = globalTerminologies.get(type);
		if (gt == null) {
			gt = new GlobalTerminology(type);
			globalTerminologies.put(type, gt);
		}
		ISetMap<Object, Term> result = gt.addTerminology(terminology, idString);
		return result;
	}
}
