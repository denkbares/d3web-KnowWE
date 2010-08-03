package de.d3web.we.kdom.sectionFinder;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.constraint.ConstraintSectionFinder;
import de.d3web.we.kdom.constraint.SingleChildConstraint;

public class AllBeforeTypeSectionFinder extends SectionFinder {

	KnowWEObjectType markerType = null;
	
	private AllTextFinderTrimmed allTextFinderTrimmed = new AllTextFinderTrimmed();

	private AllBeforeTypeSectionFinder(KnowWEObjectType type) {
		this.markerType = type;
	}

	
	public static ISectionFinder createFinder(KnowWEObjectType type) {
		
			ConstraintSectionFinder f = new ConstraintSectionFinder(new AllBeforeTypeSectionFinder(type));
			f.addConstraint(new SingleChildConstraint());
			return f;
		
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
