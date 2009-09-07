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

public class AllTextFinder extends SectionFinder {

	public AllTextFinder(KnowWEObjectType type) {
		super(type);
	}

	@Override
	public List<Section> lookForSections(Section tmp, Section father,
			KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep, IDGenerator idg) {
		Matcher textMatcher = Pattern.compile("<[/]?includedFrom[^>]*?>", Pattern.DOTALL).matcher(tmp.getOriginalText());
		List<Section> result = new ArrayList<Section>();
		int start = 0;
		int end = 0;
		boolean found = false;
		while (textMatcher.find()) {
			found = true;
			start = end;
			end = textMatcher.start();
			Section s = Section.createSection(this.getType(), father, tmp, start, end, mgn, rep, idg);
			result.add(s);
		}
		if (!found && tmp.getOriginalText().length() > 0) {
			Section s = Section.createSection(this.getType(), father, tmp, 0, tmp.getOriginalText().length(), mgn, rep, idg);
			result.add(s);
		}
		return result;
	}

}
