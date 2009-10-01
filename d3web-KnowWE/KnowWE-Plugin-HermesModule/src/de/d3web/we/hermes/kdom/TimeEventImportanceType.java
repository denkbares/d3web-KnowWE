package de.d3web.we.hermes.kdom;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class TimeEventImportanceType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		sectionFinder = new TimeEventTitleSectionFinder();
	}

	// includes "(", ")"
	public class TimeEventTitleSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {
			int indexStart = text.indexOf("(");
			int indexEnd = text.indexOf(")") + 1;

			if (indexStart == -1 || indexEnd == -1) {
				return null;
			}
			List<SectionFinderResult> list = new ArrayList<SectionFinderResult>();
			list.add(new SectionFinderResult(indexStart, indexEnd));
			return list;
		}
	}

}
