package de.knowwe.ontology.turtle;

import java.util.ArrayList;
import java.util.List;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class FirstWordFinder implements SectionFinder {

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		List<SectionFinderResult> result = new ArrayList<>(1);
		if (text.isEmpty()) return result;

		String trimmedText = Strings.trim(text);
		int leadingWhiteSpaceChars = text.indexOf(trimmedText);
		int indexOfFirstWhitspace = Strings.indexOfUnquoted(trimmedText, " ", "\t", "\n", "\r");

		// if no whitespace is found, entire text is taken as one 'word'
		if (indexOfFirstWhitspace == Integer.MAX_VALUE) {
			indexOfFirstWhitspace = trimmedText.length();
		}
		// return first 'word' of input
		result.add(new SectionFinderResult(leadingWhiteSpaceChars, leadingWhiteSpaceChars
				+ indexOfFirstWhitspace));
		return result;
	}
}
