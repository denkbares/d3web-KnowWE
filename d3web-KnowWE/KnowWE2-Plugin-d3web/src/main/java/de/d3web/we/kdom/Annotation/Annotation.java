package de.d3web.we.kdom.Annotation;

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
import de.d3web.we.kdom.kopic.renderer.AnnotationInlineAnswerRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.semanticAnnotation.AnnotationEndSymbol;
import de.d3web.we.kdom.semanticAnnotation.AnnotationStartSymbol;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class Annotation extends DefaultAbstractKnowWEObjectType {
    private static String ANNOTATIONBEGIN = "\\{\\{";
    private static String ANNOTATIONEND = "\\}\\}";
     private StandardAnnotationRenderer renderer; 
	public Annotation() {
		renderer= new StandardAnnotationRenderer();
		renderer.addConditionalRenderer(new AnnotationInlineAnswerRenderer());
	}

	   

    @Override
    public KnowWEDomRenderer getDefaultRenderer() {
	return renderer;
    }



	
        @Override
	public void init() {
		this.childrenTypes.add(new AnnotationStartSymbol("{{"));
		this.childrenTypes.add(new AnnotationEndSymbol("}}"));
		this.childrenTypes.add(new AnnotationContent());
		this.sectionFinder = new AnnotationSectioner(this);
	}
    class AnnotationSectioner extends SectionFinder {
	/**
	 * @param type
	 */
	public AnnotationSectioner(KnowWEObjectType type) {
	    super(type);
	    // TODO Auto-generated constructor stub
	}





	private String PATTERN = ANNOTATIONBEGIN + "[\\w\\W]*?" + ANNOTATIONEND;





	@Override
	public List<Section> lookForSections(Section tmp, Section father,
		KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep,
		IDGenerator idg) {
	    String text = tmp.getOriginalText();
	    ArrayList<Section> result = new ArrayList<Section>();
	    Pattern p = Pattern.compile(PATTERN);
	    Matcher m = p.matcher(text);
	    while (m.find()) {
		String found = m.group();
		if (found.contains("::"))
		    result.add(Section.createSection(this.getType(), father,
			    tmp, m.start(), m.end(), mgn, rep, idg));
	    }
	    return result;

	}

    }


}
