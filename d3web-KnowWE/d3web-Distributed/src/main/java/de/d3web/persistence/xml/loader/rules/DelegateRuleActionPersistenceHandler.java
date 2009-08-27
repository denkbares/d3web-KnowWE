package de.d3web.persistence.xml.loader.rules;

import de.d3web.kernel.psMethods.dialogControlling.PSMethodDialogControlling;

public class DelegateRuleActionPersistenceHandler extends AbstractDelegateRuleActionPersistenceHandler {

	public DelegateRuleActionPersistenceHandler() {
		super();
	}
	
	public Class getContext() {
		return PSMethodDialogControlling.class;
	}

	public String getName() {
		return "ActionDelegate";
	}

	
}
