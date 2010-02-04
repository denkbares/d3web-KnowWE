package de.d3web.we.kdom.error;

import java.util.HashSet;
import java.util.Set;

import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public abstract class KDOMError {
	
	public static final String ERROR_STORE_KEY = "ERROR-SET";
	
	public static void storeError(Section s, KDOMError e) {
		Set<KDOMError> errors = (Set<KDOMError>)KnowWEUtils.getStoredObject(s, ERROR_STORE_KEY);
		if(errors == null) {
			errors = new HashSet<KDOMError>();
			KnowWEUtils.storeSectionInfo(s, ERROR_STORE_KEY, errors);
		}
		errors.add(e);
		
		
	}
	
	public static Set<? extends KDOMError> getErrors(Section s) {
		Set<? extends KDOMError> errors = (Set<KDOMError>)KnowWEUtils.getStoredObject(s, ERROR_STORE_KEY);
		return errors;
		
	}
	
	public abstract String getVerbalization(KnowWEUserContext usercontext);

}
