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

package de.d3web.we.utils;

import java.util.Collection;

public class Strings {

	/**
	 * This method appends the strings or objects and separates them with the
	 * specified separation string in between (but not at the end). You can
	 * specify all types of objects, they will be printed as
	 * {@link String#valueOf(Object)} would do.
	 * 
	 * @param separator the separating text in between the concatenated strings
	 * @param strings the strings to be concatenated
	 * @return the resulting concatenation
	 */
	public static String concat(String separator, Object[] strings) {
		StringBuilder result = new StringBuilder();
		if (strings != null) {
			for (int i = 0; i < strings.length; i++) {
				if (i > 0) result.append(separator);
				result.append(strings[i]);
			}
		}
		return result.toString();
	}

	/**
	 * This method appends the strings or objects and separates them with the
	 * specified separation string in between (but not at the end). You can
	 * specify all types of objects, they will be printed as
	 * {@link String#valueOf(Object)} would do.
	 * 
	 * @param separator the separating text in between the concatenated strings
	 * @param strings the strings to be concatenated
	 * @return the resulting concatenation
	 */
	public static String concat(String separator, Collection<?> strings) {
		if (strings == null) return "";
		return concat(separator, strings.toArray());
	}

	/**
	 * Returns a copy of the string, with leading and trailing whitespace
	 * omitted.
	 * <p>
	 * If this <code>String</code> object represents an empty character
	 * sequence, or the first character of character sequence represented by
	 * this <code>String</code> object has a code greater than <code>'&#92;u0020'</code> (the space
	 * character), then a reference to this <code>String</code> object is
	 * returned.
	 * <p>
	 * Otherwise, if there is no character with a code greater than <code>'&#92;u0020'</code> in
	 * the string, then a new <code>String</code> object representing an empty
	 * string is created and returned.
	 * <p>
	 * Otherwise, let <i>k</i> be the index of the first character in the string
	 * whose code is greater than <code>'&#92;u0020'</code>. A new <code>String</code> object is
	 * created, representing the substring of this string that begins with the
	 * character at index <i>k</i>, the result of
	 * <code>this.substring(<i>k</i>)</code>.
	 * <p>
	 * This method may be used to trim whitespace (as defined above) from the
	 * beginning and end of a string.
	 * 
	 * @return A copy of this string with leading white space removed, or this
	 *         string if it has no leading white space.
	 */
	public static String trimLeft(String text) {
		if (text == null) return null;
		int pos = 0;
		int len = text.length();
		while ((pos < len) && (text.charAt(pos) <= ' ')) {
			pos++;
		}
		return (pos == 0) ? text : text.substring(pos);
	}

}
