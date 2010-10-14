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

package de.d3web.we.terminology.term;

import java.util.HashMap;
import java.util.Map;

public class Term implements Comparable<Term> {

	private Map<TermInfoType, Object> termInfos;

	public Term() {
		super();
		termInfos = new HashMap<TermInfoType, Object>();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Term)) return false;
		Term other = (Term) o;
		return other.getTermInfos().equals(getTermInfos());
	}

	@Override
	public int hashCode() {
		return termInfos.hashCode();
	}

	public Object getInfo(TermInfoType infoType) {
		return termInfos.get(infoType);
	}

	public void setInfo(TermInfoType infoType, Object object) {
		termInfos.put(infoType, object);
	}

	public Map<TermInfoType, Object> getTermInfos() {
		return termInfos;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (getInfo(TermInfoType.TERM_VALUE) != null) {
			sb.append(getInfo(TermInfoType.TERM_VALUE));
			sb.append(" of ");
		}
		sb.append("Term: ");
		sb.append(getInfo(TermInfoType.TERM_NAME));
		return sb.toString();
	}

	@Override
	public int compareTo(Term o) {
		if (o == null) {
			return -1;
		}
		String myName = (String) getInfo(TermInfoType.TERM_NAME);
		String otherName = (String) o.getInfo(TermInfoType.TERM_NAME);

		Object myValue = getInfo(TermInfoType.TERM_VALUE);
		Object otherValue = o.getInfo(TermInfoType.TERM_VALUE);
		if (myName == null) {
			return 1;
		}
		if (otherName == null) {
			return -1;
		}
		int comp = myName.compareTo(otherName);
		if (comp == 0) {
			if (myValue == null) {
				return 1;
			}
			if (otherValue == null) {
				return -1;
			}
			return myValue.toString().compareTo(otherValue.toString());
		}
		return comp;
	}
}
