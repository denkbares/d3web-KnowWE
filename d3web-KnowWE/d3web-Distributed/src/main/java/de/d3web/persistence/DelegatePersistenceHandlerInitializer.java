/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
