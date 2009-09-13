/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
