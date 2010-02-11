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

package de.d3web.KnOfficeParser.util;

import de.d3web.scoring.Score;

/**
 * Klasse bietet statische Methoden um Score zu finden 
 * @author Markus Friedrich
 *
 */
public class Scorefinder {
	
	/**
	 * Liefert den Score zu einem String
	 * 
	 * @param value der Score als String
	 * @return der Score als Score Objekt
	 */
	public static Score getScore(String value) {
		if (value.equals("P7") || value.equals("!")) {
			return Score.P7;
		}
		if (value.equals("P6")) {
			return Score.P6;
		}
		if (value.equals("P5")) {
			return Score.P5;
		}
		if (value.equals("P4")) {
			return Score.P4;
		}
		if (value.equals("P3") || value.equals("?")) {
			return Score.P3;
		}
		if (value.equals("P2")) {
			return Score.P2;
		}
		if (value.equals("P1")) {
			return Score.P1;
		}
		if (value.equals("N1")) {
			return Score.N1;
		}
		if (value.equals("N2")) {
			return Score.N2;
		}
		if (value.equals("N3")) {
			return Score.N3;
		}
		if (value.equals("N4")) {
			return Score.N4;
		}
		if (value.equals("N5")) {
			return Score.N5;
		}
		if (value.equals("N6")) {
			return Score.N6;
		}
		if (value.equals("N7")) {
			return Score.N7;
		}
		return null;
	}

}
