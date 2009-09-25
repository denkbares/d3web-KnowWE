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
