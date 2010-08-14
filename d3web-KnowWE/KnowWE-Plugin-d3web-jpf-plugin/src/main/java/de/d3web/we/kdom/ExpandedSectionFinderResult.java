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

package de.d3web.we.kdom;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class ExpandedSectionFinderResult extends SectionFinderResult {

	private final KnowWEObjectType type;

	private final String text;

	List<ExpandedSectionFinderResult> children = new ArrayList<ExpandedSectionFinderResult>();

	public ExpandedSectionFinderResult(String text, KnowWEObjectType type, int start) {
		super(start, start + text.length());
		this.text = text;
		this.type = type;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public String getText() {
		return this.text;
	}

	public List<ExpandedSectionFinderResult> getChildren() {
		return children;
	}

	public void addChild(ExpandedSectionFinderResult child) {
		children.add(child);
	}

	public KnowWEObjectType getObjectType() {
		return type;
	}

	@Override
	public boolean excludeFromValidating() {
		return true;
	}

}
