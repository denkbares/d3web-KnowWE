package de.d3web.we.hermes.kdom;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class TimeEventSourceType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		sectionFinder = new TimeEventSourceSectionFinder();
	}

	public class TimeEventSourceSectionFinder extends SectionFinder {


		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father) {

			String[] lines = text.split("\\r\\n");
			List<SectionFinderResult> list = new ArrayList<SectionFinderResult>();
			int actIndex = 0;
			for (String line : lines) {
				actIndex = text.indexOf(line, actIndex);
				if (line.startsWith("QUELLE:")) {
					list.add(new SectionFinderResult(actIndex, actIndex
							+ line.length()));
				}
			}

			return list;
		}
	}
}
