package de.d3web.we.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.Section;

public class AllTextFinderTrimmed extends SectionFinder {

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();

		String trimmed = text.trim();
		int leadingSpaces = text.indexOf(trimmed);
		int followingSpaces = text.length()
				- (trimmed.length() + leadingSpaces);

		result.add(new SectionFinderResult(leadingSpaces, text.length()
				- followingSpaces));
		return result;
	}

}
