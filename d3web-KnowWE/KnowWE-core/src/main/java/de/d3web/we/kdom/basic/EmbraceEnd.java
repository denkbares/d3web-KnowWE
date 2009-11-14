package de.d3web.we.kdom.basic;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class EmbraceEnd extends DefaultAbstractKnowWEObjectType {
	
	public EmbraceEnd(String end) {
		this.sectionFinder = new EmbraceEndSectionFinder(end);
	}
	
	class EmbraceEndSectionFinder extends SectionFinder {

		private String end;
		
		public EmbraceEndSectionFinder(String end) {
			this.end = end;
		}
		
		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {
			
			List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			if(text.endsWith(end)) {
				result.add(new SectionFinderResult(text.length()-end.length(), text.length() ));
			}
			
			return result;
		}
		
	}

}
