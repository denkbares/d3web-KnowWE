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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import de.d3web.core.session.Session;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.d3webModule.D3webModule;

public class RequestDialogRenderer extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		
		prepareDialog(parameterMap);
		
		StringBuffer sb = new StringBuffer();
		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"de\">");
		sb.append("<head>");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
		sb.append("<meta http-equiv=\"REFRESH\" content=\"0; url=faces/Controller?knowwe=true&id="+parameterMap.getSession().getId()+"\">");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("</body>");
		sb.append("</html>");
		return sb.toString();
	}

	public void prepareDialog(KnowWEParameterMap parameterMap) {
		String jumpId = parameterMap.get(KnowWEAttributes.JUMP_ID);
		String id = parameterMap.get(KnowWEAttributes.SESSION_ID);
		Broker broker = D3webModule.getBroker(parameterMap);
		broker.activate(broker.getSession().getServiceSession(id), null, true, false, null);
		broker.getDialogControl().showNextActiveDialog();
		KnowledgeServiceSession serviceSession = broker.getSession().getServiceSession(id);
		if(serviceSession instanceof D3webKnowledgeServiceSession) {
			//String web = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.WEB, String.class, true);
			//model.setAttribute(KnowWEAttributes.WEB, web, model.getWebApp());
			String namespace;
			namespace = parameterMap.get(KnowWEAttributes.TARGET);
			if(namespace == null) {
				namespace = parameterMap.get(KnowWEAttributes.NAMESPACE);
			}
			//KnowledgeServiceSession kss = broker.getSession().getServiceSession(id);
			D3webKnowledgeServiceSession d3webKSS = (D3webKnowledgeServiceSession) serviceSession;
			
			// add the case to a map and save it in application scope
			Map<String, Session> sessionToCaseMap = (Map) parameterMap.getSession().getServletContext().getAttribute("sessionToCaseMap");
			if (sessionToCaseMap == null) {
				sessionToCaseMap = new HashMap<String, Session>();
			}
			HttpSession s = parameterMap.getSession();
			String sID = s.getId();
			sessionToCaseMap.put(sID, d3webKSS.getXpsCase());
			parameterMap.getSession().getServletContext().setAttribute("sessionToCaseMap", sessionToCaseMap);
			

		}
	}

}
