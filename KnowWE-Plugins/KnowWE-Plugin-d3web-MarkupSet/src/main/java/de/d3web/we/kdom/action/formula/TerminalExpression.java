package de.d3web.we.kdom.action.formula;

import de.d3web.we.kdom.condition.TerminalCondition;

/**
 * 
 * The terminal-type for the CompositeFormula-KDOM, derived from the
 * TerminalCondition
 * 
 * @author Jochen
 * @created 16.10.2010
 */
public class TerminalExpression extends TerminalCondition {


	public TerminalExpression() {
		super("UnrecognizedTerminalExpression", "no valid TerminalExpression");
	}
}
