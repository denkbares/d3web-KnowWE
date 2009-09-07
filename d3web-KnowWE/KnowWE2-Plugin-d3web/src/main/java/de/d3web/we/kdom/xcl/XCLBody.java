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

public class XCLBody extends DefaultAbstractKnowWEObjectType{

	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return SpecialDelegateRenderer.getInstance();
	}
	
	@Override
	public void init() {
		
		this.childrenTypes.add(new XCListBodyEndSymbol());
		this.childrenTypes.add(new XCListBodyStartSymbol());
		this.childrenTypes.add(new XCLRelation());
		this.sectionFinder = new XCLBodySectionFinder(this);
	}

	class XCLBodySectionFinder extends SectionFinder{
		public XCLBodySectionFinder(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father,
				KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) {
			String text = tmp.getOriginalText();
			List<Section> matches = new ArrayList<Section>();

			if (text.indexOf('{') >= 0) {
				matches.add(Section.createSection(this.getType(), father, tmp, text.indexOf('{'), text.lastIndexOf('}') + 1, kbm, report, idg));
			}

			return matches;
		}
	}

}
