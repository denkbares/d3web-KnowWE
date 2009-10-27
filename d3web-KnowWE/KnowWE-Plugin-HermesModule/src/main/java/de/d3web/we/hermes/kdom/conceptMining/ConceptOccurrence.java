package de.d3web.we.hermes.kdom.conceptMining;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;

public class ConceptOccurrence extends DefaultAbstractKnowWEObjectType {
	
	@Override
	public void init() {
		this.setCustomRenderer(new ConceptOccurrenceRenderer());
		this.sectionFinder = new ConceptFinder();
		
		
	}

}
