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

import de.knowwe.core.compile.terminology.TermCompiler;
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
	 * @param compiler The article the misspelled reference is in
	 * @param section The section the misspelled reference is in
	 * @param threshold The maximium Levenshtein distance suggestions can have. (KnowWE includes an implementation in secondstring/com.wcohen.ss.Levenstein)
	 * @return A list of {@link Suggestion} objects containing the found suggestions and their distances.
	 */
	List<Suggestion> getSuggestions(TermCompiler compiler, Section<?> section, int threshold);
	

	
}
