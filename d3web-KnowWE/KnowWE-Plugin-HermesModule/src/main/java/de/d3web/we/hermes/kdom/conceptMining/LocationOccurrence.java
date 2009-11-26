package de.d3web.we.hermes.kdom.conceptMining;

import org.openrdf.model.URI;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;

public class LocationOccurrence extends DefaultAbstractKnowWEObjectType {
	

		
		@Override
		public void init() {
			this.setCustomRenderer(new LocationOccurrenceRenderer());
			this.sectionFinder = new LocationFinder();
			
			
		}
		
		
	
}

	class LocationOccurrenceRenderer extends ConceptOccurrenceRenderer{

		private static String[]  personProps = new String[] {"takesPlaceAt"};
		
		@Override
		protected String[] getPossibleProperties(URI subject, String object) {
			return personProps;
		}

	}


	class LocationFinder extends ConceptFinder {
		
		private static String [] classes = {"Location"};

		@Override
		protected String[] getClassNames() {
			return classes;
		}

	}


