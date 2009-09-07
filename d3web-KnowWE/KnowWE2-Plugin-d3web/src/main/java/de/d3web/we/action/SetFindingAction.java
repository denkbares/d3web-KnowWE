package de.d3web.we.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.d3webModule.DPSEnvironmentManager;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;


public class SetFindingAction implements KnowWEAction {

//	public void perform(Model model) throws Exception {
//		Broker broker = KnowWEUtils.getBroker(model);
//		String namespace = java.net.URLDecoder.decode((String) BasicUtils.getModelAttribute(model, KnowWEAttributes.SEMANO_NAMESPACE, String.class, true));
//		String objectid = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.SEMANO_OBJECT_ID, String.class, true);
//		String valueid = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.SEMANO_VALUE_ID, String.class, true);
//		String valuenum = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.SEMANO_VALUE_NUM, String.class, true);
//		String valueids = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.SEMANO_VALUE_IDS, String.class, true);
//		if(namespace == null || objectid == null) {
//			return;
//		}
//		Map<String,String> m = new HashMap<String,String>();
//		m.put(KnowWEAttributes.SEMANO_NAMESPACE, namespace);
//		m.put(KnowWEAttributes.SEMANO_OBJECT_ID, objectid);
//		m.put(KnowWEAttributes.SEMANO_VALUE_ID, valueid);
//		m.put(KnowWEAttributes.SEMANO_VALUE_NUM, valuenum);
//		m.put(KnowWEAttributes.SEMANO_VALUE_IDS, valueids);
//		perform(m);
//		
//	}

	@SuppressWarnings("deprecation")
	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String namespace = java.net.URLDecoder.decode(parameterMap.get(KnowWEAttributes.SEMANO_NAMESPACE));
		String objectid = parameterMap.get(KnowWEAttributes.SEMANO_OBJECT_ID);
		String valueid = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_ID);
		String valuenum = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_NUM);
		String valueids = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_IDS);
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);
		
		DPSEnvironment env = DPSEnvironmentManager.getInstance().getEnvironments(web);
		Broker broker = env.getBroker(user);
		
		if(namespace == null || objectid == null) {
			return "null";
		}
		Object value = null;
		List<Object> values = new ArrayList<Object>();
		if(valueid != null) {
			value = valueid;
		} else if(valuenum != null) {
			value = Double.valueOf(valuenum);
		}
		if(value != null) {
			values.add(value);
		}
		if(valueids != null) {
			String[] ids = valueids.split("\\,");
			for (String string : ids) {
				values.add(string.trim());
			}
		}
		
		KnowledgeServiceSession kss = broker.getSession().getServiceSession(namespace);
		
		Information info = new Information(namespace, objectid, values, TerminologyType.symptom, InformationType.OriginalUserInformation);
		kss.inform(info);
		broker.update(info);
		
		return "value set";
	}

}
