package de.d3web.we.kdom.Annotation;

import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class FindingAnswer extends DefaultAbstractKnowWEObjectType {


	class AnnotationKnowledgeSliceObjectAnswerSectionFinder extends SectionFinder {
		public AnnotationKnowledgeSliceObjectAnswerSectionFinder(
				KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father, KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep, IDGenerator idg) {
			String text = tmp.getOriginalText();
			if(father.hasLeftSonOfType(FindingComparator.class, text)) {
				List<Section> foundsections = new AllTextFinder(this.getType()).lookForSections(tmp, father,null,rep, idg);
				
				return foundsections;
			}
			return null;
		}
	}

	public String getAnswer(Section section) {
		return section.getOriginalText().trim();
	}
	
	@Override
	public KnowWEDomRenderer getRenderer() {
		return FontColorRenderer.getRenderer(FontColorRenderer.COLOR5);
	}
	
	
	@Override
	protected void init() {
		this.sectionFinder = new AnnotationKnowledgeSliceObjectAnswerSectionFinder(this);
		
	}
}
