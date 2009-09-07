package de.d3web.we.kdom.xcl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;


public class XCLRelationWeight extends DefaultAbstractKnowWEObjectType  {
	
	class XCLRelationWeightSectionFinder extends SectionFinder {
		public XCLRelationWeightSectionFinder(KnowWEObjectType type) {
			super(type);
		}
		
		@Override
		public List<Section> lookForSections(Section tmp, Section father,
				de.d3web.we.knowRep.KnowledgeRepresentationManager kbm, KnowWEDomParseReport report, IDGenerator idg) {
			
			List<Section> result = new ArrayList<Section>();		
			Pattern relWeightPattern = Pattern.compile(" *\\[(\\d|--|\\+\\+|\\!)\\]");
			Matcher m = relWeightPattern.matcher(tmp.getOriginalText());
			while (m.find()) {
				result.add(Section.createSection(this.getType(), father, tmp, m.start(), m.end(), kbm, report, idg));
			}
			return result;
		}
	}

	@Override
	protected void init() {
		this.sectionFinder = new XCLRelationWeightSectionFinder(this);
		
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return FontColorRenderer.getRenderer(FontColorRenderer.COLOR6);
	}


}
