package de.d3web.we.kdom.semanticAnnotation;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.StringSectionFinder;

public class AnnotationStartSymbol extends DefaultAbstractKnowWEObjectType {

    private String begin;
    
    	public AnnotationStartSymbol(String symbol){
    	    super();
    	    begin = symbol;
    	    this.sectionFinder = new StringSectionFinder(begin, this,true);	
    	}
    	
	@Override
	protected void init() {
				
	}

}
