package de.d3web.we.kdom.constraint;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class AtLeastOneFindingConstraint implements SectionFinderConstraint {

	@Override
	public void filterCorrectResults(
			List<SectionFinderResult> found, Section father, KnowWEObjectType type) {
		// one cannot do anything

	}

	@Override
	public boolean satisfiesConstraint(List<SectionFinderResult> found,
			Section father, KnowWEObjectType type) {
		if (found.size() >= 1) {
			return true;
		}
		return false;
	}

}
