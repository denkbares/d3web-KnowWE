package de.knowwe.ontology.kdom.turtle;

import java.util.ArrayList;
import java.util.List;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;


public class FirstWordFinder implements SectionFinder {

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>(1);
		if (text.length() == 0) return result;
		// counts leading spaces
		int index = 0;
		char c = text.charAt(index);
		while (c == ' ') {
			index++;
			if (text.length() >= index) {
				c = text.charAt(index);
			}
			else {
				// String only contains whitespaces.. !?
				break;
			}
		}
		String trimmedText = text.substring(index);

		// return first 'word' of input
		int firstSpace = Strings.indexOfUnquoted(trimmedText, " ");
		result.add(new SectionFinderResult(index, index + firstSpace));
		return result;
	}
}
