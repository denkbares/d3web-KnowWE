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

package de.knowwe.core.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.StringFragment;
import de.knowwe.core.kdom.parsing.Section;

public class SectionFinderResult implements Comparable<SectionFinderResult> {

	private int start;
	private int end;
	private Map<String, String> parameterMap = null;

	/**
	 * Creates a new section finder result from the specified start index (inclusively) to the specified end index
	 * (exclusively) with in the parents section text. The specified parameter map is stored "as is" for later usage.
	 *
	 * @param start the start index (inclusively)
	 * @param end   the end index (exclusively)
	 */
	public SectionFinderResult(int start, int end, Map<String, String> parameterMap) {
		this(start, end);
		this.parameterMap = parameterMap;
	}

	/**
	 * Creates a new section finder result from the specified start index (inclusively) to the specified end index
	 * (exclusively) with in the parents section text.
	 *
	 * @param start the start index (inclusively)
	 * @param end   the end index (exclusively)
	 */
	public SectionFinderResult(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public Map<String, String> getParameterMap() {
		return this.parameterMap;
	}

	public static List<SectionFinderResult> singleItemList(SectionFinderResult s) {
		if (s == null) return null;
		List<SectionFinderResult> resultList = new ArrayList<>(1);
		resultList.add(s);
		return resultList;
	}

	public static List<SectionFinderResult> singleItemList(int begin, int end) {
		SectionFinderResult s = new SectionFinderResult(begin, end);
		List<SectionFinderResult> resultList = new ArrayList<>();
		resultList.add(s);
		return resultList;
	}

	public static List<SectionFinderResult> resultList(List<StringFragment> fragments) {
		List<SectionFinderResult> result = new ArrayList<>();
		for (StringFragment stringFragment : fragments) {
			result.add(new SectionFinderResult(stringFragment.getStartTrimmed(),
					stringFragment.getEndTrimmed()));
		}
		return result;
	}

	public String getFoundText(Section<?> fatherSection) {
		return fatherSection.getText().substring(start, end);
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

	@Override
	public int compareTo(@NotNull SectionFinderResult o) {
		return Integer.compare(this.getStart(), o.getStart());
	}

	@Override
	public String toString() {
		return "[" + getStart() + ", " + getEnd() + "]";
	}
}