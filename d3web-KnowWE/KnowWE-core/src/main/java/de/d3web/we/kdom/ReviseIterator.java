package de.d3web.we.kdom;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;


public class ReviseIterator {

	private final List<Section<?>> rootSectionsList = new LinkedList<Section<?>>();

	private TreeMap<Priority, LinkedList<Section<?>>> priorityMap;
	
	private List<Section<?>> allSectionsList;

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
		allSectionsList = new LinkedList<Section<?>>();
		currentPriority = Priority.getRegisteredPriorities().first();
		stop = Priority.getRegisteredPriorities().first();
	}

	private void addToPriorityMap(List<Section<?>> sections) {
		for (Section<?> sec : sections) {
			for (Priority p : sec.getObjectType().getSubtreeHandlers().keySet()) {
				if (p.compareTo(currentPriority) > 0) currentPriority = p;
				priorityMap.get(p).add(sec);
			}
			allSectionsList.add(sec);
		}
	}

	public void addRootSectionToRevise(Section<?> rootSection) {
		rootSectionsList.add(rootSection);
		List<Section<?>> sections = new LinkedList<Section<?>>();
		rootSection.getAllNodesPostOrder(sections);
		addToPriorityMap(sections);
	}
	
	public void reset() {
		init();
		for (Section<?> rootSection : rootSectionsList) {
			List<Section<?>> sections = new LinkedList<Section<?>>();
			rootSection.getAllNodesPostOrder(sections);
			addToPriorityMap(sections);
		}
	}

	public SectionPriorityTuple next() {
		if (priorityMap.get(currentPriority).isEmpty()) {
			throw new NoSuchElementException();
		}

		SectionPriorityTuple tuple = new SectionPriorityTuple(
				priorityMap.get(currentPriority).removeFirst(),
				Priority.getPriority(currentPriority.intValue()));

		nextPriority();
		return tuple;
	}
	
	public boolean hasNext() {
		return !priorityMap.get(currentPriority).isEmpty();
	}

	private void nextPriority() {
		while (priorityMap.get(currentPriority).isEmpty()
				&& Priority.decrement(currentPriority) != null
				&& currentPriority.compareTo(stop) > 0) {
			currentPriority = Priority.decrement(currentPriority);
		}
	}

	public void setIteratorStop(Priority stop) {
		this.stop = stop;
		nextPriority();
	}

	public List<Section<?>> getAllSections() {
		return Collections.unmodifiableList(allSectionsList);
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
