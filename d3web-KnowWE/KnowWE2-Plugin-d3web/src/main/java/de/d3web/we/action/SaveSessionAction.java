package de.d3web.we.action;

import java.io.File;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.d3web.we.core.broker.Broker;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.persistence.SessionPersistenceHandler;
import de.d3web.we.utils.KnowWEUtils;

public class SaveSessionAction implements KnowWEAction {
	
	private SimpleDateFormat dateFormat;
	public SaveSessionAction(String name) {
		dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern("yyyyMMddHHmmss");
	}

	public String perform(KnowWEParameterMap parameterMap) {
		String userID = parameterMap.get(KnowWEAttributes.USER);
		//String userID = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.USER, String.class, true);
		
		String dir = KnowWEUtils.getSessionPath(parameterMap);
		File dirFile = new File(dir);
		if(!dirFile.exists()) {
			dirFile.mkdirs();
		}
		String sessionFileName = userID + "_" + dateFormat.format(new Date())+ ".xml";
		File session = new File(dir, sessionFileName);
		Broker broker = D3webModule.getBroker(parameterMap);
		try {
			SessionPersistenceHandler.getInstance().saveSession(broker, session.toURI().toURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "done";
	}

}
