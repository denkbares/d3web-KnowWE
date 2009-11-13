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

package de.d3web.we.utils;

import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.store.SectionStore;
import de.d3web.we.module.DefaultTextType;

public class KnowWEUtils {

//	public static URL getKbUrl(String web,String id) {
//		String varPath = getWebEnvironmentPath(web);
//		varPath = varPath + id + ".jar";
//		URL url = null;
//		try {
//			url = new File(varPath).toURI().toURL();
//		} catch (MalformedURLException e) {
//			Logger.getLogger(KnowWEUtils.class.getName()).warning("Cannot identify url for knowledgebase : " + e.getMessage());
//		}
//		return url;
//	}
	
	public static void storeSectionInfo(String web, String article, String kdomid, String key, Object o) {
		KnowWEEnvironment.getInstance().getArticleManager(web).getTypeStore().storeObject(article, kdomid, key, o);
	}
	
	public static void storeSectionInfo(Section sec, String key, Object o) {
		storeSectionInfo(sec.getWeb(), sec.getTitle(), sec.getId(), key, o);
	}
	
	public static void putSectionStore(String web, String article, String kdomid, SectionStore store) {
		KnowWEEnvironment.getInstance().getArticleManager(web).getTypeStore().putSectionStore(article, kdomid, store);
	}
	
	public static Object getStoredObject(String web, String article, String kdomid, String key) {
		return KnowWEEnvironment.getInstance().getArticleManager(web).getTypeStore().getStoredObject(article, kdomid, key);
	}
	
	public static Object getStoredObject(Section s , String key) {
		return getStoredObject(s.getWeb(), s.getTitle(), s.getId(), key);
	}
	
	public static Object getOldStoredObject(String web, String article, String kdomid, String key) {
		return KnowWEEnvironment.getInstance().getArticleManager(web).getTypeStore().getOldStoredObject(article, kdomid, key);
	}
	
	public static SectionStore getSectionStore(String web, String article, String kdomid) {
		return KnowWEEnvironment.getInstance().getArticleManager(web).getTypeStore().getStoredObjects(article, kdomid);
	}
	
	public static SectionStore getOldSectionStore(String web, String article, String kdomid) {
		return KnowWEEnvironment.getInstance().getArticleManager(web).getTypeStore().getOldStoredObjects(article, kdomid);
	}
	
	public static String convertUmlaut(String text) {
		if(text == null) return null;
		String result = text;
		result = result.replaceAll("Ä", "&Auml;");
		result = result.replaceAll("Ö", "&Ouml;");
		result = result.replaceAll("Ü", "&Uuml;");
		result = result.replaceAll("ä", "&auml;");
		result = result.replaceAll("ö", "&ouml;");
		result = result.replaceAll("ü", "&uuml;");
		result = result.replaceAll("ß", "&szlig;");
		return result;
	}
	
//	public static String getVariablePath(ServletContext context, String realPath) {
//		String varPath = context.getRealPath("");
//		varPath = varPath.replace('\\', '/');
//		realPath = realPath.replaceAll(varPath, "\\$webapp_path\\$");
//		return realPath;
//	}

	
	
	
//public static String repairUmlauts(String s) {
//		// then replace special characters
//		s = s.replaceAll("&szlig;","ß");
//		s = s.replaceAll("&auml;","ä");
//		s = s.replaceAll("&uuml;","ü");
//		s = s.replaceAll("&ouml;","ö");
//		s = s.replaceAll("&Auml;","Ä");
//		s = s.replaceAll("&Uuml;","Ü");
//		s = s.replaceAll("&Ouml;","Ö");
//		s = s.replaceAll("&deg;","°");
//		s = s.replaceAll("&micro;","µ");
//		s = s.replaceAll("&apos;", "'");
//		return(s);
//	}
	
	/**
	 * 
	 * Unmasks output strings
	 * 
	 * @param htmlContent
	 * @return
	 */
	public static String unmaskHTML(String htmlContent) {
		htmlContent = htmlContent.replaceAll(
				KnowWEEnvironment.HTML_PLUGIN_BRACKETS_OPEN, "\\[\\{");
		htmlContent = htmlContent.replaceAll(
				KnowWEEnvironment.HTML_PLUGIN_BRACKETS_CLOSE, "}]");

		htmlContent = htmlContent.replaceAll(DefaultTextType.BLOB, "");

		htmlContent = htmlContent.replaceAll(
				KnowWEEnvironment.HTML_DOUBLEQUOTE, "\"");
		htmlContent = htmlContent
				.replaceAll(KnowWEEnvironment.HTML_QUOTE, "\'");
		htmlContent = htmlContent.replaceAll(KnowWEEnvironment.HTML_GT, ">");
		htmlContent = htmlContent.replaceAll(KnowWEEnvironment.HTML_ST, "<");

		htmlContent = htmlContent.replaceAll(
				KnowWEEnvironment.HTML_BRACKET_OPEN, "[");
		htmlContent = htmlContent.replaceAll(
				KnowWEEnvironment.HTML_BRACKET_CLOSE, "]");
		return htmlContent;
	}

	/**
	 * 
	 * masks output strings
	 * 
	 * @param htmlContent
	 * @return
	 */
	public static String maskHTML(String htmlContent) {
		htmlContent = htmlContent.replaceAll("\\[\\{",
				KnowWEEnvironment.HTML_PLUGIN_BRACKETS_OPEN);
		htmlContent = htmlContent.replaceAll("}]",
				KnowWEEnvironment.HTML_PLUGIN_BRACKETS_CLOSE);

		htmlContent = htmlContent.replaceAll("\"",
				KnowWEEnvironment.HTML_DOUBLEQUOTE);
		htmlContent = htmlContent.replaceAll("'", KnowWEEnvironment.HTML_QUOTE);
		htmlContent = htmlContent.replaceAll(">", KnowWEEnvironment.HTML_GT);
		htmlContent = htmlContent.replaceAll("<", KnowWEEnvironment.HTML_ST);

		htmlContent = htmlContent.replace("[",
				KnowWEEnvironment.HTML_BRACKET_OPEN);
		htmlContent = htmlContent.replace("]",
				KnowWEEnvironment.HTML_BRACKET_CLOSE);
		return htmlContent;
	}
	
	public static String replaceUmlaut(String text) {
		String result = text;
		result = result.replaceAll("Ä", "AE");
		result = result.replaceAll("Ö", "OE");
		result = result.replaceAll("Ü", "UE");
		result = result.replaceAll("ä", "ae");
		result = result.replaceAll("ö", "oe");
		result = result.replaceAll("ü", "ue");
		result = result.replaceAll("ß", "ss");
		return result;
	}
	
	//	public static Broker getBroker(Model model) {
//		int i;
//		String web = (String)  BasicUtils.getModelAttribute(model, KnowWEAttributes.WEB, String.class, true);
//		Broker broker = (Broker) BasicUtils.getModelAttribute(model, KnowWEAttributes.getBrokerConstant(web), Broker.class, true);
//		if(broker == null) {
//			DPSEnvironment env = KnowWEUtils.getEnvironment(model);
//			String userID = (String) BasicUtils.getModelAttribute(model, KnowWEAttributes.USER, String.class, true);
//			if(userID == null || userID.equals(DPSEnvironment.defaultUser) || userID.trim().equals("")) {
//				broker = env.createBroker(userID);
//				model.setAttribute(KnowWEAttributes.getBrokerConstant(web), broker, model.getWebApp());
//			} else {
//				broker = env.getBroker(userID);
//				model.setAttribute(KnowWEAttributes.getBrokerConstant(web), broker, model.getWebApp());
//			}
//		}
//		return broker;
//	}
//	
	
	public static String getRealPath(ServletContext context, String varPath) {
		if (varPath.indexOf("$webapp_path$") != -1) {
			String realPath = context.getRealPath("");
			realPath = realPath.replace('\\', '/');
			while (realPath.endsWith("/")) {
				realPath = realPath.substring(0, realPath.length() - 1);
			}
			varPath = varPath.replaceAll("\\$webapp_path\\$", realPath);
		}
		return varPath;
	}
	
//	public static String getSessionPath(String user, String web) {
//		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
//		String sessionDir = rb.getString("KWiki.config.path.sessions");
//		sessionDir = sessionDir.replaceAll("\\$web\\$", web);
//		sessionDir = sessionDir.replaceAll("\\$user\\$", user);
//
//		sessionDir = getRealPath(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), sessionDir);
//		return sessionDir;
//	}
	
	
	public static String getSessionPath(KnowWEParameterMap parameterMap) {
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
		String sessionDir = rb.getString("KWiki.config.path.sessions");
		sessionDir = sessionDir.replaceAll("\\$web\\$", web);
		sessionDir = sessionDir.replaceAll("\\$user\\$", user);

		sessionDir = getRealPath(parameterMap.getContext(), sessionDir);
		return sessionDir;
	}
	

	
//	public static String getWebEnvironmentPath(String web) {
//		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
//		String sessionDir = rb.getString("KWiki.config.path.currentWeb");
//		sessionDir = sessionDir.replaceAll("\\$web\\$", web);
//		sessionDir = getRealPath(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(), sessionDir);
//		return sessionDir;
//	}

//	public static URL getUrl(String path) {
//		URL u = null;
//		try {
//			u = new File(path).toURI().toURL();
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return u;
//	}
	
}
