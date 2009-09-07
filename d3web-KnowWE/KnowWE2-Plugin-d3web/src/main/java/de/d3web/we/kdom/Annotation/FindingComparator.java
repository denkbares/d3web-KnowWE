package de.d3web.we.kdom.Annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class FindingComparator extends
DefaultAbstractKnowWEObjectType {
	private HashMap<Section, String> operatorstore;

	public FindingComparator() {
		operatorstore = new HashMap<Section, String>();
	}

	public static final String[] operators = { "<=", ">=", "=", "<", ">" };


//	@Override
//	public KnowWEDomRenderer getRenderer() {
//		return new XCLComparatorEditorRenderer();
//	}

	class AnnotationKnowledgeSliceObjectComparatorSectionFinder extends
			SectionFinder {
		public AnnotationKnowledgeSliceObjectComparatorSectionFinder(
				KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father,
				KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep,
				IDGenerator idg) {
			String text = tmp.getOriginalText();
			int index = -1;
			String foundOperator = "";
			for (String operator : operators) {
				index = text.lastIndexOf(operator);
				if (index != -1) {
					foundOperator = operator;
					break;
				}
			}

			List<Section> result = new ArrayList<Section>();
			if (index != -1) {
				Section createdSection = Section.createSection(this.getType(),
						father, tmp, index, index + foundOperator.length(),
						mgn, rep, idg);
				operatorstore.put(createdSection, foundOperator);
				result.add(createdSection);

			}
			return result;
		}
	}

	public String getComparator(Section section) {
		return section.getOriginalText().trim();
	}

	@Override
	protected void init() {
		this.sectionFinder = new AnnotationKnowledgeSliceObjectComparatorSectionFinder(this);
		
	}

}
