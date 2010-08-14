package de.d3web.we.kdom.sectionFinder;

import java.util.List;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;

public abstract class AbstractSingleResultFinder extends SectionFinder {

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father, KnowWEObjectType type) {
		return SectionFinderResult.createSingleItemList(lookForSection(text, father, type));
	}

	public abstract SectionFinderResult lookForSection(String text, Section father, KnowWEObjectType type);

}
