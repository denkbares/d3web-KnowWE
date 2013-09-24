package de.knowwe.kdom.constraint;

import java.util.Iterator;
import java.util.List;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * A constraint removing all results that are not matching a filter function.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 24.09.2013
 */
public abstract class AbstractFilterConstraint implements SectionFinderConstraint {

	@Override
	public final <T extends Type> void filterCorrectResults(List<SectionFinderResult> found, Section<?> father, Class<T> type, String text) {
		if (found == null || found.isEmpty()) return;
		Iterator<SectionFinderResult> iter = found.iterator();
		while (iter.hasNext()) {
			SectionFinderResult result = iter.next();
			if (!accept(text, result)) {
				iter.remove();
			}
		}
	}

	public abstract boolean accept(String text, SectionFinderResult result);
}
