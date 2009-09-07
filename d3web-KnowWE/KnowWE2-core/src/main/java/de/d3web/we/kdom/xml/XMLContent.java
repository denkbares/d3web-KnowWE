package de.d3web.we.kdom.xml;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public abstract class XMLContent extends DefaultAbstractKnowWEObjectType {
	
	public XMLContent() {
		sectionFinder = new XMLContentFinder(this);
	}
	
	@Override
	protected abstract void init();
		
	
	class XMLContentFinder extends SectionFinder {

		public XMLContentFinder(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmpSection,
				Section father, KnowledgeRepresentationManager kbm,
				KnowWEDomParseReport report, IDGenerator idg) {
			ArrayList<Section> result = new ArrayList<Section>();
			String id = father.getId()+"_content";
			Section s = Section.createSection(this.getType(), father, tmpSection, 0, tmpSection.getOriginalText().length(), kbm, report, idg, id);
			result.add(s);
			return result;
		}
		
	}

}
