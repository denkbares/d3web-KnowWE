/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.d3web.wisec.converter;

/**
 * A simple stopwatch implementation for measuring the time used for conversion.
 * 
 * @author joba
 * @created 21.07.2010
 */
public class Stopwatch {

	private long startTime = -1;
	private long stopTime = -1;
	private boolean running = false;

	public Stopwatch start() {
		startTime = System.currentTimeMillis();
		running = true;
		return this;
	}

	public Stopwatch stop() {
		stopTime = System.currentTimeMillis();
		running = false;
		return this;
	}

	/**
	 * Gives the used time im milliseconds.
	 * 
	 * @created 21.07.2010
	 * @return the elapsed time in milliseconds, if started; 0 otherwise
	 */
	public long getElapsedTime() {
		if (startTime == -1) {
			return 0;
		}
		if (running) {
			return System.currentTimeMillis() - startTime;
		}
		else {
			return stopTime - startTime;
		}
	}

	/**
	 * Resets all values to the initial state. Thus: running = false again.
	 * 
	 * @created 21.07.2010
	 * @return this instance in reseted state
	 */
	public Stopwatch reset() {
		startTime = -1;
		stopTime = -1;
		running = false;
		return this;
	}
}
