package de.d3web.we.kdom.sectionFinder;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.error.KDOMError;

public class StringEnumChecker implements ReviseSubTreeHandler{

	private String [] values;
	private KDOMError error;
	
	public StringEnumChecker(String [] values, KDOMError error ) {
		this.values = values;
		this.error = error;
	}
	
	@Override
	public void reviseSubtree(KnowWEArticle article, Section s) {
		boolean found = false;
		for (String string : values) {
			if(s.getOriginalText().contains(string)) {
				found = true;
			}
		}
		
		if(!found) {
			KDOMError.storeError(s, error);
		}
		
	}

}
