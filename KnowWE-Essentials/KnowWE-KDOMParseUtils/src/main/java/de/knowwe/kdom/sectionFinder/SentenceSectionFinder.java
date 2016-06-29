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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class SentenceSectionFinder implements SectionFinder {

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {

		if (getWordCount(text) < 2) {
			return null;
		}

		ArrayList<SectionFinderResult> result = new ArrayList<>();

		BreakIterator splitter = BreakIterator.getSentenceInstance();
		splitter.setText(text);
		int start = splitter.first();
		for (int end = splitter.next(); end != BreakIterator.DONE; start = end, end = splitter.next()) {
			result.add(new SectionFinderResult(start, end));
		}

		return result;
	}

	private int getWordCount(String text) {
		BreakIterator splitter = BreakIterator.getWordInstance();
		splitter.setText(text);
		int words = 0;
		splitter.first();
		for (int end = splitter.next(); end != BreakIterator.DONE; end = splitter.next()) {
			words++;
		}

		return words;
	}
}
