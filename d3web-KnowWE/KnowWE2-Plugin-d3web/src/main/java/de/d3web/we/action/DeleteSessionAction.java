package de.d3web.we.action;

import java.io.File;

import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public class DeleteSessionAction implements KnowWEAction {


	public String perform(KnowWEParameterMap map) {
		String dir = D3webModule.getSessionPath(map);
		String sessionFileName = map.get(KnowWEAttributes.SESSION_FILE);
		File session = new File(dir, sessionFileName);
		if(!session.delete()) {
			session.deleteOnExit();
		}
		return "done";
	}

}
