package de.d3web.we.d3webModule;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.d3web.we.core.DPSEnvironment;

public class DPSEnvironmentManager {
	
	private String webEnvironmentLocation; 
	
//	FL: commented this cBServide out, cause it was never used
//	private Map<String, D3webKnowledgeService> cBServices; 
	
	private static DPSEnvironmentManager instance = new DPSEnvironmentManager();
	
	private DPSEnvironmentManager() {
		super();
		initialize();
	}
	
	public static DPSEnvironmentManager getInstance() {
		return instance;
	}
	
	private Map<String, DPSEnvironment> environments;
	
	
	private void initialize() {
		environments = new HashMap<String, DPSEnvironment>();
//		cBServices = new HashMap<String, D3webKnowledgeService>();
	}
	
//	public DPSEnvironment getEnvironment(String webID, Model model) {
//		DPSEnvironment result = environments.get(webID);
//		if(result == null) {
//			String webDirString = KnowWEUtils.getWebEnvironmentPath(model, webID);
//			File webDirFile =  new File(webDirString);
//			if(!webDirFile.exists()) {
//				webDirFile.mkdirs();
//			}
//			URL webLocation;
//			try {
//				webLocation = new File(webDirString).toURI().toURL();
//				result = new DPSEnvironment(webLocation);
//				environments.put(webID, result);
//			} catch (MalformedURLException e) {
//				Logger.getLogger(getClass().getName()).warning("Error: initialization failed: " + webDirString);
//			}
//		}
//		return result;
//	}
	
	public DPSEnvironment getEnvironment(String webID, String webDirString) {
		DPSEnvironment result = environments.get(webID);
		if(result == null) {
			File webDirFile =  new File(webDirString);
			if(!webDirFile.exists()) {
				webDirFile.mkdirs();
			}
			URL webLocation;
			try {
				webLocation = new File(webDirString).toURI().toURL();
				result = new DPSEnvironment(webLocation);
				environments.put(webID, result);
			} catch (MalformedURLException e) {
				Logger.getLogger(getClass().getName()).warning("Error: initialization failed: " + webDirString);
			}
		}
		return result;
	}
	
	public DPSEnvironment createEnvironment(String webID) {
		DPSEnvironment result = null;
		try {
			URL url = new File(webEnvironmentLocation + webID + File.separatorChar).toURI().toURL();
			result = new DPSEnvironment(url);
			environments.put(webID, result);
		} catch (MalformedURLException e) {
			Logger.getLogger(getClass().getName()).warning("Error: Cannot initialize: " + webEnvironmentLocation + webID + " :\n " + e.getMessage());
		}
		return result;
	}
	
	public Map<String, DPSEnvironment> getEnvironmentMap() {
		return environments;
	}
	
	public Collection<DPSEnvironment> getEnvironments() {
		return environments.values();
	}
	
	public DPSEnvironment getEnvironments(String web) {
		
		/* HOTFIX for lazy initialization without persistence */
		if(web.equals("default_web") && environments.get(web) == null) {
			this.createEnvironment("default_web");
		}
		return environments.get(web);
	}
	
	public void remove(String webID) {
		environments.remove(webID);
	}
	
	public void setWebEnvironmentLocation(String location) {
		webEnvironmentLocation = location;
	}
	
}
