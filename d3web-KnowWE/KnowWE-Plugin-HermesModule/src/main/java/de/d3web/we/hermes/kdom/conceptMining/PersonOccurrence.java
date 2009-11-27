package de.d3web.we.hermes.kdom.conceptMining;

import org.openrdf.model.URI;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;

public class PersonOccurrence extends DefaultAbstractKnowWEObjectType {

    @Override
    public void init() {
	this.setCustomRenderer(new PersonOccurrenceRenderer());
	this.sectionFinder = new PersonFinder();
    }
}

class PersonOccurrenceRenderer extends ConceptOccurrenceRenderer {

    private static String[] personProps = new String[] { "involves" };

    @Override
    protected String[] getPossibleProperties(URI subject, String object) {
	return personProps;
    }
}

class PersonFinder extends ConceptFinder {

    private static String[] classes = { "Person" };

    @Override
    protected String[] getClassNames() {
	return classes;
    }
}
