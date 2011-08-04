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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.knowRep.KnowledgeRepresentationHandler;
import de.d3web.we.user.UserContext;
import de.d3web.we.utils.KnowWEUtils;

public class D3webModule {

	private static final String defaultJarsPath = "/var/lib/tomcat-6/webapps/JSPWiki/KnowWEExtension/KBrepository/";
	private static final String ontfile = "d3web.owl";

	public static ResourceBundle getKwikiBundle_d3web() {

		return ResourceBundle.getBundle("KnowWE_plugin_d3web_messages");
	}

	public static ResourceBundle getKwikiBundle_d3web(UserContext user) {
		if (user == null) return getKwikiBundle_d3web();

		Locale.setDefault(KnowWEEnvironment.getInstance().getWikiConnector()
				.getLocale(user.getRequest()));
		return getKwikiBundle_d3web();
	}

	public static ResourceBundle getKwikiBundle_d3web(HttpServletRequest request) {
		if (request == null) return getKwikiBundle_d3web();

		Locale.setDefault(KnowWEEnvironment.getInstance().getWikiConnector()
				.getLocale(request));
		return getKwikiBundle_d3web();
	}

	public static void initModule(ServletContext context) {



		// Introduce my ontology parts to the core
		// Ontology parts currently not part of d3web-basic
		
		/*
		ISemanticCore sc = SemanticCoreDelegator.getInstance();
		if (sc != null) {
			sc.getUpper().loadOwlFile(
					new File(KnowWEEnvironment.getInstance()
							.getKnowWEExtensionPath()
							+ File.separatorChar + ontfile));
			loadData(context);
		}
		*/
	}

	/**
	 * On KnowWE initialisation> Loads the knowledgebases into the distributed
	 * reasoning engine.
	 *
	 * @param context
	 */
	private static void loadData(ServletContext context) {
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
		String webPath = rb.getString("knowwe.config.path.webs");
		webPath = getRealPath(context, webPath);
		File path = new File(webPath);
		if (!path.exists()) {
			try {
				System.err.println("trying to create kb dir:"
						+ path.getAbsolutePath());
				File dweb = new File(path + "/default_web");
				dweb.mkdirs();
			}
			catch (SecurityException e) {
				System.err
						.println("KB directory creation failed, check permissions!! path:"
								+ path.getAbsolutePath());
				e.printStackTrace();
				// System.exit(1);
			}
		}
		File[] files = path.listFiles();
		if (files != null) {
			for (File each : files) {
				if (each.isDirectory()) {
					WikiEnvironmentManager.getInstance().createEnvironment(
							each.getName());
					// initArticleManager(each.getName());

				}
			}
		}

	}

	/**
	 * Returns a KnowledgeService for a given article name
	 *
	 * @param web
	 * @param topic
	 * @return
	 */
	public static KnowledgeBase getAD3webKnowledgeServiceInTopic(
			String web, String topic) {
		WikiEnvironment env = WikiEnvironmentManager.getInstance()
				.getEnvironment(web, defaultJarsPath);
		Collection<KnowledgeBase> coll = env.getServices();
		for (KnowledgeBase knowledgeService : coll) {
			if (knowledgeService.getId().startsWith(topic + "..")) {
				return knowledgeService;
			}
		}

		return null;
	}

	public static SessionBroker getBroker(java.util.Map<String, String> parameterMap) {
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);

		return getBroker(user, web);
	}

	public static SessionBroker getBroker(String user, String web) {
		WikiEnvironment env = WikiEnvironmentManager.getInstance()
				.getEnvironments(web);
		SessionBroker broker = env.getBroker(user);
		return broker;
	}

	public static WikiEnvironment getDPSE(
			java.util.Map<String, String> parameterMap) {
		// String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);

		WikiEnvironment env = WikiEnvironmentManager.getInstance()
				.getEnvironments(web);
		return env;
	}

	public static WikiEnvironment getDPSE(String web) {

		WikiEnvironment env = WikiEnvironmentManager.getInstance()
				.getEnvironments(web);
		return env;
	}

	public static URL getKbUrl(String web, String id) {
		String varPath = getWebEnvironmentPath(web);
		varPath = varPath + id + ".jar";
		URL url = null;
		try {
			url = new File(varPath).toURI().toURL();
		}
		catch (MalformedURLException e) {
			Logger.getLogger(KnowWEUtils.class.getName())
					.warning(
							"Cannot identify url for knowledgebase : "
									+ e.getMessage());
		}
		return url;
	}

	public static String getWebEnvironmentPath(String web) {
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
		String sessionDir = rb.getString("knowwe.config.path.currentWeb");
		sessionDir = sessionDir.replaceAll("\\$web\\$", web);
		sessionDir = getRealPath(KnowWEEnvironment.getInstance()
				.getWikiConnector().getServletContext(), sessionDir);
		return sessionDir;
	}

	public static String getSessionPath(UserContext context) {
		String user = context.getParameter(KnowWEAttributes.USER);
		String web = context.getParameter(KnowWEAttributes.WEB);
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
		String sessionDir = rb.getString("knowwe.config.path.sessions");
		sessionDir = sessionDir.replaceAll("\\$web\\$", web);
		sessionDir = sessionDir.replaceAll("\\$user\\$", user);

		sessionDir = getRealPath(context.getServletContext(), sessionDir);
		return sessionDir;
	}

	public static String getRealPath(ServletContext context, String varPath) {
		if (context != null && varPath.indexOf("$webapp_path$") != -1) {
			String realPath = context.getRealPath("");
			realPath = realPath.replace('\\', '/');
			while (realPath.endsWith("/")) {
				realPath = realPath.substring(0, realPath.length() - 1);
			}
			varPath = varPath.replaceAll("\\$webapp_path\\$", realPath);
		}
		return varPath;
	}

	public static D3webKnowledgeHandler getKnowledgeRepresentationHandler(
			String web) {
		Collection<KnowledgeRepresentationHandler> handlers = KnowWEEnvironment
				.getInstance().getKnowledgeRepresentationManager(web)
				.getHandlers();
		for (KnowledgeRepresentationHandler handler : handlers) {
			if (handler instanceof D3webKnowledgeHandler) {
				return (D3webKnowledgeHandler) handler;
			}
		}
		return null;
	}

}
