package de.d3web.we.kdom.semanticAnnotation;

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
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class SemanticAnnotation extends DefaultAbstractKnowWEObjectType {

    private static String ANNOTATIONBEGIN = "\\[";
    private static String ANNOTATIONEND = "\\]";

   

    public SemanticAnnotation() {

    }
        
    @Override
    public SectionFinder getSectioner() {
	return new AnnotationSectioner(this);
    }

    class AnnotationSectioner extends SectionFinder {
	private String PATTERN = ANNOTATIONBEGIN
		+ "[\\w\\W]*?"
		+ ANNOTATIONEND;

	public AnnotationSectioner(KnowWEObjectType type) {
	    super(type);
	}

	@Override
	public List<Section> lookForSections(Section tmp, Section father,
		KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep,
		IDGenerator idg) {
	    String text = tmp.getOriginalText();
	    ArrayList<Section> result = new ArrayList<Section>();
	    Pattern p = Pattern.compile(PATTERN);
	    Matcher m = p.matcher(text);
	    while (m.find()) {
		String found=m.group();
		if (found.contains("::"))
		    result.add(Section.createSection(this.getType(), father, tmp, m
			.start(), m.end(), mgn, rep, idg));
	    }
	    return result;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.d3web.we.dom.AbstractKnowWEObjectType#init()
     */
    @Override
    protected void init() {
	this.setCustomRenderer(new StandardAnnotationRenderer());
	this.childrenTypes.add(new AnnotationStartSymbol("["));
	this.childrenTypes.add(new AnnotationEndSymbol("]"));
	this.childrenTypes.add(new AnnotationContent());

    }

}
