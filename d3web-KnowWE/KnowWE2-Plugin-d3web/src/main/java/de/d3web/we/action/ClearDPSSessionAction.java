package de.d3web.we.action;


import de.d3web.we.core.broker.Broker;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public class ClearDPSSessionAction implements KnowWEAction {


	public String perform(KnowWEParameterMap map) {
		Broker broker = D3webModule.getBroker(map);
		broker.clearDPSSession();
		return "done";
	}

}
