/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.utils;

import de.d3web.we.kdom.Section;

/**
 * KnowWE works with relative positions of Sections to their father. UIMA uses
 * Absolute Positions in a given text. This interface defines the methods needed
 * to calculate the possible intervals of children. And gives the relative
 * Positions needed by the <code>Sectionizer</code>. A default Implementation is
 * the {@link DefaultIntervalCalculator}
 */
public interface IIntervalCalculator {

	/**
	 * Checks if a found Annotation is a valid(KnowWE) child of this.father. It
	 * also checks for loops.
	 * 
	 * @param start
	 * @param end
	 * @param text
	 * @param clazzName
	 * @return
	 */
	public boolean isResultValid(int start, int end, String text, String clazzName);

	/**
	 * Returns the relative position of the given text.
	 * 
	 * @param begin
	 * @param end
	 * @param text
	 * @return
	 */
	public Integer[] getRelativePositions(int begin, int end, String text);

	/**
	 * Calculates the absolute starting and end position of a given section in
	 * the UIMASection.
	 * 
	 * @param s Section
	 */
	public int[] calculateAbsoluteFather(Section<?> father);

	/**
	 * Reinit the IntervalCalculator with the given fathersection.
	 * 
	 * @param father
	 */
	public IIntervalCalculator reInit(Section<?> father);
}
