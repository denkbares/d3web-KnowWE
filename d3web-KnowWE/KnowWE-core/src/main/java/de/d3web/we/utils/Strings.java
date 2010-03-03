package de.d3web.we.utils;


import java.util.Collection;

public class Strings {

	/**
	 * This method appends the strings or objects and separates them with the specified
	 * separation string in between (but not at the end). You can specify all
	 * types of objects, they will be printed as {@link String#valueOf(Object)}
	 * would do.
	 * 
	 * @param separator
	 *            the separating text in between the concatenated strings
	 * @param strings
	 *            the strings to be concatenated
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
	 * This method appends the strings or objects and separates them with the specified
	 * separation string in between (but not at the end). You can specify all
	 * types of objects, they will be printed as {@link String#valueOf(Object)}
	 * would do.
	 * 
	 * @param separator
	 *            the separating text in between the concatenated strings
	 * @param strings
	 *            the strings to be concatenated
	 * @return the resulting concatenation
	 */
	public static String concat(String separator, Collection<?> strings) {
		if (strings == null) return "";
		return concat(separator, strings.toArray());
	}

}
