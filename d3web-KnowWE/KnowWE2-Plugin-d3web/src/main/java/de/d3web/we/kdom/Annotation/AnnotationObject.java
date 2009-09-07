/**
 * 
 */
package de.d3web.we.kdom.Annotation;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;
import de.d3web.we.kdom.semanticAnnotation.AnnotationProperty;
import de.d3web.we.kdom.semanticAnnotation.SimpleAnnotation;

/**
 * @author kazamatzuri
 *
 */
public class AnnotationObject extends DefaultAbstractKnowWEObjectType {

    @Override
    public void init() {
    	this.childrenTypes.add(new AnnotationProperty());
    	this.childrenTypes.add(new Finding());
    	this.childrenTypes.add(new SimpleAnnotation());
    	this.sectionFinder = new AllTextFinder(this);
    }
    
	@Override
	public String getName() {
		return this.getClass().getName();
	}
	
}
