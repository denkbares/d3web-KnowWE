package de.d3web.we.kdom.rules;

import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.sectionFinder.OneOfStringFinder;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 03.03.2014
 */
public class RuleTokenFinder implements SectionFinder {
	private final ConstraintSectionFinder finder;

	public RuleTokenFinder(String... tokens) {
		finder = new ConstraintSectionFinder(new OneOfStringFinder(tokens), AtMostOneFindingConstraint.getInstance());
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		return finder.lookForSections(text, father, type);
	}
}
