/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.d3web.we.ci4ke.util;

/**
 * Class representing a generic pair.
 * 
 * @author Sebastian Furth
 * 
 */
public class Pair<T1, T2> {

	private final T1 a;
	private final T2 b;

	public Pair(T1 a, T2 b) {
		if (a == null || b == null) {
			throw new NullPointerException("The constructor parameters can't be null!");
		}
		this.a = a;
		this.b = b;
	}

	public T1 getA() {
		return a;
	}

	public T2 getB() {
		return b;
	}

	@Override
	public String toString() {
		return "#Pair["
				+ String.valueOf(getA()) + "; "
				+ String.valueOf(getB()) + "]";
	}

}
