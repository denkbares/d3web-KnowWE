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

package de.knowwe.core.compile;

import java.util.HashMap;
import java.util.TreeSet;

public class Priority implements Comparable<Priority> {

	private static final TreeSet<Priority> registeredPrioritiesTree = new TreeSet<Priority>();

	private static final HashMap<Integer, Priority> registeredPrioritiesHash = new HashMap<Integer, Priority>();

	public static final Priority INIT = new Priority(-1000);

	public static final Priority HIGHEST = new Priority(-300);

	public static final Priority HIGHER = new Priority(-200);

	public static final Priority HIGH = new Priority(-100);

	public static final Priority ABOVE_DEFAULT = new Priority(-1);

	public static final Priority DEFAULT = new Priority(0);

	public static final Priority BELOW_DEFAULT = new Priority(1);

	public static final Priority LOW = new Priority(100);

	public static final Priority LOWER = new Priority(200);

	public static final Priority LOWEST = new Priority(300);

	private final int value;

	private Priority(int value) {
		this.value = value;
		registeredPrioritiesTree.add(this);
		registeredPrioritiesHash.put(value, this);
	}

	// private to prevent usage
	private Priority() {
		this(0);
	}

	public int intValue() {
		return value;
	}

	public static TreeSet<Priority> getRegisteredPriorities() {
		return registeredPrioritiesTree;
	}

	/**
	 * Returns the next higher Priority or null, if there is no higher Priority.
	 * 
	 * If this method returns null, you may simply need to add a new higher
	 * Priority in this class.
	 */
	public static Priority increment(Priority p) {
		return getRegisteredPriorities().higher(p);
	}

	/**
	 * Returns the next lower Priority or null if there is no lower Priority.
	 * 
	 * If this method returns null, you may simply need to add a new lower
	 * Priority in this class.
	 */
	public static Priority decrement(Priority p) {
		return getRegisteredPriorities().lower(p);
	}

	public static Priority getPriority(int value) {
		return registeredPrioritiesHash.get(value);
	}

	@Override
	public int compareTo(Priority p) {
		if (p == null) return -1;
		return this.value < p.value ? 1 : (this.value == p.value ? 0 : -1);
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Priority other = (Priority) obj;
		if (value != other.value) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	// /**
	// * Creates a Map of Lists with Sections that contain SubTreeHandlers with
	// a
	// * certain Priority. The Lists are mapped by this common Priority of the
	// * SubTreeHandlers.
	// * <p />
	// * The Sections in each List of the returned Map occur in the same order
	// as
	// * they occur in the given List of Sections.
	// * <p />
	// * Sections may appear in multiple Lists, if they contain multiple
	// * SubtreeHandlers with different Priorities.
	// *
	// * @param sections
	// */
	// public static TreeMap<Priority, List<Section<? extends
	// Type>>> createPrioritySortedList(List<Section<? extends
	// Type>> sections) {
	//
	// TreeMap<Priority, List<Section<? extends Type>>> priorityMap
	// = new TreeMap<Priority, List<Section<? extends Type>>>();
	//
	// for (Section<? extends Type> section : sections) {
	//
	// for (Priority p : section.get().getSubtreeHandlers().keySet())
	// {
	//
	// List<Section<? extends Type>> singlePrioList =
	// priorityMap.get(p);
	//
	// if (singlePrioList == null) {
	// singlePrioList = new ArrayList<Section<? extends Type>>();
	// priorityMap.put(p, singlePrioList);
	// }
	// singlePrioList.add(section);
	// }
	// }
	//
	// return priorityMap;
	//
	// }

}