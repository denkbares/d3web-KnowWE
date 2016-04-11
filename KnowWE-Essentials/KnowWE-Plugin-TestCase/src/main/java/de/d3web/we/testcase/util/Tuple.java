/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.d3web.we.testcase.util;

/**
 * 
 * @author Sebastian Furth abc
 * @created Apr 12, 2011
 */
public class Tuple<T, V> {

	private final T first;
	private final V second;

	public static <T, V> Tuple<T, V> createTuple(T first, V second) {
		return new Tuple<T, V>(first, second);
	}

	private Tuple(T first, V second) {
		if (first == null || second == null) {
			throw new NullPointerException();
		}
		this.first = first;
		this.second = second;
	}

	public T getFirst() {
		return first;
	}

	public V getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Tuple)) return false;
		@SuppressWarnings("rawtypes")
		Tuple other = (Tuple) obj;
		if (first == null) {
			if (other.first != null) return false;
		}
		else if (!first.equals(other.first)) return false;
		if (second == null) {
			if (other.second != null) return false;
		}
		else if (!second.equals(other.second)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "Tuple [first=" + first + ", second=" + second + "]";
	}

}
