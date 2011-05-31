package de.d3web.we.kdom.objects;


/**
 * 
 * An abstract class for TermReferences that are always acting in the global
 * scope.
 * 
 * @author Jochen
 * @created 17.12.2010
 * @param <TermObject>
 */

public abstract class GlobalTermReference<TermObject> extends TermReference<TermObject> {

	public GlobalTermReference(Class<TermObject> termObjectClass) {
		super(termObjectClass);
		this.setTermScope(Scope.GLOBAL);
	}
	
}
