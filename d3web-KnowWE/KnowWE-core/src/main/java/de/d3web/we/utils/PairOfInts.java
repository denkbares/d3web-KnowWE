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

package de.d3web.we.utils;

public class PairOfInts implements Comparable<PairOfInts> {

		private int first;
		private int second;
		
		public PairOfInts(int first, int second) {
			super();
			this.first = first;
			this.second = second;
		}
		public int getFirst() {
			return first;
		}
		
		public Integer getFirstInteger() {
			return first;
		}
		public void setFirst(int first) {
			this.first = first;
		}
		public int getSecond() {
			return second;
		}
		public void setSecond(int second) {
			this.second = second;
		}
	
		@Override
		public String toString () {
			return "[" + first + "," + second + "]";
		}

		@Override
		public int compareTo(PairOfInts o) {
			return this.getFirstInteger().compareTo(o.getFirstInteger());
		}
		
}
