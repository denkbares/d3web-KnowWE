/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.wisec.model;

/**
 * Private class which encapsulates the name of a substance and it's current
 * rating.
 * 
 * @author Sebastian Furth
 * @created 15.06.2010
 */
public class RatedSubstance implements Comparable<RatedSubstance> {

	private final String substance;
	private double score = 0;

	// used for one criteria
	private double intermediateScore = 0;
	private int intermediateCounter = 0;

	public RatedSubstance(String substance) {
		if (substance == null) throw new IllegalArgumentException();

		this.substance = substance;
	}

	public double getScore() {
		return score;
	}

	/*
	 * Adds a value to the intermediate score
	 */
	public void addValue(double value) {
		if (value != 0) {
			this.intermediateScore += value;
			this.intermediateCounter++;
		}
	}

	/*
	 * Updates the real score at the end of the criteria processing
	 */
	public void updateScore() {
		if (intermediateCounter > 0) {
			score += intermediateScore / intermediateCounter;
			intermediateScore = 0;
			intermediateCounter = 0;
		}
	}

	public String getSubstance() {
		return substance;
	}

	@Override
	public int compareTo(RatedSubstance o) {
		// We return the reversed values by purpose to get the correct
		// sorting order!
		if (this.score < o.score) return 1;
		if (this.score > o.score) return -1;
		return this.substance.compareTo(o.substance);
	}

	@Override
	public String toString() {
		return substance + " (" + score + ")";
	}

}
