package de.d3web.we.kdom;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;


public class ReviseIterator {

	private final TreeMap<Priority, LinkedList<Section<?>>> priorityMap =
			new TreeMap<Priority, LinkedList<Section<?>>>();

	private final TreeMap<Priority, LinkedList<Section<?>>> tempPriorityMap =
			new TreeMap<Priority, LinkedList<Section<?>>>();
	
	private final List<Section<?>> sectionsList = new LinkedList<Section<?>>();

	private Priority currentPriority = Priority.getRegisteredPriorities().first();

	public ReviseIterator() {
		for (Priority p : Priority.getRegisteredPriorities()) {
			priorityMap.put(p, new LinkedList<Section<?>>());
			tempPriorityMap.put(p, new LinkedList<Section<?>>());
		}
	}

	public void addSectionsToRevise(Collection<Section<?>> sections) {
		for (Section<?> sec : sections) {
			for (Priority p : sec.getObjectType().getSubtreeHandlers().keySet()) {
				if (p.compareTo(currentPriority) > 0) currentPriority = p;
				priorityMap.get(p).add(sec);
				tempPriorityMap.get(p).add(sec);
			}
			sectionsList.add(sec);
		}
	}
	

	public SectionPriorityTuple next() {
		if (tempPriorityMap.get(currentPriority).isEmpty()) {
			throw new NoSuchElementException();
		}

		SectionPriorityTuple tuple = new SectionPriorityTuple(
				tempPriorityMap.get(currentPriority).removeFirst(),
				Priority.getPriority(currentPriority.intValue()));

		while (tempPriorityMap.get(currentPriority).isEmpty()
				&& Priority.decrement(currentPriority) != null) {
			currentPriority = Priority.decrement(currentPriority);
		}
		return tuple;
	}
	
	public boolean hasNext() {
		return !tempPriorityMap.get(currentPriority).isEmpty();
	}

	public void reset() {
		for (Priority p : Priority.getRegisteredPriorities()) {
			tempPriorityMap.get(p).clear();
			tempPriorityMap.get(p).addAll(priorityMap.get(p));
		}
		currentPriority = Priority.getRegisteredPriorities().last();
		while (tempPriorityMap.get(currentPriority).isEmpty()
				&& Priority.decrement(currentPriority) != null) {
			currentPriority = Priority.decrement(currentPriority);
		}
	}

	public List<Section<?>> getAllSections() {
		return Collections.unmodifiableList(sectionsList);
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
