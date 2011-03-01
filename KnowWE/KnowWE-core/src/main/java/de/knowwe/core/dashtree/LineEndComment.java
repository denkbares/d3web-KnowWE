/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.knowwe.core.dashtree;

import java.util.List;

import de.d3web.we.kdom.AbstractType;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.StyleRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.SplitUtility;

public class LineEndComment extends AbstractType {

	@Override
	protected void init() {
		this.setSectionFinder(new LineEndCommentFinder());
		setCustomRenderer(StyleRenderer.COMMENT);
	}

	/**
	 * @author Jochen
	 * 
	 *         this LineEndCommentFinder assumes that single text lines are
	 *         given to the sectionfinder
	 * 
	 */
	static class LineEndCommentFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father, Type type) {

			// looks for an unquoted occurrence of '//' and cuts off from this
			// point
			int start = SplitUtility.indexOfUnquoted(text, "//");
			if (start != -1) {
				// if found return section from start to the end of the line
				return SectionFinderResult
						.createSingleItemList(new SectionFinderResult(start,
								text.length()));
			}
			return null;
		}

	}
}
