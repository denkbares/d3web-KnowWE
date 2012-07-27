package de.knowwe.core.compile;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import de.knowwe.core.kdom.parsing.Section;

public class ReviseIterator {

	private final List<Section<?>> rootSectionsList = new LinkedList<Section<?>>();

	private TreeMap<Priority, LinkedList<Section<?>>> priorityMap;

	private Priority currentPriority;

	private Priority stop;

	public ReviseIterator() {
		init();
	}

	private void init() {
		priorityMap = new TreeMap<Priority, LinkedList<Section<?>>>();
		for (Priority p : Priority.getRegisteredPriorities()) {
			priorityMap.put(p, new LinkedList<Section<?>>());
		}
		currentPriority = Priority.getRegisteredPriorities().first();
		stop = Priority.getRegisteredPriorities().first();
	}

	/**
	 * Adds a single section to the iterator. Successors of the section are not
	 * added.<br/>
	 * You can use this method while the article of this iterator is compiled.
	 * The iterator will continue iterating over its sections in the correct
	 * order also considering the newly added.
	 * 
	 * @created 27.07.2012
	 * @param section the section you want to add
	 */
	public void addSection(Section<?> section) {
		for (Priority p : section.get().getSubtreeHandlers().keySet()) {
			if (p.compareTo(currentPriority) > 0) currentPriority = p;
			priorityMap.get(p).add(section);
		}
	}

	/**
	 * Adds the given root section and all its successors to the iterator.<br/>
	 * You can use this method while the article of this iterator is compiled.
	 * The iterator will continue iterating over its sections in the correct
	 * order also considering the newly added.
	 * 
	 * @param rootSection the section and its successors you want to add
	 */
	public void addRootSection(Section<?> rootSection) {
		rootSectionsList.add(rootSection);
		addSectionRecursively(rootSection);
	}

	private void addSectionRecursively(Section<?> section) {
		for (Section<?> child : section.getChildren()) {
			addSectionRecursively(child);
		}
		addSection(section);
	}

	public void reset() {
		init();
		for (Section<?> rootSection : rootSectionsList) {
			addSectionRecursively(rootSection);
		}
	}

	public SectionPriorityTuple next() {
		if (!hasNext()) throw new NoSuchElementException();

		return new SectionPriorityTuple(
				priorityMap.get(currentPriority).removeFirst(),
				Priority.getPriority(currentPriority.intValue()));
	}

	public boolean hasNext() {
		if (currentPriority.compareTo(stop) < 0) return false;
		if (!priorityMap.get(currentPriority).isEmpty()) return true;

		// switch to lower priority if possible
		while (Priority.decrement(currentPriority) != null
				&& currentPriority.compareTo(stop) > 0) {
			currentPriority = Priority.decrement(currentPriority);
			if (!priorityMap.get(currentPriority).isEmpty()) return true;
		}

		return false;
	}

	public void setIteratorStop(Priority stop) {
		this.stop = stop;
	}

	public Priority getCurrentPriority() {
		return Priority.getPriority(currentPriority.intValue());
	}

	public Collection<Section<?>> getRootSections() {
		return Collections.unmodifiableCollection(rootSectionsList);
	}

	public class SectionPriorityTuple {

		private final Section<?> section;

		private final Priority priority;

		public SectionPriorityTuple(Section<?> s, Priority p) {
			this.section = s;
			this.priority = p;
		}

		public Section<?> getSection() {
			return section;
		}

		public Priority getPriority() {
			return priority;
		}

	}

}
