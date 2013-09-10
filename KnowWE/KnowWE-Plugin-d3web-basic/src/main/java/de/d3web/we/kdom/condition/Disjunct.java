package de.d3web.we.kdom.condition;

import de.d3web.we.kdom.condition.helper.ConjunctSectionFinder;

/**
 * @author Jochen
 * 
 *         Type for a disjunct element in the CompositeCondition
 * 
 *         example: 'a OR b' here 'a' and 'b' are nodes of type disjunct
 * 
 */
public class Disjunct extends NonTerminalCondition implements de.knowwe.core.kdom.ExclusiveType {

	public Disjunct(String[] keys) {
		this.setSectionFinder(ConjunctSectionFinder.createConjunctFinder(keys));
	}
}