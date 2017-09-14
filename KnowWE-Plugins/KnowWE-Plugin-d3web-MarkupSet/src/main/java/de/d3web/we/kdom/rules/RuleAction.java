package de.d3web.we.kdom.rules;

import de.d3web.core.inference.PSAction;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 14.09.17.
 */
public class RuleAction {
	final PSAction action;
	final Class psContext;

	public RuleAction(PSAction action, Class psContext) {
		this.action = action;
		this.psContext = psContext;
	}
}
