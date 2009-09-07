/**
 * 
 */
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

/**
 * @author kazamatzuri
 *
 */
public class AnnotationProperty extends DefaultAbstractKnowWEObjectType {

     @Override
    public void init() {
    	this.sectionFinder = new AnnotationPropertyFinder(this);
    	this.childrenTypes.add(new AnnotationPropertyDelimiter());
    	this.childrenTypes.add(new AnnotationPropertyName());
    }

    
    private class AnnotationPropertyFinder extends SectionFinder{

	private String PATTERN="[\\w]*::";

	/**
	 * @param type
	 */
	public AnnotationPropertyFinder(KnowWEObjectType type) {
	    super(type);
	    // TODO Auto-generated constructor stub
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
			result.add(Section.createSection(this.getType(), father, tmp, m.start(), m.end(), mgn, rep, idg));
		}
		return result;
	}
			
    }
    
   
  
}
