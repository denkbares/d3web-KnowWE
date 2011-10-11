package de.knowwe.core.kdom.objects;

/**
 * 
 * An abstract class for TermDefinitions that are always acting in the global
 * scope.
 * 
 * @author Jochen
 * @created 17.12.2010
 * @param <TermObject>
 */
public abstract class GlobalTermDefinition<TermObject> extends TermDefinition<TermObject> {

	public GlobalTermDefinition(Class<TermObject> termObjectClass) {
		super(termObjectClass);
		this.setTermScope(Scope.GLOBAL);
		this.setMultiDefMode(MultiDefMode.INACTIVE);
	}

}
