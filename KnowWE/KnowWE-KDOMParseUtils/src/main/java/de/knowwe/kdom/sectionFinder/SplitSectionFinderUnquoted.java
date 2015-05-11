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

package de.knowwe.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;

import de.d3web.strings.QuoteSet;
import de.d3web.strings.StringFragment;
import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * Works similar to string.split(key), but does ignore key-signs which are in
 * quotes. The quoted Strings can contain escaped quotes.
 * 
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 16.09.2011
 */
public class SplitSectionFinderUnquoted implements SectionFinder {

	private final String splitKey;
	private final QuoteSet[] quoteSets;

	public SplitSectionFinderUnquoted(String key) {
		this(key, '"');
	}

	public SplitSectionFinderUnquoted(String key, char... quoteChars) {
		this.splitKey = key;
		quoteSets = new QuoteSet[quoteChars.length];
		for (int i = 0; i < quoteChars.length; i++) {
			quoteSets[i] = new QuoteSet(quoteChars[i]);
		}
	}

	public SplitSectionFinderUnquoted(String key, QuoteSet... quoteSets) {
		this.splitKey = key;
		this.quoteSets = quoteSets;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text,
			Section<?> father, Type type) {

		List<SectionFinderResult> result = new ArrayList<>();
		List<StringFragment> list = Strings.splitUnquoted(text, splitKey, false, quoteSets);
		for (StringFragment stringFragment : list) {
			result.add(new SectionFinderResult(stringFragment.getStartTrimmed(),
					stringFragment.getEndTrimmed()));
		}
		return result;
	}

}
