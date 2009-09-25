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

import java.net.MalformedURLException;
import java.net.URL;

import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public class AddD3webKBFileAction extends AddD3webKnowledgeServiceAction {


	public String perform(KnowWEParameterMap map){
		String urlString = map.get(KnowWEAttributes.UPLOAD_KB_URL);
		String id = map.get(KnowWEAttributes.UPLOAD_KB_ID);
		if (id == null || urlString == null)
			return "no ID or URL found";
		
		return addService(map, urlString, id);

		//model.removeAttribute(KnowWEAttributes.UPLOAD_KB_URL, model
		//		.getWebApp());
	}

	private String addService(KnowWEParameterMap map, String urlString, String id) {
		URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (url == null)
			return "malformed url";
		DPSEnvironment env = D3webModule.getDPSE(map);
		if (env.getService(id) != null) {
			//model.getWebApp().getAction("KWiki_removeD3webKnowledgeService")
			//		.perform(model);
			map.put(KnowWEAttributes.KNOWLEDGEBASE_ID, id);
			new RemoveD3webKnowledgeServiceAction().perform(map);
		}

		String clusterID = map.get(KnowWEAttributes.CLUSTERID);
		
		KnowledgeService service = new D3webKnowledgeService(url, D3webModule.getKbUrl(map.getWeb(), id),  id);
		env.addService(service, clusterID, true);
		//KnowledgeBaseRepository.getInstance().addKnowledgeBase(id, ((D3webKnowledgeService)service).getBase());

		for (Broker broker : env.getBrokers()) {
			broker.register(service);
		}
		
		return "done";

		
	}

}
