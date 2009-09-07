package de.d3web.we.kdom.semanticAnnotation;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.StringSectionFinder;

public class AnnotationEndSymbol extends DefaultAbstractKnowWEObjectType {

    private String end;

    public AnnotationEndSymbol(String symbol) {
	super();
	end = symbol;
	this.sectionFinder = (new StringSectionFinder(end, this, true));
    }

    @Override
    protected void init() {
	
    }

}
