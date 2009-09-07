package de.d3web.we.kdom.sectionFinder;

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

public class LineSectionFinder extends SectionFinder {

	public LineSectionFinder(KnowWEObjectType type) {
		super(type);
	}

	@Override
	public List<Section> lookForSections(Section text, Section father,
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) {
		
		String lineRegex = "\\r\\n";
		Pattern linePattern = Pattern.compile( lineRegex);
		
        Matcher tagMatcher = linePattern.matcher( text.getOriginalText() );		
        ArrayList<Section> resultRegex = new ArrayList<Section>();
        int lastStart = 0;
        while (tagMatcher.find()) 
		{
        	
        	resultRegex.add(Section.createSection(this.getType(), father, text, lastStart, tagMatcher.end(), kbm, report, idg));
        	lastStart = tagMatcher.end();
		}
		return resultRegex;
	}
}
