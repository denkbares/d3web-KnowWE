/**
 * 
 */
package de.d3web.we.kdom.semanticAnnotation;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.StringSectionFinder;

/**
 * @author kazamatzuri
 *
 */
public class AnnotationPropertyDelimiter extends DefaultAbstractKnowWEObjectType {

    @Override
    public void init() {
    	this.sectionFinder = new StringSectionFinder("::", this);
    }


}
