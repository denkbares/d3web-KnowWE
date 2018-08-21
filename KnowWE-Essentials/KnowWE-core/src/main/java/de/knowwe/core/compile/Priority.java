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

import org.jetbrains.annotations.NotNull;

public final class Priority implements Comparable<Priority> {

	private static final TreeSet<Priority> registeredPrioritiesTree = new TreeSet<>();

	private static final HashMap<Integer, Priority> registeredPrioritiesHash = new HashMap<>();

	// creation of the compiler thread, start creating the artifacts (TerminologyManager, KnowledgeBase, Ontology, ...)
	public static final Priority INIT = new Priority(-1000);

	// compiler has created its artifacts (TerminologyManager, KnowledgeBase, Ontology, ...) and prepares the compile scripts
	public static final Priority PREPARE = new Priority(-999);

	public static final Priority HIGHEST = new Priority(-300);

	public static final Priority HIGHER = new Priority(-200);

	public static final Priority HIGH = new Priority(-100);

	public static final Priority ABOVE_DEFAULT = new Priority(-1);

	public static final Priority DEFAULT = new Priority(0);

	public static final Priority BELOW_DEFAULT = new Priority(1);

	public static final Priority LOW = new Priority(100);

	public static final Priority LOWER = new Priority(200);

	public static final Priority LOWEST = new Priority(300);

	public static final Priority DONE = new Priority(1000);

	private final int value;

	private Priority(int value) {
		this.value = value;
		registeredPrioritiesTree.add(this);
		registeredPrioritiesHash.put(value, this);
	}

	public int intValue() {
		return value;
	}

	public static TreeSet<Priority> getRegisteredPriorities() {
		return registeredPrioritiesTree;
	}

	/**
	 * Returns the next higher Priority or null, if there is no higher Priority.
	 * <p>
	 * If this method returns null, you may simply need to add a new higher Priority in this class.
	 */
	public static Priority increment(Priority p) {
		return getRegisteredPriorities().higher(p);
	}

	/**
	 * Returns the next lower Priority or null if there is no lower Priority.
	 * <p>
	 * If this method returns null, you may simply need to add a new lower Priority in this class.
	 */
	public static Priority decrement(Priority p) {
		return getRegisteredPriorities().lower(p);
	}

	public static Priority getPriority(int value) {
		return registeredPrioritiesHash.get(value);
	}

	@Override
	public int compareTo(@NotNull Priority priority) {
		return Integer.compare(priority.value, this.value);
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
		return value == other.value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}