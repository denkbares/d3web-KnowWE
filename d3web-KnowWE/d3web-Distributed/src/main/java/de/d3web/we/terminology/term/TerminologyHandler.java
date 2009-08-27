package de.d3web.we.terminology.term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class TerminologyHandler<T, E> implements Iterable<E> {
	
	protected T terminology;
	private Collection<Class> exclusiveFilters;
	private Collection<Class> filters;
	
	public TerminologyHandler() {
		super();
		exclusiveFilters = new ArrayList<Class>();
		filters = new ArrayList<Class>();
	}
	
	public abstract TerminologyHandler newInstance();
	
	public void setTerminology(T terminology) {
		this.terminology = terminology;
	}

	public T getTerminology() {
		return terminology;
	}
		
	public Iterator<E> iterator() {
		return fifo(getTerminology()).iterator();
	}

	protected abstract Iterable<E> fifo(T terminology);

	public Collection<Class> getExclusiveFilters() {
		return exclusiveFilters;
	}

	public void setExclusiveFilters(Collection<Class> exclusiveFilters) {
		this.exclusiveFilters = exclusiveFilters;
	}
	
	public Collection<Class> getFilters() {
		return filters;
	}

	public void setFilters(Collection<Class> filters) {
		this.filters = filters;
	}

	public boolean checkFilter(Object obj) {
		boolean result = true;
		for (Class context : getFilters()) {
			result &= context.isAssignableFrom(obj.getClass());
			if(!result) return result;
		}
		for (Class context : getExclusiveFilters()) {
			result &= !context.isAssignableFrom(obj.getClass());
			if(!result) return result;
		}
		return result;
	}
	
	
	
	
}
