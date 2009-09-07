package de.d3web.we.action;

import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEParameterMap;

@Deprecated
public class ReInitDPSEnvironment implements KnowWEAction {


	public String perform(KnowWEParameterMap parameterMap) {
//		String web = parameterMap.get(KnowWEAttributes.WEB);
		DPSEnvironment dpse = D3webModule.getDPSE(parameterMap);
		dpse.reInitialize();
		//((GlobalTerminologyRenderer) model.getWebApp().getRenderer("KWiki_globalTerminology")).reInitialize(model, web);
		return "done";
	}
	
	

}
