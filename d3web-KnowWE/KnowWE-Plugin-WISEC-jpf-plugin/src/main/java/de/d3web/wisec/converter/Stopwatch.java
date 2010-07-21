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
