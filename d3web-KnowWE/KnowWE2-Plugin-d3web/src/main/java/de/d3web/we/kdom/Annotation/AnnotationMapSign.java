package de.d3web.we.kdom.Annotation;

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

public class AnnotationMapSign extends DefaultAbstractKnowWEObjectType {
	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return SpecialDelegateRenderer.getInstance();
	}

	class AnnotationMapSignSectionFinder extends SectionFinder {
		public AnnotationMapSignSectionFinder(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father,
				KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep,
				IDGenerator idg) {
			String text = tmp.getOriginalText();
			int index = text.lastIndexOf("<=>");
			List<Section> result = new ArrayList<Section>();
			result.add(Section.createSection(this.getType(), father, tmp,
					index, index + 3, mgn, rep, idg));
			return result;
		}
	}

	@Override
	protected void init() {
		this.sectionFinder = new AnnotationMapSignSectionFinder(this);
		
	}

}
