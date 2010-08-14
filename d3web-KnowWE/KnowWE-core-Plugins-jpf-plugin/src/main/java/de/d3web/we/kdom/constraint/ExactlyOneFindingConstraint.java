package de.d3web.we.kdom.constraint;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class ExactlyOneFindingConstraint implements SectionFinderConstraint {

	private static ExactlyOneFindingConstraint instance;

	public static ExactlyOneFindingConstraint getInstance() {
		if (instance == null) instance = new ExactlyOneFindingConstraint();
		return instance;
	}

	@Override
	public void filterCorrectResults(
			List<SectionFinderResult> found, Section father, KnowWEObjectType type) {
		if (found == null || found.size() == 0) return;

		SectionFinderResult firstResult = found.get(0);
		found.clear();
		found.add(firstResult);

	}

	@Override
	public boolean satisfiesConstraint(List<SectionFinderResult> found,
			Section father, KnowWEObjectType type) {
		if (found.size() != 1) {
			return false;
		}
		return true;
	}

}
