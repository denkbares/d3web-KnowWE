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

package de.d3web.we.kdom.bulletLists;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.CommentRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class BulletCommentType extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.sectionFinder = new BulletCommentFinder();
		this.setCustomRenderer(new CommentRenderer());
	}

	class BulletCommentFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father, KnowWEObjectType type) {
			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			;
			if (text.contains("//")) {

				// be sure the comment is in a quoted region
				int index = text.indexOf("//");
				int quotesBefore = countQuotes(text.substring(0, index));
				int quotesAfter = countQuotes(text.substring(index, text.length()));

				if (!(quotesBefore == 1 && quotesAfter >= 1)) {
					result.add(new SectionFinderResult(index, text.length()));
				}

			}
			return result;
		}

		private int countQuotes(String text) {
			String lineRegex = "\"";
			Pattern linePattern = Pattern.compile(lineRegex);

			Matcher tagMatcher = linePattern.matcher(text);
			int count = 0;
			while (tagMatcher.find()) {
				count++;
			}
			return count;
		}

	}
}
