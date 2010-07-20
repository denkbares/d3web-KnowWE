package de.d3web.we.kdom.basic;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class EmbraceStart extends DefaultAbstractKnowWEObjectType{
	
	public EmbraceStart(String start) {
		this.sectionFinder = new EmbraceStartSectionFinder(start);
	}
	
	
	class EmbraceStartSectionFinder extends SectionFinder {

		private String start;
		
		public EmbraceStartSectionFinder(String start) {
			this.start = start;
		}
		
		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father, KnowWEObjectType type) {
			
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			if(text.startsWith(start)) {
				result.add(new SectionFinderResult(0, start.length() ));
			}
			
			return result;
		}
		
	}

}
