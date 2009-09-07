package de.d3web.we.kdom.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class RuleFinder extends SectionFinder {

	public RuleFinder(KnowWEObjectType type) {
		super(type);
	}

	@Override
	public List<Section> lookForSections(Section tmpSection, Section father,
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) {

		ArrayList<Section> result = new ArrayList<Section>();

		Pattern p = Pattern.compile 
			("(IF|WENN).*?(?=(\\s*IF|\\s*WENN|\\s*<[/]?includedFrom[^>]*?>|\\s*\\z))", Pattern.DOTALL);
		Matcher m = p.matcher(tmpSection.getOriginalText());
		
		while(m.find()) {
			Section s = Section.createSection(this.getType(), father, tmpSection, m.start(), m.end(), 
					kbm, report, idg);
			result.add(s);
		}
	
		return result;
	}
}
