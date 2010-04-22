package de.d3web.we.kdom.sectionFinder;

import java.util.List;

import de.d3web.we.kdom.Section;

public class FirstOccurrenceOfFinder extends AbstractSingleResultFinder {

	private SectionFinder finder = null;

	public FirstOccurrenceOfFinder(String regex) {
		finder = new RegexSectionFinder(regex);
	}

	public FirstOccurrenceOfFinder(RegexSectionFinder f) {
		this.finder = f;
	}

	public FirstOccurrenceOfFinder(StringSectionFinder f) {
		this.finder = f;
	}

	@Override
	public SectionFinderResult lookForSection(String text, Section father) {
		List<SectionFinderResult> secs = finder.lookForSections(text, father);
		if (secs.size() >= 1) return secs.get(0);
		return null;
	}

}
