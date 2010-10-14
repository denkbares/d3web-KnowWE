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

package de.d3web.we.terminology.global;

import java.util.Collection;
import java.util.TreeSet;

import de.d3web.utilities.ISetMap;
import de.d3web.we.alignment.AlignmentUtilRepository;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.terminology.local.LocalTerminologyAccess;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermFactory;
import de.d3web.we.terminology.term.TermInfoType;

public class GlobalTerminology {

	private TerminologyType type;
	private Collection<Term> terms;

	public GlobalTerminology(TerminologyType type) {
		super();
		this.type = type;
		terms = new TreeSet<Term>();
	}

	public TerminologyType getType() {
		return type;
	}

	public ISetMap<Object, Term> addTerminology(LocalTerminologyAccess terminology, String idString) {
		TermFactory termFactory = AlignmentUtilRepository.getInstance().getTermFactory(
				terminology.getContext());
		return termFactory.addTerminology(terminology, idString, this);
	}

	public Term getTerm(String name, Object value) {
		GlobalTerminologyHandler globalHandler = AlignmentUtilRepository.getInstance().getGlobalTerminogyHandler(
				this.getClass());
		globalHandler.setTerminology(this);
		for (Term term : globalHandler) {
			if (term.getInfo(TermInfoType.TERM_NAME).equals(name)) {
				if (value == null) {
					if (term.getInfo(TermInfoType.TERM_VALUE) == null) {
						return term;
					}
				}
				else {
					if (term.getInfo(TermInfoType.TERM_VALUE) != null
							&& term.getInfo(TermInfoType.TERM_VALUE).equals(value)) {
						return term;
					}
				}
			}
		}
		return null;
	}

	public final Collection<Term> getAllTerms() {
		return terms;
	}

	public void setTerms(Collection<Term> terms) {
		terms = new TreeSet<Term>(terms);
	}

	public void addTerm(Term term) {
		terms.add(term);
	}

}
