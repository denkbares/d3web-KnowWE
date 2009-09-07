package de.d3web.we.action;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import de.d3web.we.basic.Information;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.persistence.SessionPersistenceHandler;

public class LoadSessionAction implements KnowWEAction {


	public String perform(KnowWEParameterMap map) {
		String dir = D3webModule.getSessionPath(map);
		String sessionFileName = map.get(KnowWEAttributes.SESSION_FILE);
		File session = new File(dir, sessionFileName);
		Broker broker = D3webModule.getBroker(map);
		List<Information> infos;
		try {
			infos = SessionPersistenceHandler.getInstance().loadSession(broker, session.toURI().toURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "ERROR: malformed SessionURL";
		}
		broker.clearDPSSession();
		broker.getSession().getBlackboard().setAllInformation(infos);
		broker.processInit();
		return "done";
	}

}
