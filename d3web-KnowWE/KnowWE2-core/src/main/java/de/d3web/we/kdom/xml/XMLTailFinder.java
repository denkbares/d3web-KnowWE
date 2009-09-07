package de.d3web.we.kdom.xml;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class XMLTailFinder extends SectionFinder {


	public XMLTailFinder(KnowWEObjectType type) {
		super(type);
	}

	@Override
	public List<Section> lookForSections(Section tmpSection, Section father,
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report,
			IDGenerator idg) {

		if (father.getObjectType() instanceof AbstractXMLObjectType) {
			
			String text = ((AbstractXMLObjectType) father.getObjectType())
					.getMapFor(father).get(XMLSectionFinder.TAIL);
			int start = tmpSection.getOriginalText().lastIndexOf(text);
			Section s = Section.createSection(this.getType(),
					father, tmpSection, start, start + text.length(),
					kbm, report, idg, father.getId() + "_tail");
			List<Section> result = new ArrayList<Section>();
			result.add(s);
			return result;
			
		}
		
		return null;
	}
	
}
