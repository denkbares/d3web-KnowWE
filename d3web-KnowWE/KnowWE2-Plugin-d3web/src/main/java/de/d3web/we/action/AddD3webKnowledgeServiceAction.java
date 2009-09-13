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

package de.d3web.we.action;

import java.net.URL;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public abstract class AddD3webKnowledgeServiceAction implements KnowWEAction {


//	public String perform(KnowWEParameterMap map) {
//		
//		String baseID = map.get(KnowWEAttributes.KNOWLEDGEBASE_ID);
//		
//		if(baseID == null) {
//			return "baseID is null";
//		}
//		
//		URL url = KnowWEUtils.getKbUrl(map, baseID);
//		addService(map, base, url);
//		return "done";
//	}

	protected void addService(KnowWEParameterMap map, KnowledgeBase base, URL url)
			throws Exception {
		if(url == null) return;
		DPSEnvironment env = D3webModule.getDPSE(map);
		if(env.getService(base.getId()) != null) {
			map.put(KnowWEAttributes.KNOWLEDGEBASE_ID, base.getId());
			new RemoveD3webKnowledgeServiceAction().perform(map);
		}
		
		String clusterID = map.get(KnowWEAttributes.CLUSTERID);
		KnowledgeService service = new D3webKnowledgeService(base, base.getId(), url);
		env.addService(service, clusterID, true);
		//KnowledgeBaseRepository.getInstance().addKnowledgeBase(base.getId(), base);
		
		for (Broker broker : env.getBrokers()) {
			broker.register(service);
		}
		
		//model.removeAttribute(KnowWEAttributes.KNOWLEDGEBASE, model.getWebApp());
	}

}
