package de.d3web.we.kdom.sectionFinder;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class SentenceSectionFinder extends SectionFinder {
	
	private static SentenceSectionFinder instance = null;

	

	public SentenceSectionFinder(KnowWEObjectType type) {
		super(type);
	}

	@Override
	public List<Section> lookForSections(Section tmpSection, Section father,
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) {

		
		if(getWordCount(tmpSection.getOriginalText()) < 2) {
			return null;
		}
		
		ArrayList<Section> result = new ArrayList<Section>();
		
		BreakIterator splitter = BreakIterator.getSentenceInstance();
		splitter.setText(tmpSection.getOriginalText());
		int start = splitter.first();
		for (int end = splitter.next(); end != BreakIterator.DONE; start = end, end = splitter
				.next()) {
			result.add(Section.createSection(this.getType(), father, tmpSection, start, end, kbm, report, idg));
			
		}

		return result;
	}
	
	private int getWordCount(String text) {
		BreakIterator splitter = BreakIterator.getWordInstance();
		splitter.setText(text);
		int words = 0;
		int start = splitter.first();
		for (int end = splitter.next(); end != BreakIterator.DONE; start = end, end = splitter
				.next()) {
			words++;
		}
		
		return words;
	}
}
