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
package de.knowwe.timeline.tree;

/**
 * A <tt>TimeUnit</tt> represents time durations at a given unit of granularity.
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public enum TimeUnit {
	MSEC(1), SEC(1000), MIN(1000*60), HOUR(1000*60*60), DAY(1000*60*60*24);
	
	private long multiplier;

	private TimeUnit(long multiplier) {
		this.multiplier = multiplier;
	}

	public long getMultiplier() {
		return multiplier;
	}
}
