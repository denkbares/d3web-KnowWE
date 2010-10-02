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
package de.d3web.we.wisec.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


/**
 * Offers some often needed methods for the WISEC project.
 * 
 * @author Sebastian Furth
 * @created 02/10/2010
 */
public class WISECUtil {

	/**
	 * Returns the color specified by the "UBA traffic light" for a double
	 * value.
	 * 
	 * @created 02/10/2010
	 * @param value
	 * @return
	 */
	public static String getTrafficLightColor(double value) {
		if (value == 0) {
			return "transparent";
		}
		else if (value > 1.5) {
			return "red";
		}
		else if (value <= 1.5 && value >= -1.5) {
			return "yellow";
		}
		else if (value < -1.5) {
			return "green";
		}
		return "transparent";
	}

	/**
	 * Converts a double to a nicely formatted String.
	 * 
	 * Format: #.##
	 * 
	 * @created 02/10/2010
	 * @param value
	 * @return
	 */
	public static String format(double value) {
		return format(value, "#,##0.00");
	}

	/**
	 * Converts a double to a nicely formatted String whose format is specified
	 * by the committed format.
	 * 
	 * @created 02/10/2010
	 * @param value
	 * @param format
	 * @return
	 */
	public static String format(double value, String format) {
		DecimalFormat df = new DecimalFormat(format, new DecimalFormatSymbols(
				new Locale("en", "US")));
		return df.format(value);
	}

}
