/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.timeline;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.knowwe.timeline.tree.Comparators;

/**
 * Needs performance optimizations
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public class Timeset implements Iterable<Timespan> {
	public SortedSet<Timespan> spans;

	public Timeset() {
		spans = new TreeSet<Timespan>();
	}

	public Timeset add(Timespan x) {
		if(x == null)
			throw new IllegalArgumentException();
		Set<Timespan> toRemove = new HashSet<Timespan>();
		for (Timespan ts : spans) {
			if (ts.intersect(x) == null)
				continue;
			toRemove.add(ts);
			x = x.union(ts);
		}
		spans.removeAll(toRemove);
		spans.add(x);
		return this;
	}

	/**
	 * Checks whether this Timeset contains the given date.
	 * 
	 * @param date
	 * 	The date to be checked.
	 * @return
	 * 	 {@code true} if the Timeset contains date argument;  {@code false} otherwise
	 */
	public boolean contains(Date date) {
		for (Timespan ts : spans) {
			if (ts.contains(date))
				return true;
		}
		return false;
	}

	// O(n^2) :(
	public Timeset union(Timeset other) {
		for (Timespan ts : other.spans)
			this.add(ts);
		return this;
	}

	// O(n^2) :(
	public Timeset intersect(Timeset other) {
		Timeset temp = new Timeset();
		for (Timespan ts : spans) {
			for (Timespan ts2 : other.spans) {
				Timespan span = ts.intersect(ts2);
				if(span != null)
					temp.add(span);
			}
		}
		spans = temp.spans;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		for (Timespan span : spans) {
			if (s.length() > 0)
				s.append("U");
			s.append(span.toString());
		}
		return s.toString();
	}

	@Override
	public Iterator<Timespan> iterator() {
		return spans.iterator();
	}

	public Timeset removeDurationNotMatching(long duration, Comparators comp) {
		Set<Timespan> toRemove = new HashSet<Timespan>();
		for (Timespan ts : spans) {
			if (!comp.matches(ts.length(), duration))
				toRemove.add(ts);
		}
		spans.removeAll(toRemove);
		return this;
	}

	public int getCount() {
		return spans.size();
	}

}
