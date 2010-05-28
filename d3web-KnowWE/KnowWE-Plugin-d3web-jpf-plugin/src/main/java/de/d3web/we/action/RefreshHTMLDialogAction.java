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

import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import de.d3web.core.session.Session;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.HTMLDialogRenderer;

public class RefreshHTMLDialogAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		
		//TODO find better solution
		String namespace = parameterMap.get(KnowWEAttributes.SEMANO_NAMESPACE);
		
		String parts [] = namespace.split("\\.\\.");		
		String topic = parts[0];
		
		
		String user = parameterMap.getUser();
		HttpServletRequest request = parameterMap.getRequest();
		String web = parameterMap.getWeb();
		
		return callDialogRenderer(topic, user, request, web);
	}

	public static String callDialogRenderer(String topic, String user, HttpServletRequest request, String web) {
		
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(request);
		
		D3webKnowledgeService knowledgeServiceInTopic = D3webModule.getAD3webKnowledgeServiceInTopic(web, topic);
		if(knowledgeServiceInTopic == null) return rb.getString("KnowWE.DialogPane.error");
		String kbid = knowledgeServiceInTopic.getId();
		//String kbid = topic+".."+KnowWEEnvironment.generateDefaultID(topic);
		
		Broker broker = D3webModule.getBroker(user,web);
		broker.activate(broker.getSession().getServiceSession(kbid), null, true,
				false, null);
		broker.getDialogControl().showNextActiveDialog();
		KnowledgeServiceSession serviceSession = broker.getSession()
				.getServiceSession(kbid);
		Session session = null;
		
		if(serviceSession instanceof D3webKnowledgeServiceSession) {
			session = ((D3webKnowledgeServiceSession)serviceSession).getSession();
			return HTMLDialogRenderer.renderDialog(session,web);
		}
		
		if(serviceSession == null) {
			kbid =  KnowWEEnvironment.WIKI_FINDINGS+".."+KnowWEEnvironment.generateDefaultID(KnowWEEnvironment.WIKI_FINDINGS);
			 serviceSession = broker.getSession().getServiceSession(kbid);
			 if(serviceSession instanceof D3webKnowledgeServiceSession) {
					session = ((D3webKnowledgeServiceSession)serviceSession).getSession();
					return HTMLDialogRenderer.renderDialog(session,web);
				}
		}
		
		return null;
	}

}
