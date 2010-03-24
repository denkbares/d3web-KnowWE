package de.d3web.we.kdom.sectionFinder;

import java.util.List;

import de.d3web.we.kdom.MultiSectionFinder;
import de.d3web.we.kdom.Section;

public class OneOfStringEnumFinder extends SectionFinder {

	MultiSectionFinder msf;

	public OneOfStringEnumFinder(String[] values) {
		msf = new MultiSectionFinder();
		for (int i = 0; i < values.length; i++) {
			msf.addSectionFinder(new RegexSectionFinder(values[i]));
		}
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
		return msf.lookForSections(text, father);
	}

}
