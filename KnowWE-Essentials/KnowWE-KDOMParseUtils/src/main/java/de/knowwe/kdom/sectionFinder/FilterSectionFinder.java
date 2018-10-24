package de.knowwe.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * A decorating SectionFinder that applies an additional filter function
 * to each match, that is detected by the decorated SectionFinder.
 * This is for instance helpful to post-process regex matches.
 *
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 24.10.18.
 */
public abstract class FilterSectionFinder implements SectionFinder{

	private SectionFinder finder = null;

	public FilterSectionFinder(SectionFinder internalFinder) {
		this.finder = internalFinder;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		List<SectionFinderResult> sectionFinderResults = finder.lookForSections(text, father, type);
		return sectionFinderResults.stream().filter(x->filter(x, father)).collect(Collectors.toList());
	}

	protected abstract boolean filter(SectionFinderResult match, Section<?> father);
}
