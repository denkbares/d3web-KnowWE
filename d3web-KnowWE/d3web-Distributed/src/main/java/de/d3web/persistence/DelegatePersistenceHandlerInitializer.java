package de.d3web.persistence;

import de.d3web.kernel.psMethods.delegate.PSMethodDelegate;
import de.d3web.persistence.xml.BasicPersistenceHandler;
import de.d3web.persistence.xml.PersistenceManager;
import de.d3web.persistence.xml.loader.KBLoader;
import de.d3web.persistence.xml.loader.rules.DelegateRuleActionPersistenceHandler;
import de.d3web.persistence.xml.loader.rules.InstantDelegateRuleActionPersistenceHandler;
import de.d3web.persistence.xml.writers.actions.ActionDelegateWriter;
import de.d3web.persistence.xml.writers.actions.ActionInstantDelegateWriter;

public class DelegatePersistenceHandlerInitializer {

	public static void initialize(PersistenceManager pm) {
		BasicPersistenceHandler bph = pm.getBasicPersistenceHandler();
		bph.addPSWriter(PSMethodDelegate.class);
		bph.addXMLWriter(ActionDelegateWriter.ID, new ActionDelegateWriter());
		bph.addXMLWriter(ActionInstantDelegateWriter.ID, new ActionInstantDelegateWriter());
		KBLoader loader = bph.getLoader();
		loader.getRuleLoader().addRuleActionHandler(new DelegateRuleActionPersistenceHandler());
		loader.getRuleLoader().addRuleActionHandler(new InstantDelegateRuleActionPersistenceHandler());
	}
	
}
