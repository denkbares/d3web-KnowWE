package de.d3web.we.kdom.TiRex;

import java.text.BreakIterator;
import java.util.List;

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class TiRexRestWordSequence extends TiRexChunk {
	
	
	@Override
	protected void init() {
		this.sectionFinder = new TiRexRestWordSectionFinder(this);
	}


	class TiRexRestWordSectionFinder extends SectionFinder {
		public TiRexRestWordSectionFinder(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father,
				KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep,
				IDGenerator idg) {
			BreakIterator splitter = BreakIterator.getWordInstance();
			splitter.setText(tmp.getOriginalText());
			int words = 0;
			int start = splitter.first();
			for (int end = splitter.next(); end != BreakIterator.DONE; start = end, end = splitter
					.next()) {
				words++;
			}
			if(words >= 2) {
				return new AllTextFinder(type).lookForSections(tmp, father, mgn, rep, idg);
			}

			return null;
		}
	}

}
