/**
 * 
 */
package de.d3web.we.kdom.semanticAnnotation;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;

/**
 * @author kazamatzuri
 * 
 */
public class SimpleAnnotation extends DefaultAbstractKnowWEObjectType {

	private class SimpleAnnotationFinder extends SectionFinder {
		/**
		 * @param type
		 */
		public SimpleAnnotationFinder(KnowWEObjectType type) {
			super(type);
		}

		@Override
		public List<Section> lookForSections(Section tmp, Section father,
				KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep,
				IDGenerator idg) {
			ArrayList<Section> result = new ArrayList<Section>();
			if (tmp.getOriginalText().trim().length() > 0) {
				Section s = Section.createSection(this.getType(), father, tmp,
						0, tmp.getOriginalText().length(), mgn, rep, idg);

				result.add(s);
			}
			return result;
		}
	}

	@Override
	public void init() {
		this.sectionFinder = new SimpleAnnotationFinder(this);
	}

	@Override
	public IntermediateOwlObject getOwl(Section s) {
		IntermediateOwlObject io = new IntermediateOwlObject();
		UpperOntology2 uo = UpperOntology2.getInstance();
		String annos = s.getOriginalText().trim().replaceAll(" ", "_");
		URI anno = null;
		if (annos.contains(":")) {
			String[] list = annos.split(":");
			String ns =list[0];
			if (ns.equals("ns")){
				ns=uo.getBaseNS();
			}
			anno = uo.createURI(ns, list[1]);
		} else {
			anno = uo.createlocalURI(annos);
		}
		if (anno != null) {
			io.addLiteral(anno);
		}
		return io;
	}

}
