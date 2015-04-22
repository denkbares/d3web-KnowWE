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
/**
 * Encapsulates a Suggestion found by a CorrectionProvider.
 *
 * @author Alex Legler
 * @created 20.05.2011
 */
public class DefaultSuggestion implements Suggestion{
	private String suggestion;
	private int distance;

	public DefaultSuggestion(String suggestion, int distance) {
		this.suggestion = suggestion;
		this.distance = distance;
	}

	public String getSuggestionText() {
		return suggestion;
	}

	@Override
	public String getSuggestionLabel() {
		return suggestion;
	}

	public int getDistance() {
		return distance;
	}

	@Override
	public int compareTo(Suggestion other) {
		return other.getDistance() - distance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((suggestion == null) ? 0 : suggestion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Suggestion other = (Suggestion) obj;
		if (suggestion == null) {
			if (other.getSuggestionText() != null)
				return false;
		} else if (!suggestion.equals(other.getSuggestionText()))
			return false;
		return true;
	}
}
