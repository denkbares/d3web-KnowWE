package de.d3web.we.kdom.Annotation;

import java.util.List;

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class FindingSectionFinder extends SectionFinder {

	public FindingSectionFinder(KnowWEObjectType type) {
		super(type);
	}

	@Override
	public List<Section> lookForSections(Section tmpSection, Section father,
			KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep, IDGenerator idg) {
		if(tmpSection.getOriginalText().contains("=")) {
			return new AllTextFinder(super.type).lookForSections(tmpSection, father, mgn, rep, idg);
		}
		return null;
	}

}
