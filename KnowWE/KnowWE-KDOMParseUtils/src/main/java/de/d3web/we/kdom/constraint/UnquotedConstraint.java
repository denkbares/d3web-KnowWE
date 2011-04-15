package de.d3web.we.kdom.constraint;

import java.util.Iterator;
import java.util.List;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.Type;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.SplitUtility;

public class UnquotedConstraint implements SectionFinderConstraint {

	private static UnquotedConstraint instance = new UnquotedConstraint();

	public static UnquotedConstraint getInstance() {
		return instance;
	}

	@Override
	public <T extends Type> void filterCorrectResults(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
		Iterator<SectionFinderResult> iter = found.iterator();
		while (iter.hasNext()) {
			SectionFinderResult result = iter.next();
			if (isQuoted(text, result)) { // if finding is in quotes
				iter.remove(); // remove it from list
			}
		}

	}

	@Override
	public <T extends Type> boolean satisfiesConstraint(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {

		for (SectionFinderResult sectionFinderResult : found) {
			boolean quoted = isQuoted(text, sectionFinderResult);
			if (quoted) return false;
		}
		return false;
	}

	private boolean isQuoted(String text, SectionFinderResult sectionFinderResult) {
		return SplitUtility.isQuoted(text,
				sectionFinderResult.getStart());
	}

}
