package de.d3web.we.kdom.sectionFinder;

import java.util.List;

import de.d3web.we.kdom.MultiSectionFinder;
import de.d3web.we.kdom.Section;

/**
 * This SectionFinder is created with an array of Strings. It looks for unquoted
 * occurrences of these Strings and creates Sections from it.
 * 
 * @author Jochen
 *
 */
public class OneOfStringEnumUnquotedFinder extends SectionFinder {

	private final MultiSectionFinder msf;

	public OneOfStringEnumUnquotedFinder(String[] values) {
		msf = new MultiSectionFinder();
		for (int i = 0; i < values.length; i++) {
			msf.addSectionFinder(new UnquotedExpressionFinder(values[i]));
		}
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		return msf.lookForSections(text, father);
	}
}
