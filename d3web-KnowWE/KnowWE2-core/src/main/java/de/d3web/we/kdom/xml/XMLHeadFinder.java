package de.d3web.we.kdom.xml;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class XMLHeadFinder extends SectionFinder {


	public XMLHeadFinder(KnowWEObjectType type) {
		super(type);
	}

	@Override
	public List<Section> lookForSections(Section tmpSection, Section father,
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report,
			IDGenerator idg) {

		if (father.getObjectType() instanceof AbstractXMLObjectType) {
			
			String text = ((AbstractXMLObjectType) father.getObjectType())
				.getMapFor(father).get(XMLSectionFinder.HEAD);
			Section s = Section.createSection(this.getType(),
					father, tmpSection, tmpSection.getOriginalText().indexOf(text), text.length(),
					kbm, report, idg, father.getId() + "_head");
			List<Section> result = new ArrayList<Section>();
			result.add(s);
			return result;
			
		}
		
		return null;
	}

}
