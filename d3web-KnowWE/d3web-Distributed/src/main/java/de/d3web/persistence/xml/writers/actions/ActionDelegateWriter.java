package de.d3web.persistence.xml.writers.actions;

import de.d3web.kernel.psMethods.delegate.ActionDelegate;

public class ActionDelegateWriter extends AbstractActionDelegateWriter {
	
	public static final Class ID = ActionDelegate.class;

	protected String getType() {
		return "ActionDelegate";
	}

}
