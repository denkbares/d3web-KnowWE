package de.d3web.we.utils;

import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

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
	
	public static Object getStoredObject(String web, String article, String kdomid, String key) {
		return KnowWEEnvironment.getInstance().getArticleManager(web).getTypeStore().getStoredObject(article, kdomid, key);
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
