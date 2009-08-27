package de.d3web.persistence.xml.writers.actions;

import de.d3web.kernel.psMethods.delegate.ActionInstantDelegate;

public class ActionInstantDelegateWriter extends AbstractActionDelegateWriter {
	
	public static final Class ID = ActionInstantDelegate.class;

	protected String getType() {
		return "ActionInstantDelegate";
	}

}
