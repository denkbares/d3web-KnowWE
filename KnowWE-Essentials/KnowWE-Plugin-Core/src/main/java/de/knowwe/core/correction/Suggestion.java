/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.core.correction;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 21.04.15
 */
public interface Suggestion extends Comparable<Suggestion>  {

	/**
	 * Returns the suggested replacement string
	 *
	 * @created 20.05.2011
	 * @return The suggested string
	 */
	String getSuggestionText();

	/**
	 * Returns the label of the suggestion
	 *
	 * @created 21.04.2015
	 * @return The label of the suggestion
	 */
	String getSuggestionLabel();

	/**
	 * Returns the distance from the misspelled string, used for sorting
	 *
	 * @created 20.05.2011
	 * @return distance
	 */
	int getDistance();

	/**
	 * Returns true if the suggestion text is a call of a javascript function
	 * The default implementation returns false!
	 *
	 * @return true if suggestion is a call of a function
	 */
	default boolean isScript() {
		return false;
	}
}
