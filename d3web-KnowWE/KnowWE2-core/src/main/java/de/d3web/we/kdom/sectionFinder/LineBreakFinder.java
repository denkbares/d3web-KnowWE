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

public class LineBreakFinder extends SectionFinder{

	public LineBreakFinder(KnowWEObjectType type) {
		super(type);
	}

	@Override
	public List<Section> lookForSections(Section text, Section father, KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep, IDGenerator idgen) {
		ArrayList<Section> result = new ArrayList<Section>();
		Matcher m = Pattern.compile(" *[\\r\\n]+").matcher(text.getOriginalText());
		while (m.find()) {
			result.add(Section.createSection(this.getType(), father, text, m.start(), m.end(), mgn, rep, idgen));
		}
		return result;
	}

}
