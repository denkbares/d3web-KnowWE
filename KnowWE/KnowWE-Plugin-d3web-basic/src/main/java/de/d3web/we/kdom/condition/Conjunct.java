package de.d3web.we.kdom.condition;

import de.d3web.we.kdom.condition.helper.ConjunctSectionFinder;

/**
 * @author Jochen
 * 
 *         Type for a conjunct element in the CompositeCondition
 * 
 *         example: 'a AND b' here 'a' and 'b' are nodes of type conjunct
 * 
 */
public class Conjunct extends NonTerminalCondition implements de.knowwe.core.kdom.ExclusiveType {

	public Conjunct(String[] keys) {
		this.setSectionFinder(ConjunctSectionFinder.createConjunctFinder(keys));
	}
}