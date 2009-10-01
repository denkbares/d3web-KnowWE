package de.d3web.we.hermes.kdom;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class TimeEventDateType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		sectionFinder = new TimeEventDateSectionFinder();

	}


	

	public class TimeEventDateSectionFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section father){
			int indexStart = text.indexOf("\n") + 1;
			int indexEnd = text.indexOf("\n", indexStart);
			List<SectionFinderResult> list = new ArrayList<SectionFinderResult>();
			list.add(new SectionFinderResult(0, indexEnd));
			return list;
		}
	}

}
