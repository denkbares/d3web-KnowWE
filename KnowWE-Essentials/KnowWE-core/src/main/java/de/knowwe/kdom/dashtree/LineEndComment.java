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

package de.knowwe.kdom.dashtree;

import java.util.List;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.renderer.StyleRenderer;

public class LineEndComment extends AbstractType {

	public LineEndComment() {
		this.setSectionFinder(new LineEndCommentFinder());
		setRenderer(StyleRenderer.COMMENT);
	}

	/**
	 * this LineEndCommentFinder assumes that single text lines are given to the
	 * sectionfinder
	 * 
	 * @author Jochen, Albrecht
	 */
	static class LineEndCommentFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, Type type) {

			// looks for an unquoted occurrence of '//' and cuts off from this
			// point
			int start = Strings.indexOfUnquoted(text, "//");
			if (start != -1) {
				String prefix = text.substring(0, start);
				// we also want a white space directly before the // to avoid
				// interpreting links (http://) and so on as comments
				if (prefix.matches(".+?\\s$")) {
					// if found return section from start to the end of the line
					return SectionFinderResult.singleItemList(new SectionFinderResult(start,
							text.length()));
				}
			}
			return null;
		}

	}
}
