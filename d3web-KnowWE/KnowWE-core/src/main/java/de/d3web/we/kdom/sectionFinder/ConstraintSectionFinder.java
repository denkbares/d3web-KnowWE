package de.d3web.we.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.SectionFinderConstraint;

/**
 * @author Jochen
 * 
 *         An abstract SectionFnder class that allows for the use of so called
 *         SectionFinderConstraints This simplifies the implementation of
 *         certain SectionFinders. E.g., if only the first occurrence of some
 *         text-entity should be allocated for a type, then the
 *         'AtMostOneFindingConstraint' can be applied, filtering out additional
 *         matches
 * 
 */
public abstract class ConstraintSectionFinder extends SectionFinder {

	private List<SectionFinderConstraint> constraintList;


	public void addConstraint(SectionFinderConstraint constraint) {
		if (constraintList == null) {
			constraintList = new ArrayList<SectionFinderConstraint>();

		}
		this.constraintList.add(constraint);
	}

	/**
	 * First the lookForSection-task is delegated the actual implementation.
	 * Then The SectionFinderConstraints are filtered
	 */
	@Override
	public List<SectionFinderResult> lookForSections(String text,
			Section<?> father, KnowWEObjectType type) {

		List<SectionFinderResult> result = lookForSectionToBeConstrained(text, father);

		applyConstraints(result, father, null);

		return null;
	}

	/**
	 * Delegates the lookForSection-task to the actual implementation
	 * 
	 * @created 20.07.2010
	 * @param text
	 * @param father
	 * @return 
	 */
	protected abstract List<SectionFinderResult> lookForSectionToBeConstrained(String text,
			Section<?> father);

	private void applyConstraints(List<SectionFinderResult> results, Section<?> father,
			KnowWEObjectType ob) {
		List<SectionFinderConstraint> constraints = ob.getSectioner().getConstraints();

		if (constraints == null)
			return;

		for (SectionFinderConstraint sectionFinderConstraint : constraints) {
			if (!sectionFinderConstraint.satisfiesConstraint(results, father, ob)) {
				sectionFinderConstraint.filterCorrectResults(results, father, ob);
			}
		}

	}

}
