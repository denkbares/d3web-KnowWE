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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Auxiliary data structure that helps to build up the list of children during
 * the initialization process of all plugins.
 * 
 * @author jochenreutelshofer
 * @created 26.08.2013
 */
public class ChildrenTypePriorityList {

	public static double DEFAULT_PRIORITY = 5;

	private final SortedMap<Double, List<Type>> children = new TreeMap<Double, List<Type>>();
	private List<Type> cachedChildrenOrder = null;

	public void replaceChildType(Type newType, Class<? extends Type> classToBeReplaced) throws InvalidKDOMSchemaModificationOperation {
		if (!classToBeReplaced.isAssignableFrom(newType.getClass())) {
			throw new InvalidKDOMSchemaModificationOperation("class"
					+ classToBeReplaced.toString() + " may not be replaced by: "
					+ newType.getClass().toString()
					+ " since it isn't a subclass of former");
		}

		Set<Double> keySet = children.keySet();
		for (Double priorityValue : keySet) {
			List<Type> childrenForPriorityValue = children.get(priorityValue);
			Type toReplace = null;
			for (Type type : childrenForPriorityValue) {
				if (classToBeReplaced.isAssignableFrom(type.getClass())) {
					toReplace = type;
					break;
				}
			}
			if (toReplace != null) {
				int index = childrenForPriorityValue.indexOf(toReplace);
				childrenForPriorityValue.set(index, newType);
				clearCache();
			}
		}
	}

	public List<Type> getChildrenTypes() {
		if (cachedChildrenOrder != null) {
			return cachedChildrenOrder;
		}
		List<Type> childrenOrder = new ArrayList<Type>();
		Set<Double> keySet = children.keySet();
		for (Double priorityValue : keySet) {
			List<Type> childrenForPriorityValue = children.get(priorityValue);
			childrenOrder.addAll(childrenForPriorityValue);
		}
		cachedChildrenOrder = childrenOrder;
		return Collections.unmodifiableList(childrenOrder);
	}

	public void clear() {
		children.clear();
	}

	private void clearCache() {
		this.cachedChildrenOrder = null;
	}

	public void addChildType(double i, Type t) {
		List<Type> list = children.get(i);
		if (list == null) {
			list = new ArrayList<Type>();
			children.put(i, list);
		}
		list.add(t);
		clearCache();
	}

	public void addChildTypeAtPosition(int pos, Type t) {
		List<Type> childrenTypes = this.getChildrenTypes();
		int index = 0;
		boolean foundIndex = false;
		for (Type type : childrenTypes) {
			if (index == pos) {
				foundIndex = true;
				addBefore(type, t);
			}
			index++;
		}
		if (!foundIndex) {
			addLast(t);
		}
	}

	/**
	 * 
	 * @created 27.08.2013
	 * @param type
	 * @param t
	 */
	private void addBefore(Type indexType, Type newType) {
		Set<Double> keySet = children.keySet();
		for (Double priorityValue : keySet) {
			List<Type> childrenForPriorityValue = children.get(priorityValue);
			if (childrenForPriorityValue.contains(indexType)) {
				childrenForPriorityValue.add(childrenForPriorityValue.indexOf(indexType), newType);
			}
		}

	}

	public void addChildType(Type t) {
		addChildType(DEFAULT_PRIORITY, t);
	}

	/**
	 * @deprecated: Use addChildType instead!
	 * @created 26.08.2013
	 * @param t
	 */
	@Deprecated
	public void add(Type t) {
		addLast(t);
	}

	public void addLast(Type t) {
		Double lastKey = DEFAULT_PRIORITY - 1;
		if (children.keySet().size() != 0) {
			lastKey = children.lastKey();
		}
		Double newLastKey = lastKey + 1;
		List<Type> set = new ArrayList<Type>();
		set.add(t);
		children.put(newLastKey, set);
		clearCache();
	}

	/**
	 * @deprecated: Use addChildType instead!
	 * @created 26.08.2013
	 * @param t
	 * @param index
	 */
	@Deprecated
	public void add(int index, Type t) {
		addChildType(index, t);
	}

	public Type removeChildType(Class<? extends Type> c) {
		Set<Double> keySet = children.keySet();
		for (Double priorityValue : keySet) {
			List<Type> childrenForPriorityValue = children.get(priorityValue);
			Type toRemove = null;
			for (Type type : childrenForPriorityValue) {
				if (c.isAssignableFrom(type.getClass())) {
					toRemove = type;
					break;
				}
			}
			if (toRemove != null) {
				childrenForPriorityValue.remove(toRemove);
				clearCache();
				return toRemove;
			}
		}
		return null;
	}

	/**
	 * Removes the child that is currently at position i;
	 * 
	 * @deprecated Use is unsecure, use removeChildType() instead!
	 * @created 26.08.2013
	 * @param i position index of child to remove
	 * @return
	 */
	@Deprecated
	public Type removeChild(int i) {
		Type type = this.getChildrenTypes().get(i);
		return removeChildType(type.getClass());
	}

	/**
	 * 
	 * @created 26.08.2013
	 * @param i
	 * @param types
	 */
	@Deprecated
	public void addAll(List<? extends Type> types) {
		for (Type type : types) {
			this.add(type);
		}
	}

	/**
	 * 
	 * @created 26.08.2013
	 * @param i
	 * @param types
	 */
	@Deprecated
	public void addAll(int i, List<? extends Type> types) {
		for (Type type : types) {
			this.add(i, type);
		}
	}

}
