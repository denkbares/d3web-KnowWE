/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.correction;

import java.util.List;

import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;

/**
 * The common interface for term correction providers.
 *
 * <b>Important:</b><br />
 * To create a new provider, create a class implementing this interface and add it to plugin.xml,
 * extending the "CorrectionProvider" extension point.
 * 
 * Be sure to specify the correct scope parameters for the (object) types you support.
 * <b>Also, you need to add the object type 
 *  
 * @author Alex Legler
 * @created 20.02.2011 
 */
public interface CorrectionProvider {

	/**
	 * Returns a list of suggestions for a given section of an article,
	 * that have a levenshtein distance of no more than <tt>threshold</tt>. 
	 * 
	 * @created 20.02.2011
	 * @param article The article the misspelled reference is in
	 * @param section The section the misspelled reference is in
	 * @param threshold The maximium Levenshtein distance suggestions can have. (KnowWE includes an implementation in secondstring/com.wcohen.ss.Levenstein)
	 * @return A list of {@link Suggestion} objects containing the found suggestions and their distances.
	 */
	public List<Suggestion> getSuggestions(Article article, Section<?> section, int threshold);
	
	/**
	 * Encapsulates a Suggestion found by a CorrectionProvider.
	 * 
	 * @author Alex Legler
	 * @created 20.05.2011
	 */
	public class Suggestion implements Comparable<Suggestion> {
		private String suggestion;
		private int distance;
		
		public Suggestion(String suggestion, int distance) {
			this.suggestion = suggestion;
			this.distance = distance;
		}

		/**
		 * Returns the suggested replacement string
		 * 
		 * @created 20.05.2011
		 * @return The suggested string
		 */
		public String getSuggestion() {
			return suggestion;
		}
		
		/**
		 * Returns the distance from the misspelled string, used for sorting
		 * 
		 * @created 20.05.2011
		 * @return distance
		 */
		public int getDistance() {
			return distance;
		}

		@Override
		public int compareTo(Suggestion other) {
				return other.distance - distance;
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
				if (other.suggestion != null)
					return false;
			} else if (!suggestion.equals(other.suggestion))
				return false;
			return true;
		}
	}
	
}
