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

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.RootType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.ContentType;

/**
 * Default Implementation of {@link IIntervalCalculator}
 * 
 * @author Johannes Dienst
 * 
 */
public class DefaultIntervalCalculator implements IIntervalCalculator {

	private Section<?> father;
	private int[] absoluteFather;
	private List<Integer[]> tokenSpace;
	private static DefaultIntervalCalculator uniqueInstance;

	/**
	 * Singleton.
	 * 
	 * @return
	 */
	public static DefaultIntervalCalculator getInstance() {
		if (uniqueInstance == null) uniqueInstance = new DefaultIntervalCalculator();
		return uniqueInstance;
	}

	/**
	 * Needed for Singleton.
	 */
	public DefaultIntervalCalculator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Every Calculator is initialized with the father Section. It is used for
	 * its children.
	 * 
	 * @param father
	 */
	public DefaultIntervalCalculator(Section<?> father) {
		this.tokenSpace = new ArrayList<Integer[]>();
		this.father = father;
		this.absoluteFather =
				this.calculateAbsoluteFather(father);
		this.calculatePossibleIntervals();
	}

	/**
	 * Called by the constructor. If the father has already children. Their
	 * intervals are saved in tokenSpace
	 */
	protected void calculatePossibleIntervals() {
		List<Section<?>> chi = this.father.getChildren();
		int start;
		int end;
		for (Section<?> s : chi) {
			start = s.getOffSetFromFatherText() + this.absoluteFather[0];
			end = start + s.getOriginalText().length();
			this.tokenSpace.add(new Integer[] {
					start, end });
		}
	}

	/**
	 * Implements Interface method.
	 */
	public boolean isResultValid(int start, int end, String text, String clazzName) {

		// This is not optimal:
		// i.E: GN-GN and you have GN as text
		// this is ambivalent
		int index = this.father.getOriginalText().indexOf(text)
				+ this.absoluteFather[0];
		int indexEnd = index + text.length();

		if ((start < index) || (end > indexEnd)) return false;

		// catch loop when father has same type as text.
		clazzName = clazzName.substring(clazzName.lastIndexOf(".") + 1);
		if (clazzName.equals(father.getObjectType().getName())
				&& (father.getOriginalText().length() == (end - start))) return false;

		for (Integer[] i : this.tokenSpace) {
			// int t0 = i[0];
			// int t1 = i[1];

			// find if the interval is the same
			if ((start == i[0]) && (end == i[1])) return false;

			if ((start > i[0]) && (start < i[1])) return false;

			if ((end > i[0]) && (end < i[1])) return false;
		}
		return true;
	}

	/**
	 * Adds an interval, that marks a child.
	 * 
	 * @param start
	 * @param end
	 */
	public void updateTakenSpace(int start, int end) {
		this.tokenSpace.add(new Integer[] {
				start, end });
	}

	/**
	 * Gets the intervals already taken from the father.
	 * 
	 * @return
	 */
	public List<Integer[]> getTakenIntervals() {
		return this.tokenSpace;
	}

	/**
	 * Gets the absolutePosition of the Interval of this.father in the analyzed
	 * text.
	 * 
	 * @return
	 */
	public int[] getAbsoluteFather() {
		return this.absoluteFather;
	}

	@Override
	public Integer[] getRelativePositions(int begin, int end, String text) {

		// This is not optimal:
		// i.E: GN-GN and you have GN as text
		// this is ambivalent
		int index =
				this.father.getOriginalText().indexOf(text)
						+ this.absoluteFather[0];
		int indexEnd = index + text.length();

		// TODO
		index = begin - index;
		indexEnd = (index + (end - begin));
		return new Integer[] {
				index, indexEnd };
	}

	@Override
	public int[] calculateAbsoluteFather(Section<?> s) {
		int absStart = 0;
		int absEnd = s.getOriginalText().length();
		Section<?> father2 = s;

		while (!(father2.getObjectType() instanceof ContentType)
				&& !(father2.getObjectType() instanceof RootType)) {

			int offsetfromFather = father2.getOffSetFromFatherText();
			absStart += offsetfromFather;
			father2 = father2.getFather();
			if (father2 == null) break;
		}
		absEnd += absStart;
		return new int[] {
				absStart, absEnd };
	}

	@Override
	public DefaultIntervalCalculator reInit(Section<?> father) {
		this.tokenSpace = new ArrayList<Integer[]>();
		this.father = father;
		this.absoluteFather =
				this.calculateAbsoluteFather(father);
		this.calculatePossibleIntervals();
		return this;
	}
}
