package de.d3web.we.hermes.kdom.conceptMining;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;

public class LocationOccurrence extends DefaultAbstractKnowWEObjectType {

	@Override
	public void init() {
		this.setCustomRenderer(new ConceptOccurrenceRenderer());
		this.sectionFinder = new LocationFinder();

	}

}



class LocationFinder extends ConceptFinder {

	private static String[] classes = { "Location" };

	@Override
	protected String[] getClassNames() {
		return classes;
	}

}
