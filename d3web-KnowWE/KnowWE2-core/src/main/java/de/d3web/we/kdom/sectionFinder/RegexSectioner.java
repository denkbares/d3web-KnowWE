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

public class RegexSectioner extends SectionFinder{
	private int patternmod;
	private String pattern;
	
	public RegexSectioner(String p, KnowWEObjectType type) {
		super(type);
		this.pattern = p;
		this.patternmod=0;
	}
	
	public RegexSectioner(String p, KnowWEObjectType type, int patternmod) {
		super(type);
		this.pattern = p;
		this.patternmod = patternmod;
	}

	@Override
	public List<Section> lookForSections(Section text, Section father, KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep, IDGenerator idg) {
		ArrayList<Section> result = new ArrayList<Section>();
		
	
		Pattern p = null; 
		if (patternmod != 0) {
		    p= Pattern.compile(pattern, patternmod);
		} else {
		    p = Pattern.compile(pattern);
		}
		
		Matcher m = p.matcher(text.getOriginalText());

		while (m.find()) {
			result.add(Section.createSection(this.getType(), father, text, m.start(), m.end(), mgn, rep, idg));
		}
		return result;
	}
}
