package de.d3web.we.kdom.xcl;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class XCLTail extends DefaultAbstractKnowWEObjectType{

	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return SpecialDelegateRenderer.getInstance();
	}
	
	class XCLTailSectionFinder extends SectionFinder {
		public XCLTailSectionFinder(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father, KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) {
			String text = tmp.getOriginalText();
			List<Section> matches = new ArrayList<Section>();
			
			int end = text.lastIndexOf('}');
			if(text.lastIndexOf('[') > end) {
				matches.add(Section.createSection(this.getType(), father, tmp, end+1, text.length(), kbm, report, idg));
			}
			
			
			return matches;
		}

	}

	@Override
	protected void init() {
		this.sectionFinder = new XCLTailSectionFinder(this);
		
	}

}
