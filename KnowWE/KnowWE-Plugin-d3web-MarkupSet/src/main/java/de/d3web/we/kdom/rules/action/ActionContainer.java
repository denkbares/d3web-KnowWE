package de.d3web.we.kdom.rules.action;

import de.d3web.we.kdom.action.SingleAction;
import de.knowwe.core.kdom.AbstractType;

/**
 * Created by Albrecht Striffler (denkbares GmbH) on 02.03.14.
 */
public class ActionContainer extends AbstractType {

	public ActionContainer() {
		addChildType(new SingleAction(new RuleAction()));
	}
}
