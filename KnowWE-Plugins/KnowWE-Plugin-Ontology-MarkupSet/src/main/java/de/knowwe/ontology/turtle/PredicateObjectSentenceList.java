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
package de.knowwe.ontology.turtle;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.RecursiveType;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;


public class PredicateObjectSentenceList extends AbstractType {

	/**
	 * Recursive type: it is referenced from within its own child graph (BlankNode). {@link RecursiveType} registers the
	 * instance before its children are built, so the recursion resolves to the same instance and construction
	 * terminates. The scope is per construction (and therefore per environment); see S4.7.
	 */
	private static final ThreadLocal<PredicateObjectSentenceList> CONSTRUCTION = new ThreadLocal<>();

	public static PredicateObjectSentenceList create() {
		return RecursiveType.create(CONSTRUCTION,
				PredicateObjectSentenceList::new,
				PredicateObjectSentenceList::init);
	}

	private PredicateObjectSentenceList() {
		// initialization performed in init() to prevent infinite init-loop on
		// startup
	}

	private void init() {
		this.addChildType(new PredicateSentence());
		this.setSectionFinder(new AllTextFinderTrimmed());
	}


}
