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

package de.knowwe.core.kdom.objects;

import de.knowwe.core.compile.IncrementalMarker;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Interface for type containing/wrapping term names
 * 
 * @author Jochen, Albrecht
 * 
 * @param <TermObject> represents the class of the object defined or referenced
 *        by this KnowWETerm.
 */
public interface KnowWETerm<TermObject> extends IncrementalMarker {

	public static enum Scope {
		/**
		 * LOCAL terms are valid only for the master compiling them.
		 * <p/>
		 * Example: questions, answers, solution...
		 */
		LOCAL,
		/**
		 * GLOBAL terms are valid for the whole wiki, disregarding packages,
		 * respectively masters articles.
		 * <p/>
		 * Example: semantic/owl statements.
		 */
		GLOBAL
	}

	/**
	 * 
	 * Needs to return a global/package-wide unique name/identifier for this term 
	 * 
	 * @created 06.06.2011
	 * @param s
	 * @return 
	 */
	public String getTermIdentifier(Section<? extends KnowWETerm<TermObject>> s);
	
	/**
	 * Returns the (potentially short or abbreviated) name of this Term.
	 * WARNING: This name is not necessarily globally/package-wide unique
	 * 
	 * 
	 * @created 06.06.2011
	 * @param s
	 * @return 
	 */
	public String getTermName(Section<? extends KnowWETerm<TermObject>> s);

	public Class<TermObject> getTermObjectClass();

	public Scope getTermScope();

	public void setTermScope(Scope termScope);
}
