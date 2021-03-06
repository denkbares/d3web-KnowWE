/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.core.kdom;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.denkbares.collections.PriorityList;

/**
 * Auxiliary data structure that helps e.g. to build up the list of children types during the initialization process of
 * all plugins.
 *
 * @author Jochen Reutelshoefer
 * @created 26.08.2013
 */
public class TypePriorityList {

	public static double DEFAULT_PRIORITY = 5;

	/**
	 * Data structure containing the types according to specified priorities (if such are given). For each priority
	 * value a list is managed. According to the final total order of the types the types within a list have descending
	 * order.
	 */
	private final PriorityList<Double, Type> types = new PriorityList<>(DEFAULT_PRIORITY);

	/**
	 * Replaces the first type with the passed type where the type is instance of the passed class, if such is
	 * existing.
	 *
	 * @param classToBeReplaced class to determine what type should be replaced
	 * @param newType           type to be inserted
	 * @return the replaced type if the replacement was successful, else
	 * <tt>null</tt> is returned
	 * @created 27.08.2013
	 */
	public Type replaceType(Class<? extends Type> classToBeReplaced, Type newType) {
		for (int i = 0; i < types.size(); i++) {
			Type type = types.get(i);
			if (classToBeReplaced.isAssignableFrom(type.getClass())) {
				types.set(i, newType);
				return type;
			}
		}
		return null;
	}

	/**
	 * Returns the final (at the point of execution) total order of the types.
	 *
	 * @created 27.08.2013
	 */
	@NotNull
	public List<Type> getTypes() {
		return Collections.unmodifiableList(types);
	}

	/**
	 * Clears the type data structure.
	 *
	 * @created 27.08.2013
	 */
	public void clear() {
		types.clear();
	}

	/**
	 * Adds the type with the given priority value. If there are already types for the given priority it is appended at
	 * the end of the list (lower priority).
	 *
	 * @param priority the priority with which the type is added
	 * @param t        the type to add
	 * @created 27.08.2013
	 */
	public void addType(double priority, Type t) {
		types.add(priority, t);
	}

	/**
	 * Adds the type as the (currently) last type with the default priority (which is 5).
	 *
	 * @param type the type to add
	 * @created 27.08.2013
	 */
	public void addType(Type type) {
		addType(DEFAULT_PRIORITY, type);
	}

	/**
	 * Adds the given type at the end of the (current) priority chain.
	 *
	 * @created 27.08.2013
	 */
	public void addLast(Type type) {
		if (types.isEmpty()) {
			types.add(type);
		}
		else {
			types.add(types.getHighestPriority() + 1, type);
		}
	}

	/**
	 * Removes the first occurrence (descending priority order) of a type where the given class c is assignable from
	 * this type.
	 *
	 * @created 27.08.2013
	 */
	public Type removeType(Class<? extends Type> typeClass) {
		Iterator<Type> iterator = types.iterator();
		while (iterator.hasNext()) {
			Type type = iterator.next();
			if (typeClass.isAssignableFrom(type.getClass())) {
				iterator.remove();
				return type;
			}
		}
		return null;
	}
}
