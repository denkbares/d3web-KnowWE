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

package de.d3web.tirex.core.extractionStrategies;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.tirex.core.OriginalMatchAndStrategy;

/**
 * The interface for all kinds of Strategies to extract certain portions of text
 * from wiki-files.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public interface ExtractionStrategy {

	/**
	 * @param toMatch The IDObject (Question or Answer) which is to be searched
	 *        for.
	 * @param knowledge The String, which is to be parsed and may contain the
	 *        textual representation of the IDObject "toMatch".
	 * @return An OriginalMatchAndStrategy object is created and returned if the
	 *         extraction was successful.
	 */
	public abstract OriginalMatchAndStrategy extract(IDObject toMatch, String knowledge);

	/**
	 * @return The name of the strategy.
	 */
	public abstract String getName();

	/**
	 * @return It might be useful to mark a match found by a strategy. The
	 *         Annotation provided by this method can be used to do that.
	 */
	public abstract Annotation getAnnotation();

	/**
	 * return Returns a custom "rating" for the strategy, which indicates how
	 * good it supposedly is. For example a direct match is better than a
	 * synonym match, etc.. The value returned should be chosen between 0 and 1.
	 */
	public abstract double getRating();
}
