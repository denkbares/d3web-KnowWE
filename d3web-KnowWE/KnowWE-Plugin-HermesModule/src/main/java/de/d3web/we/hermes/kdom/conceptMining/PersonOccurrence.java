package de.d3web.we.hermes.kdom.conceptMining;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;

public class PersonOccurrence extends DefaultAbstractKnowWEObjectType {

    @Override
    public void init() {
	this.setCustomRenderer(new ConceptOccurrenceRenderer());
	this.sectionFinder = new PersonFinder();
    }
}



class PersonFinder extends ConceptFinder {

    private static String[] classes = { "Person" };

    @Override
    protected String[] getClassNames() {
	return classes;
    }
}
