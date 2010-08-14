package de.d3web.we.kdom.sectionFinder;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

/**
 * @author Jochen
 * 
 * 
 *         The normal trim() operation of Strings also cuts of line breaks. This
 *         SectionFinder only cuts off real space characters
 * 
 * 
 */
public class AllTextFinderTrimSpaces extends SectionFinder {

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {

		int leadingSpaces = 0;
		while (text.charAt(leadingSpaces) == ' ') {
			leadingSpaces++;
		}

		int postSpacesIndex = text.length() - 1;
		while (text.charAt(postSpacesIndex) == ' ') {
			postSpacesIndex--;
		}

		// dont take just line breaks
		if (text.substring(leadingSpaces,
				postSpacesIndex + 1).matches("\\r?\\n")) return null;

		return SectionFinderResult.createSingleItemList(new SectionFinderResult(
				leadingSpaces,
				postSpacesIndex + 1));

	}

}
