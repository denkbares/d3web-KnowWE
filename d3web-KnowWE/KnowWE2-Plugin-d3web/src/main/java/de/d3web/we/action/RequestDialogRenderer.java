package de.d3web.we.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import de.d3web.kernel.XPSCase;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public class RequestDialogRenderer implements KnowWEAction {

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
			Map<String, XPSCase> sessionToCaseMap = (Map) parameterMap.getSession().getServletContext().getAttribute("sessionToCaseMap");
			if (sessionToCaseMap == null) {
				sessionToCaseMap = new HashMap<String, XPSCase>();
			}
			HttpSession s = parameterMap.getSession();
			String sID = s.getId();
			sessionToCaseMap.put(sID, d3webKSS.getXpsCase());
			parameterMap.getSession().getServletContext().setAttribute("sessionToCaseMap", sessionToCaseMap);
			

		}
	}

}
