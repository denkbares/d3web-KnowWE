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

package de.knowwe.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.knowwe.core.kdom.Type;

/**
 * Used as a set for Types. Makes sorting out unnecessary See
 * KnowWEEnvironment.getAllTypes().
 * 
 * @author Johannes Dienst
 * 
 */
public class KnowWETypeSet {

	private final HashMap<String, Type> types;

	public KnowWETypeSet() {
		types = new HashMap<String, Type>();
	}

	public void addAll(Collection<Type> c) {
		for (Type o : c) {
			types.put(o.getName(), o);
		}
	}

	public void add(Type o) {
		types.put(o.getName(), o);
	}

	public boolean contains(Type o) {
		if (types.get(o.getName()) != null) return true;
		return false;
	}

	public List<Type> toList() {
		ArrayList<Type> r = new ArrayList<Type>(types.size());
		for (String s : types.keySet()) {
			r.add(types.get(s));
		}
		return r;
	}

	public Type getInstanceOf(Class<? extends Type> cl) {
		for (Type o : types.values()) {
			if (cl.isAssignableFrom(o.getClass())) {
				return o;
			}
		}

		return null;
	}

	public List<Type> toLexicographicalList() {
		ArrayList<Type> r = new ArrayList<Type>(types.size());
		for (String s : types.keySet()) {
			r.add(types.get(s));
		}
		Collections.sort(r, new KnowWETypeComparator());
		return r;
	}
}
