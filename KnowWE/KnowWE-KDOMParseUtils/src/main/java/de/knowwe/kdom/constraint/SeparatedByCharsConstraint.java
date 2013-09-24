package de.knowwe.kdom.constraint;

import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * A constraint removing all results that are not separated by special
 * characters from the rest of the surrounding text. The start end end of the
 * text is also accepted as separated correctly.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 24.09.2013
 */
public class SeparatedByCharsConstraint extends AbstractFilterConstraint {

	private final String separatingChars;

	public SeparatedByCharsConstraint(String separatingChars) {
		if (separatingChars == null) throw new NullPointerException();
		this.separatingChars = separatingChars;
	}

	@Override
	public boolean accept(String text, SectionFinderResult result) {
		int before = result.getStart() - 1;
		int after = result.getEnd();

		if (before >= 0) {
			if (!isSeparatorChar(text.charAt(before))) {
				return false;
			}
		}

		if (after < text.length()) {
			if (!isSeparatorChar(text.charAt(after))) {
				return false;
			}
		}

		return true;
	}

	private boolean isSeparatorChar(char charAt) {
		return separatingChars.indexOf(charAt) > 0;
	}
}
