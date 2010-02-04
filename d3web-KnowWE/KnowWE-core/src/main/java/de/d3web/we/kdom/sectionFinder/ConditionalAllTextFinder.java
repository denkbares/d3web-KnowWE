package de.d3web.we.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.Section;

public abstract class ConditionalAllTextFinder extends SectionFinder {

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		if (text.length() > 0) {
			if (condition(text, father)) {
				result.add(new SectionFinderResult(0, text.length()));
			}
		}
		return result;
	}
	
	protected abstract boolean condition(String text, Section father);

}
