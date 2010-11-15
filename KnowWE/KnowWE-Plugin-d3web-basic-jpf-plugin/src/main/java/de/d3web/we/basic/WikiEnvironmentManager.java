/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.basic;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.we.core.KnowWEEnvironment;

public class WikiEnvironmentManager {

	// FL: commented this cBServide out, cause it was never used
	// private Map<String, D3webKnowledgeService> cBServices;

	private static WikiEnvironmentManager instance = new WikiEnvironmentManager();

	private WikiEnvironmentManager() {
		super();
		initialize();
	}

	public static WikiEnvironmentManager getInstance() {
		return instance;
	}

	private Map<String, WikiEnvironment> environments;

	private void initialize() {
		environments = new HashMap<String, WikiEnvironment>();
	}

	public WikiEnvironment getEnvironment(String webID, String webDirString) {
		WikiEnvironment result = environments.get(webID);
		if (result == null) {
			File webDirFile = new File(webDirString);
			if (!webDirFile.exists()) {
				webDirFile.mkdirs();
			}
			result = new WikiEnvironment();
			environments.put(webID, result);
		}
		return result;
	}

	public WikiEnvironment createEnvironment(String webID) {
		WikiEnvironment result = new WikiEnvironment();
		result = new WikiEnvironment();
		environments.put(webID, result);
		return result;
	}

	public Map<String, WikiEnvironment> getEnvironmentMap() {
		return environments;
	}

	public Collection<WikiEnvironment> getEnvironments() {
		return environments.values();
	}

	public WikiEnvironment getEnvironments(String web) {

		/* HOTFIX for lazy initialization without persistence */
		if (web.equals("default_web") && environments.get(web) == null) {
			this.createEnvironment("default_web");
		}
		return environments.get(web);
	}

	public void remove(String webID) {
		environments.remove(webID);
	}

	public static void registerKnowledgeBase(KnowledgeBaseManagement kbm, String topic, String webname) {

		KnowledgeBase base = kbm.getKnowledgeBase();

		base.setId(topic + ".."
				+ KnowWEEnvironment.generateDefaultID(topic));
		WikiEnvironment env = D3webModule.getDPSE(webname);

		env.addService(base);

		for (SessionBroker broker : env.getBrokers()) {
			Session serviceSession = env.createServiceSession(
					base.getId());
			broker.addServiceSession(base.getId(), serviceSession);
		}
	}
}
