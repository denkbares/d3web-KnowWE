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
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;


public class PredicateObjectSentenceList extends AbstractType {

	private static PredicateObjectSentenceList instance = null;

	public static PredicateObjectSentenceList getInstance() {
		if (instance == null) {
			instance = new PredicateObjectSentenceList();
			instance.init();
		}
		return instance;
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
