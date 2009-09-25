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

package de.d3web.we.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;

/**
 * Used as a set for KnowWEObjectTypes.
 * Makes sorting out unnecessary
 * See KnowWEEnvironment.getAllKnowWEObjectTypes().
 * 
 * @author Johannes Dienst
 *
 */
public class KnowWEObjectTypeSet{

	private HashMap<String, KnowWEObjectType> types;
	
	public KnowWEObjectTypeSet() {
		types = new HashMap<String, KnowWEObjectType>();
	}
	
	public void addAll(Collection<KnowWEObjectType> c) {
		for (KnowWEObjectType o : c) {
			types.put(o.getName(), o);
		}
	}
	
	public void add(KnowWEObjectType o) {
		types.put(o.getName(), o);
	}
	
	public boolean contains(KnowWEObjectType o) {
		if (types.get(o.getName()) != null)
			return true;
		return false;
	}
	
	public List<KnowWEObjectType> toList() {
		ArrayList<KnowWEObjectType> r = new ArrayList<KnowWEObjectType>(types.size());
		for (String s : types.keySet()) {
			r.add(types.get(s));
		}
		return r;
	}
	
	public List<KnowWEObjectType> toLexicographicalList() {
		ArrayList<KnowWEObjectType> r = new ArrayList<KnowWEObjectType>(types.size());
		for (String s : types.keySet()) {
			r.add(types.get(s));
		}
		Collections.sort(r, new KnowWEObjectTypeComparator());
		return r;
	}
}
