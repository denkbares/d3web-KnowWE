package de.d3web.we.kdom.sectionFinder;

import de.d3web.we.kdom.Section;
import de.d3web.we.utils.SplitUtility;

/**
 * Simple SectionFinder that identifies the first (or last if flagged)
 * occurrence of a specific String that is _unquoted_ in the text, i.e., there
 * is not an odd number of quotes '"' before it {@link SplitUtility}
 *
 *
 * @author Jochen
 * 
 */
public class StringSectionFinderUnquoted extends AbstractSingleResultFinder {

	private final String string;
	private boolean last = false;

	public StringSectionFinderUnquoted(String s) {
		this.string = s;
	}

	public StringSectionFinderUnquoted(String s, boolean last) {
		this.string = s;
		this.last = last;
	}

	@Override
	public SectionFinderResult lookForSection(String text, Section father) {

		int index;

		if (last) {
			index = SplitUtility.lastIndexOfUnquoted(text, string);
		}
		else {
			index = SplitUtility.indexOfUnquoted(text, string);
		}

		if (index == -1)
			return null;

		return new SectionFinderResult(index, index + string.length());
	}
}
