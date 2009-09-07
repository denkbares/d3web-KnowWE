package de.d3web.we.kdom.semanticAnnotation;

import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class AnnotatedString extends DefaultAbstractKnowWEObjectType {

	


	@Override
	public void init() {
		this.sectionFinder = new AnnotatedStringSectionFinder(this);
	}
	
	class AnnotatedStringSectionFinder extends  SectionFinder {
		public AnnotatedStringSectionFinder(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father, KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep, IDGenerator idg) {
			String text = tmp.getOriginalText();
			if(father.hasRightSonOfType(AnnotationMapSign.class, text)) {
				return new AllTextFinder(this.getType()).lookForSections(tmp, father,null,rep, idg );
			}
			return null;
		}
	}

}
