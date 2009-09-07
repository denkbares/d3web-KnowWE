package de.d3web.we.action;

import de.d3web.kernel.XPSCase;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.HTMLDialogRenderer;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public class RefreshHTMLDialogAction implements de.d3web.we.action.KnowWEAction{

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		
		//TODO find better solution
		String namespace = parameterMap.get(KnowWEAttributes.SEMANO_NAMESPACE);
		
		String parts [] = namespace.split("\\.\\.");		
		String topic = parts[0];
		
		
		String user = parameterMap.getUser();
		String web = parameterMap.getWeb();
		
		return callDialogRenderer(topic, user, web);
	}

	public static String callDialogRenderer(String topic, String user, String web) {
		D3webKnowledgeService knowledgeServiceInTopic = D3webModule.getInstance().getAD3webKnowledgeServiceInTopic(web, topic);
		if(knowledgeServiceInTopic == null) return "no KB found";
		String kbid = knowledgeServiceInTopic.getId();
		//String kbid = topic+".."+KnowWEEnvironment.generateDefaultID(topic);
		
		Broker broker = D3webModule.getBroker(user,web);
		broker.activate(broker.getSession().getServiceSession(kbid), null, true,
				false, null);
		broker.getDialogControl().showNextActiveDialog();
		KnowledgeServiceSession serviceSession = broker.getSession()
				.getServiceSession(kbid);
		XPSCase c = null;
		if(serviceSession instanceof D3webKnowledgeServiceSession) {
			c = ((D3webKnowledgeServiceSession)serviceSession).getXpsCase();
			return HTMLDialogRenderer.renderDialog(c,web);
		}
		if(serviceSession == null) {
			kbid =  KnowWEEnvironment.WIKI_FINDINGS+".."+KnowWEEnvironment.generateDefaultID(KnowWEEnvironment.WIKI_FINDINGS);
			 serviceSession = broker.getSession().getServiceSession(kbid);
			 if(serviceSession instanceof D3webKnowledgeServiceSession) {
					c = ((D3webKnowledgeServiceSession)serviceSession).getXpsCase();
					return HTMLDialogRenderer.renderDialog(c,web);
				}
		}
		return null;
	}

}
