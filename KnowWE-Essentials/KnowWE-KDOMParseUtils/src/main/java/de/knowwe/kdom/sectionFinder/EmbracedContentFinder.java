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

package de.knowwe.kdom.sectionFinder;

import java.util.List;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * Finds stuff embraced (regarding quoted occurrences of the embracing signs)
 *
 * e.g.;
 * somestuff (content-to-find) other  --> using '(' and ')'
 * "some <stuff>" <content-to-find> other --> using '<' and '>'
 *
 *
 *	WANRING! open and close may NOT be equal!
 * @author Jochen
 *
 */
public class EmbracedContentFinder implements SectionFinder {

	private final char open;
	private final char close;
	private int chains = -1;
	private boolean contentOnly = false;

	public EmbracedContentFinder(char open, char close) {
		this.close = close;
		this.open = open;
	}

	public EmbracedContentFinder(char open, char close, boolean contentOnly) {
		this.close = close;
		this.open = open;
		this.contentOnly = contentOnly;
	}

	public EmbracedContentFinder(char open, char close, int chains) {
		this.close = close;
		this.open = open;
		this.chains = chains;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		return lookForSections(text, father, type, 0);
	}

	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type, int firstIndex) {
		int start = Strings.indexOfUnquoted(
				text.substring(firstIndex), String.valueOf(open)) + firstIndex;
		if (start >= firstIndex) {
			int end = Strings.indexOfClosingBracket(text, start,
					open, close);
			if (end < 0) return null;

			int startIndex = start;
			int endIndex = end + 1;
			if (contentOnly) { // cut out open and close char
				startIndex = start + 1;
				endIndex = end;
			}

			// if chains restriction uninitialized, take all
			if (chains == -1) {
				return SectionFinderResult.singleItemList(startIndex,
						endIndex);
			}
			else {
				// else check chain restriction
				String content = text.substring(startIndex, endIndex);
				if (Strings.getCharacterChains(content).length == chains) {
					return SectionFinderResult.singleItemList(startIndex,
							endIndex);
				}
				// if not matches, try to find an other one after these brackets
				else {
					return lookForSections(text, father, type, start + 1);
				}
			}

		}
		return null;
	}

}
