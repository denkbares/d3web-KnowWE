/**
 * 
 */
package de.d3web.we.kdom.semanticAnnotation;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.SolutionContext;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

/**
 * @author kazamatzuri
 * 
 */
public class AnnotationSubject extends DefaultAbstractKnowWEObjectType {

    /*
     * (non-Javadoc)
     * 
     * @see de.d3web.we.dom.AbstractKnowWEObjectType#init()
     */
    @Override
    protected void init() {
	this.sectionFinder = new AnnotationSubjectSectioner(this);
    }

    class AnnotationSubjectSectioner extends SectionFinder {
	public AnnotationSubjectSectioner(KnowWEObjectType type) {
	    super(type);
	}

	@Override
	public List<Section> lookForSections(Section tmp, Section father,
		KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep,
		IDGenerator idg) {
	    String text = tmp.getOriginalText();
	    if (father.hasRightSonOfType(AnnotationProperty.class, text)) {
		ArrayList<Section> result = new ArrayList<Section>();
		if (tmp.getOriginalText().trim().length()==0)
		    return null;
		Section s = Section.createSection(this.getType(), father, tmp,
			0, tmp.getOriginalText().length(), mgn, rep, idg);
		SolutionContext sol = new SolutionContext();
		sol.setSolution(s.getOriginalText().trim());
		ContextManager.getInstance().attachContext(father, sol);
		result.add(s);
		return result;
	    }
	    return null;

	}
    }
}
