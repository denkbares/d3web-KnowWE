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

package de.d3web.we.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SectionFinderResult implements Comparable<SectionFinderResult> {

	protected int start = -1;

	protected int end = -1;

	protected Map<String, String> parameterMap = null;

	public static final String ATTRIBUTE_MAP_STORE_KEY = "attributeMap";

	public SectionFinderResult(int start, int end, Map<String, String> parameterMap) {
		this(start, end);
		this.parameterMap = parameterMap;
	}

	public SectionFinderResult(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public Map<String, String> getParameterMap() {
		return this.parameterMap;
	}

	public static List<SectionFinderResult> createSingleItemList(SectionFinderResult s) {
		if (s == null) return null;
		List<SectionFinderResult> resultList = new ArrayList<SectionFinderResult>();
		resultList.add(s);
		return resultList;
	}

	public static List<SectionFinderResult> createSingleItemResultList(int begin, int end) {
		SectionFinderResult s = new SectionFinderResult(begin, end);
		List<SectionFinderResult> resultList = new ArrayList<SectionFinderResult>();
		resultList.add(s);
		return resultList;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * If you overwrite this method to return true for your own
	 * SectionFinderResult, the result will not be validated in the Sectionizer.
	 * Use with care and only if you know what you are doing!
	 * 
	 * @created 26.07.2010
	 * @return
	 */
	public boolean excludeFromValidating() {
		return false;
	}

	@Override
	public int compareTo(SectionFinderResult o) {
		return Integer.valueOf(this.getStart())
				.compareTo(Integer.valueOf(o.getStart()));
	}

	@Override
	public String toString() {
		return "[" + getStart() + ", " + getEnd() + "]";
	}

}