package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class Priority implements Comparable<Priority> {

	private static final TreeSet<Priority> registeredPriorities = new TreeSet<Priority>();

	public static final Priority PRECOMPILE = new Priority(100000);

	public static final Priority HIGHEST = new Priority(300);

	public static final Priority HIGHER = new Priority(200);

	public static final Priority HIGH = new Priority(100);

	public static final Priority DEFAULT = new Priority(0);

	public static final Priority LOW = new Priority(-100);

	public static final Priority LOWER = new Priority(-200);

	public static final Priority LOWEST = new Priority(-300);

	public static final Priority POSTCOMPILE = new Priority(-100000);

	private final int value;

	private Priority(int value) {
		this.value = value;
		registeredPriorities.add(this);
	}

	// private to prevent usage
	private Priority() {
		this(0);
	}

	public int intValue() {
		return value;
	}

	public static TreeSet<Priority> getRegisteredPriorities() {
		return registeredPriorities;
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

	@Override
	public int compareTo(Priority p) {
		return Integer.valueOf(value).compareTo(Integer.valueOf(p.value));
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Priority)) {
			return false;
		}
		return value == ((Priority) o).value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	/**
	 * Creates a Map of Lists with Sections that contain SubTreeHandlers with a
	 * certain Priority. The Lists are mapped by this common Priority of the
	 * SubTreeHandlers.
	 * <p />
	 * The Sections in each List of the returned Map occur in the same order as
	 * they occur in the given List of Sections.
	 * <p />
	 * Sections may appear in multiple Lists, if they contain multiple
	 * SubtreeHandlers with different Priorities.
	 * 
	 * @param sections
	 */
	public static TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> createPrioritySortedList(List<Section<? extends KnowWEObjectType>> sections) {

		TreeMap<Priority, List<Section<? extends KnowWEObjectType>>> priorityMap = new TreeMap<Priority, List<Section<? extends KnowWEObjectType>>>();

		for (Section<? extends KnowWEObjectType> section : sections) {

			for (Priority p : section.getObjectType().getSubtreeHandlers().keySet()) {

				List<Section<? extends KnowWEObjectType>> singlePrioList = priorityMap.get(p);

				if (singlePrioList == null) {
					singlePrioList = new ArrayList<Section<? extends KnowWEObjectType>>();
					priorityMap.put(p, singlePrioList);
				}
				singlePrioList.add(section);
			}
		}

		return priorityMap;

	}

}