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

import java.util.ArrayList;
import java.util.List;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * This SectionFinder finds the _all_ unquoted occurrence of the 'symbol' in the
 * text and creates a section from it.
 *
 * @author Jochen
 */
public class UnquotedExpressionFinder implements SectionFinder {

	private final String symbol;
	private final int flags;

	public UnquotedExpressionFinder(String symbol, int flags) {
		this.symbol = symbol;
		this.flags = flags;
	}

	public UnquotedExpressionFinder(String symbol) {
		this(symbol, Strings.UNQUOTED);
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

		int index = Strings.indexOf(text, flags, symbol);

		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		int counter = 0;
		// in this loop the text is scanned and cropped for occurrences and the
		// results are created

		while (index != -1) {
			result.add(new SectionFinderResult(
					index + counter, index + counter + symbol.length()));

			text = text.substring(index + 1);
			counter += index + 1;
			index = Strings.indexOf(text, flags, symbol);
		}

		return result;
	}

}
