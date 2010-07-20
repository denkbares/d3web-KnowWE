package de.d3web.we.kdom.sectionFinder;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.SingleChildConstraint;

public class AllBeforeTypeSectionFinder extends SectionFinder {

	KnowWEObjectType markerType = null;
	
	private AllTextFinderTrimmed allTextFinderTrimmed = new AllTextFinderTrimmed();

	public AllBeforeTypeSectionFinder(KnowWEObjectType type) {
		this.addConstraint(new SingleChildConstraint());
		this.markerType = type;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
		// note this indexOf call is unsafe - wrong matches are caught by SingelChildConstraint
		Section s = father.getChildSectionAtPosition(father.getOriginalText().indexOf(
				text)
				+ text.length());
		if(s != null && s.getObjectType().getName().equals(markerType.getName())) {
			return allTextFinderTrimmed.lookForSections(text, father, type);
		}

		return null;
	}

}
