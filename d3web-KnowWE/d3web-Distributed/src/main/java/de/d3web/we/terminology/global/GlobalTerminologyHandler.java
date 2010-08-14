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

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TerminologyHandler;

public abstract class GlobalTerminologyHandler extends TerminologyHandler<GlobalTerminology, Term> {

	protected List<Term> fifo(GlobalTerminology gt) {
		/*
		 * List<Term> queue = new LinkedList<Term>(); for (Term term :
		 * gt.getRoots()) { if(checkFilter(term)) { queue.add(term);; } } queue
		 * = fifo(queue); for (Term term : gt.getUnstructuredTerms()) {
		 * if(checkFilter(term)) { queue.add(term);; } }
		 */
		return new ArrayList<Term>(gt.getAllTerms());
	}
	/*
	 * private List<Term> fifo(List<? extends Term> toExpand) { List<Term>
	 * result = new LinkedList<Term>(); for (Term term : toExpand) {
	 * if(checkFilter(term)) { result.add(term); } result.addAll(fifo(new
	 * ArrayList<Term>(term.getChildren()))); } return result; }
	 */

}
