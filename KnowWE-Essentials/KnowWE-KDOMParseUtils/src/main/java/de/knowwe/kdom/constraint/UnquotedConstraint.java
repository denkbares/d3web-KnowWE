package de.knowwe.kdom.constraint;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public class UnquotedConstraint extends AbstractFilterConstraint {

	private static UnquotedConstraint instance = new UnquotedConstraint();

	public static UnquotedConstraint getInstance() {
		return instance;
	}

	private UnquotedConstraint() {
	}

	@Override
	public boolean accept(String text, SectionFinderResult result) {
		return !Strings.isQuoted(text, result.getStart());
	}

}
