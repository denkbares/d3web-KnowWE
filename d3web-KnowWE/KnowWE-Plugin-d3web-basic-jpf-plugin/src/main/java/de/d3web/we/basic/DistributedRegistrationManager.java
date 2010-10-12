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

package de.d3web.we.basic;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;

public class DistributedRegistrationManager {

	private static DistributedRegistrationManager instance = null;

	public static DistributedRegistrationManager getInstance() {
		if (instance == null) {
			instance = new DistributedRegistrationManager();

		}

		return instance;
	}

	public void registerKnowledgeBase(KnowledgeBaseManagement kbm, String topic, String webname) {

		KnowledgeBase base = kbm.getKnowledgeBase();

		base.setId(topic + ".."
				+ KnowWEEnvironment.generateDefaultID(topic));
		DPSEnvironment env = D3webModule.getDPSE(webname);
		KnowledgeService service = new D3webKnowledgeService(base,
				base.getId());

		env.addService(service);

		for (Broker broker : env.getBrokers()) {
			broker.register(service);
		}
	}

}
