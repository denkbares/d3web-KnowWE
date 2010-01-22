package de.d3web.we.kdom.constraint;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public interface SectionFinderConstraint {
	
	public boolean satisfiesConstraint(List<SectionFinderResult> found, Section father, KnowWEObjectType type);
	
	public void filterCorrectResults(List<SectionFinderResult> found, Section father, KnowWEObjectType type);

}
