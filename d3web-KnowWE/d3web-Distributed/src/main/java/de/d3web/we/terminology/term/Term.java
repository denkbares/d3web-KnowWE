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

package de.d3web.we.terminology.term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.we.basic.TerminologyType;

public class Term implements Comparable<Term>{

	private Map<TermInfoType, Object> termInfos;
	private List<Term> parents;
	private List<Term> children;
	private final TerminologyType type;
	
	public Term(TerminologyType type) {
		super();
		this.type = type;
		termInfos = new HashMap<TermInfoType, Object>();
		parents = new ArrayList<Term>();
		children = new ArrayList<Term>();
	}
	
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof Term)) return false;
		Term other = (Term) o;
		return other.getTermInfos().equals(getTermInfos());
	}
	
	public int hashCode() {
		return termInfos.hashCode();
	}
	
	public Object getInfo(TermInfoType infoType) {
		return termInfos.get(infoType);
	}
	
	public void setInfo(TermInfoType infoType, Object object) {
		termInfos.put(infoType, object);
	}
	
	public Collection<Term> getChildren() {
		return children;
	}

	public Collection<Term> getParents() {
		return parents;
	}

	public Map<TermInfoType, Object> getTermInfos() {
		return termInfos;
	}

	public void setTermInfos(Map<TermInfoType, Object> termInfos) {
		this.termInfos = termInfos;
	}

	public void setChildren(Collection<Term> newChildren) {
		children = new ArrayList<Term>(newChildren);
	}

	public void setParents(Collection<Term> newParents) {
		parents = new ArrayList<Term>(newParents);
	}

	public void addChild(Term child) {
		if(!children.contains(child)) { 
			children.add(child);
		}
	}

	public void addParent(Term parent) {
		if(!parents.contains(parent)) {
			parents.add(parent);
		}
	}
	
	public List<Term> getAncestors() {
		return getAncestors(this);	
	}
	
	private List<Term> getAncestors(Term fixpoint) {
		List<Term> result = new ArrayList<Term>();
		for (Term parent : fixpoint.getParents()) {
			result.addAll(getAncestors(parent));
		}
		return result;
	}
	
	
	public boolean isAncestor(Term term) {
		return isAncestor(this, term);
	}
	
	private boolean isAncestor(Term fixpoint, Term search) {
		for (Term parent : fixpoint.getParents()) {
			if(parent.equals(search)) {
				return true;
			} else {
				return isAncestor(parent, search);
			}
		}
		return false;
	}
	
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(getInfo(TermInfoType.TERM_VALUE) != null) {
			sb.append(getInfo(TermInfoType.TERM_VALUE));
			sb.append(" of ");
		}
		sb.append("Term: ");
		sb.append(getInfo(TermInfoType.TERM_NAME));
		return sb.toString();
	}

	public int compareTo(Term o) {
		if(o == null) {
			return -1;
		}
		String myName = (String)getInfo(TermInfoType.TERM_NAME);
		String otherName = (String)o.getInfo(TermInfoType.TERM_NAME);
		
		
		Object myValue = getInfo(TermInfoType.TERM_VALUE);
		Object otherValue = o.getInfo(TermInfoType.TERM_VALUE);
		if(myName == null) {
			return 1;
		}
		if(otherName == null) {
			return -1;
		}
		int comp = myName.compareTo(otherName);
		if(comp == 0) {
			if(myValue == null) {
				return 1;
			}
			if(otherValue == null) {
				return -1;
			}
			return myValue.toString().compareTo(otherValue.toString());
		}
		return comp;
	}

	public TerminologyType getType() {
		return type;
	}
	
}
