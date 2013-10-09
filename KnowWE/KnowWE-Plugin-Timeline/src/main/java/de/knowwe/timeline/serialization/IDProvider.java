package de.knowwe.timeline.serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
class IDProvider<T> implements Iterable<T> {

	private ArrayList<T> arrayList = new ArrayList<T>();

	@Override
	public Iterator<T> iterator() {
		return arrayList.iterator();
	}

	public int getId(T element) {
		int index = arrayList.indexOf(element);
		if (index == -1) {
			arrayList.add(element);
			index = arrayList.size() - 1;
		}
		return index;
	}
	
	public List<T> getIds() {
		return Collections.unmodifiableList(arrayList);
	}
}
