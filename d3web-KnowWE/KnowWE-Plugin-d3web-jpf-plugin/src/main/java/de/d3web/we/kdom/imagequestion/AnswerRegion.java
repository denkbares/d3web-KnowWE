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
package de.d3web.we.kdom.imagequestion;

/**
 * Copied from old Dialog2. Stores the Information of an AnswerRegion.
 * 
 * @author Johannes Dienst
 * 
 */
public class AnswerRegion {

	private String answerID;

	private int xStart;

	private int xEnd;

	private int yStart;

	private int yEnd;

	public AnswerRegion(String answerID, int xStart, int xEnd, int yStart,
			int yEnd) {
		this.answerID = answerID;
		this.xStart = xStart;
		this.xEnd = xEnd;
		this.yStart = yStart;
		this.yEnd = yEnd;
	}

	public String getAnswerID() {
		return answerID;
	}

	public int getHeight() {
		return yEnd - yStart;
	}

	public int getWidth() {
		return xEnd - xStart;
	}

	// Getter and setter

	public int getXEnd() {
		return xEnd;
	}

	public int getXStart() {
		return xStart;
	}

	public int getYEnd() {
		return yEnd;
	}

	public int getYStart() {
		return yStart;
	}

	@Override
	public String toString() {
		return "<AnswerRegion answerID=" + answerID + " xStart=" + xStart
				+ " xEnd=" + xEnd + " yStart=" + yStart + " yEnd=" + yEnd
				+ " />";
	}

}
