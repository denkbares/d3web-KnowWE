package de.d3web.we.hermes.kdom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class TimeEventTitleType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		sectionFinder = new TimeEventTitleSectionFinder();
	}

	public class TimeEventTitleSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {
			if (text.length() < 3) {
				return null;
			}
			int indexEnd = text.indexOf("(");
			if (indexEnd == -1) {
				return null;
			}
			List<SectionFinderResult> list = new ArrayList<SectionFinderResult>();
			list.add(new SectionFinderResult(0, indexEnd));
			return list;

		}
	}
}
