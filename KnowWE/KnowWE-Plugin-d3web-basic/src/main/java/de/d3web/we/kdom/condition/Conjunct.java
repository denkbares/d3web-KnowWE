package de.d3web.we.kdom.condition;

import de.d3web.we.kdom.condition.helper.ConjunctSectionFinder;

/**
 * Type for a conjunct element in the CompositeCondition
 * <p>
 * example: 'a AND b' here 'a' and 'b' are nodes of type conjunct
 *
 * @author Jochen
 */
public class Conjunct extends NonTerminalCondition implements de.knowwe.core.kdom.ExclusiveType {

	public Conjunct(String[] keys) {
		this.setSectionFinder(ConjunctSectionFinder.createConjunctFinder(keys));
	}
}