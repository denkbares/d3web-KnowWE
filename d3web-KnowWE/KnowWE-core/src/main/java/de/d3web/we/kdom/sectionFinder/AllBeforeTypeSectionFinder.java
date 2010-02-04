package de.d3web.we.kdom.sectionFinder;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public class AllBeforeTypeSectionFinder extends SectionFinder {
	
	KnowWEObjectType markerType = null;
	
	public AllBeforeTypeSectionFinder(KnowWEObjectType type) {
		this.markerType = type;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		Section s = father.getChildSectionAtPosition(father.getOriginalText().indexOf(text)+text.length() + 1);
		if(s != null && s.getObjectType().getName().equals(markerType.getName())) {
			return AllTextFinderTrimmed.getInstance().lookForSections(text, father);
		}
		
		return null;
	}

}
