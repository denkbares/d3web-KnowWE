package de.d3web.KnOfficeParser.util;

import de.d3web.kernel.domainModel.Score;

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
