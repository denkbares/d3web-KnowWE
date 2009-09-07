package de.d3web.we.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class StringSectionFinder extends SectionFinder{
	
	private String string;
	private boolean last = false;
	
	public StringSectionFinder(String s, KnowWEObjectType type ) {
		super(type);
		this.string = s;
	}
	
	public StringSectionFinder(String s, KnowWEObjectType type, boolean last) {
		super(type);
		this.string = s;
		this.last = last;
	}
	
	@Override
	public List<Section> lookForSections(Section tmp, Section father, KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep, IDGenerator idg) {
		int index = tmp.getOriginalText().indexOf(string); 
		if(last) index = tmp.getOriginalText().lastIndexOf(string);
		
		if(index == -1) return null;
		List<Section> result = new ArrayList<Section>();
		//return result;
		result.add(Section.createSection(this.getType(), father, tmp, index, index + string.length(), mgn, rep, idg));		
		return result;
	}

}
