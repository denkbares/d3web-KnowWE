package de.d3web.we.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

/**
 * @author Jochen
 * 
 *         This SectionFinder takes all the text given, but performing a trim()
 *         operation cutting off whitespace characters
 * 
 */
public class AllTextFinderTrimmed extends SectionFinder {

	public AllTextFinderTrimmed() {
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();

		String trimmed = text.trim();
		if (trimmed.length() == 0) return result;
		int leadingSpaces = text.indexOf(trimmed);
		int followingSpaces = text.length()
				- (trimmed.length() + leadingSpaces);

		result.add(new SectionFinderResult(leadingSpaces, text.length()
				- followingSpaces));
		return result;
	}

}
