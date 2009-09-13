/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
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

package de.d3web.we.alignment;

import de.d3web.we.alignment.type.AbstractAlignType;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.terminology.term.Term;

public class GlobalAlignment extends Alignment {

	protected final Term term;
	
	public GlobalAlignment(Term term, IdentifiableInstance object, AbstractAlignType type) {
		super(object, type);
		this.term = term;
	}

	@Override
	public IdentifiableInstance getAligned(IdentifiableInstance ii) {
		return getObject();
	}
	
	@Override
	public boolean equals(Object o) {
		if(!super.equals(o)) return false;
		if(!(o instanceof GlobalAlignment)) return false;
		GlobalAlignment alignment = (GlobalAlignment) o;
		return alignment.getTerm().equals(term);
	}

	@Override
	public int hashCode() {
		try {
			return super.hashCode() + 19 * term.hashCode();
			
		} catch (Exception e) {
			return 0;// TODO: handle exception
		}
	}
	
	@Override
	public String toString() {
		return term.toString() + " " + super.toString();
	}

	public Term getTerm() {
		return term;
	}

	
}
