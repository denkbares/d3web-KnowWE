package de.d3web.we.kdom.TiRex;

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

public class TiRexSectionFinder extends SectionFinder {
	
	public TiRexSectionFinder(KnowWEObjectType type) {
		super(type);
	}

	private static final String REGEXP_TIREX = "<TiRex[\\w\\W]*?</TiRex>";
	


	@Override
	public List<Section>lookForSections(Section tmp, Section father, KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep, IDGenerator idg) {
		String text = tmp.getOriginalText();
		List<Section> result = new ArrayList<Section>();
		Pattern p = Pattern.compile(REGEXP_TIREX);
		Matcher m = p.matcher(text);
		while (m.find()) {
			
			result.add(Section.createSection(this.getType(), father, tmp,m.start(),m.end(), mgn, rep, idg));
		}
		return result;
	}
	

}
