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

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.utils.SplitUtility;

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

	public EmbracedContentFinder(char open, char close) {
		this.close = close;
		this.open = open;
	}

	public EmbracedContentFinder(char open, char close, int chains) {
		this.close = close;
		this.open = open;
		this.chains = chains;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		int start = SplitUtility.indexOfUnquoted(text, "" + open);
		if (start > -1) {
			int end = SplitUtility.findIndexOfClosingBracket(text, start,
						open, close);
			if (end < 0) return null;

			// if chains restriction uninitialized, take all
			if (chains == -1) {
				return SectionFinderResult.createSingleItemResultList(start,
						end + 1);
			}
			else {
				// else check chain restriction
				String content = text.substring(start,
						end + 1);
				if (SplitUtility.getCharacterChains(content).length == chains) {
					return SectionFinderResult.createSingleItemResultList(start,
							end + 1);
				}
			}

		}
		return null;
	}

}
