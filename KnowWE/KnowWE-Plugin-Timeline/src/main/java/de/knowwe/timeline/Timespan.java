/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.timeline;

import java.util.Date;

/**
 * 
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public class Timespan implements Comparable<Timespan> {

	private final Date start;

	private final Date end;

	/**
	 * Creates a time span with <tt>date</tt> as beginning and end, so actually
	 * more like an instant of time.
	 * 
	 * @param date The only included date.
	 */
	public Timespan(Date date) {
		this.start = (Date) date.clone();
		this.end = (Date) date.clone();
	}

	/**
	 * Creates a time span lasting from <tt>start</tt> to <tt>end</tt>(closed
	 * interval, including both endpoints).
	 * 
	 * @param start The first date to be included.
	 * @param end The last date to be included.
	 */
	public Timespan(Date start, Date end) {
		this.start = (Date) start.clone();
		this.end = (Date) end.clone();
	}

	/**
	 * Returns true if and only if this Timespan contains <tt>date</tt>.
	 * 
	 * A Timespan contains a date if only if the date is not before the
	 * Timespan's start and the date is not after the Timespan's end.
	 * 
	 * @param date the date to check
	 * @return true if this Timespan contains <tt>date</tt>, false otherwise
	 */
	public boolean contains(Date date) {
		return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
	}

	public Timespan union(Timespan other) {
		Timespan intersect = this.intersect(other);
		if (intersect == null) {
			throw new IllegalArgumentException();
		}
		Date start = this.start;
		if (start.after(other.start)) start = other.start;
		Date end = this.end;
		if (end.before(other.end)) end = other.end;
		return new Timespan(start, end);
	}

	public Timespan intersect(Timespan other) {
		Date start = this.start;
		if (start.before(other.start)) start = other.start;
		Date end = this.end;
		if (end.after(other.end)) end = other.end;
		if (start.after(end)) return null;
		else return new Timespan(start, end);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Timespan other = (Timespan) obj;
		if (end == null) {
			if (other.end != null) return false;
		}
		else if (!end.equals(other.end)) return false;
		if (start == null) {
			if (other.start != null) return false;
		}
		else if (!start.equals(other.start)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + start.getTime() + ";" + end.getTime() + "]";
	}

	@Override
	public int compareTo(Timespan o) {
		return start.compareTo(o.start);
	}

	public Date getStart() {
		return (Date) start.clone();
	}

	public Date getEnd() {
		return (Date) end.clone();
	}

	public long length() {
		return end.getTime() - start.getTime();
	}
}
