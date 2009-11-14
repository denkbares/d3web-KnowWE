package de.d3web.we.kdom.condition;

import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.QuotedType;
import de.d3web.we.kdom.basic.SquareBracedType;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;

public class CondKnownType extends DefaultAbstractKnowWEObjectType{
	
	public void init() {
		this.sectionFinder = new RegexSectionFinder("KNOWN\\[[^\\]]*]");
		this.childrenTypes.add(new CondKnownKey());
		this.childrenTypes.add(new SquareBracedType(new QuotedQuestion()));
	}
	
	@Deprecated
	class CondKnownFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	class CondKnownKey extends DefaultAbstractKnowWEObjectType {
		public void init() {
			this.sectionFinder = new RegexSectionFinder("KNOWN");
		}
	}

}
