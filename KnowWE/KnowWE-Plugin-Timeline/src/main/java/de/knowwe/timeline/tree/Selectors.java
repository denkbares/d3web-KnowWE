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
package de.knowwe.timeline.tree;

import de.d3web.core.session.Value;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public enum Selectors {
	CHANGE {
		@Override
		public Double getValue(Double lastVal, Double newVal) {
			if (lastVal == null)
				return newVal;
			if (lastVal.equals(newVal))
				return null;
			return Math.abs(lastVal - newVal);
		}

		@Override
		public boolean matches(Value lastVal, Value newVal) {
			return lastVal == null || !lastVal.equals(newVal);
		}
	},
	INCREASE {
		@Override
		public Double getValue(Double lastVal, Double newVal) {
			if (lastVal == null)
				return newVal;
			if (lastVal > newVal)
				return null;
			return newVal - lastVal;
		}
		
		@Override
		public boolean matches(Value lastVal, Value newVal) {
			return lastVal == null || lastVal.compareTo(newVal) < 0;
		}
	},
	DECREASE {
		@Override
		public Double getValue(Double lastVal, Double newVal) {
			if (lastVal == null)
				return newVal;
			if (lastVal < newVal)
				return null;
			return lastVal - newVal;
		}
		
		@Override
		public boolean matches(Value lastVal, Value newVal) {
			return lastVal == null || lastVal.compareTo(newVal) > 0;
		}
	};

	public abstract Double getValue(Double lastVal, Double newVal);

	public abstract boolean matches(Value lastVal, Value newVal);

}
