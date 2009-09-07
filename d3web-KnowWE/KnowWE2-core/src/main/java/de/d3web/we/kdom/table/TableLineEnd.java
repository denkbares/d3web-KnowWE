package de.d3web.we.kdom.table;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class TableLineEnd extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		sectionFinder = new TableLineEndFinder(this);
	}

	class TableLineEndFinder extends SectionFinder {

		public TableLineEndFinder(KnowWEObjectType type) {
			super(type);

		}

		@Override
		public List<Section> lookForSections(Section tmpSection,
				Section father, KnowledgeRepresentationManager kbm,
				KnowWEDomParseReport report, IDGenerator idg) {

			String originalText = tmpSection.getOriginalText();
			int index = originalText.lastIndexOf("|");
			if(index == -1) return null;
			String end = originalText.substring(index+1, originalText.length());

			if (end.length() > 0) {
				List<Section> result = new ArrayList<Section>();
				result.add(Section.createSection(this.getType(), father,
						tmpSection, index+1, originalText.length(), kbm, report,
						idg));
				return result;
			}

			return null;
		}

	}
}
