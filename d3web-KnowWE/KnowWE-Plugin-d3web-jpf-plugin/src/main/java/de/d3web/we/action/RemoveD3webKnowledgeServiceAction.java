/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.action;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.basic.WikiEnvironment;
import de.d3web.we.basic.SessionBroker;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;

public class RemoveD3webKnowledgeServiceAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap map) {
		String baseID = map.get(KnowWEAttributes.KNOWLEDGEBASE_ID);
		if (baseID == null) return "no kbid to remove knowledge service";
		WikiEnvironment env = D3webModule.getDPSE(map);
		KnowledgeBase service = env.getService(baseID);
		if (service == null) return "no service found for id: " + baseID;
		env.removeService(service);
		for (SessionBroker broker : env.getBrokers()) {
			Session serviceSession = env.createServiceSession(
					service.getId());
			broker.addServiceSession(service.getId(), serviceSession);
		}
		return "done";
	}

}
