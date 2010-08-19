/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.hermes;

public class TimeStamp implements Comparable<TimeStamp> {

	String encodedString;
	PointInTime startPoint;

	public TimeStamp(String encodedString) {
		this.encodedString = encodedString;
		if (encodedString.contains("-")) {
			String[] stringParts = encodedString.split("-");
			startPoint = new PointInTime(stringParts[0].trim());
			endPoint = new PointInTime(stringParts[1].trim());

			// check, if string was something like 1000-700v
			// ok, that is not that nice, but that was the initial agreement
			// with
			// historians...
			if ((startPoint.getInterpretableTime() > endPoint
					.getInterpretableTime())
					&& (startPoint.getInterpretableTime() * (-1) < endPoint
							.getInterpretableTime())) {
				startPoint.setInterpretableTime(startPoint
						.getInterpretableTime()
						* (-1));
			}
		}
		else {
			startPoint = new PointInTime(encodedString.trim());
		}
	}

	public PointInTime getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(PointInTime startPoint) {
		this.startPoint = startPoint;
	}

	public PointInTime getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(PointInTime endPoint) {
		this.endPoint = endPoint;
	}

	public boolean hasEndpoint() {
		return endPoint != null;
	}

	PointInTime endPoint;

	@Override
	public String toString() {
		String result = startPoint.toString();
		if (endPoint != null) {
			result += " - " + endPoint;
		}
		return result;
	}

	public String getDescription() {
		return toString();
	}

	public double getInterpretableTime() {
		return startPoint.getInterpretableTime();
	}

	@Override
	public int compareTo(TimeStamp o) {
		return Double.compare(getInterpretableTime(), o.getInterpretableTime());
	}

	public static String decode(String encodedString) {
		return new TimeStamp(encodedString).getDescription();
	}

}
