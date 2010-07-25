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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.terminology.TerminologyHandler;

public class KnowWEUtils {
	
	private static final String createMsgMapKey(Class<?> msgType) {
		return "message_map_key_" + msgType.getName();
	}

	// public static URL getKbUrl(String web,String id) {
	// String varPath = getWebEnvironmentPath(web);
	// varPath = varPath + id + ".jar";
	// URL url = null;
	// try {
	// url = new File(varPath).toURI().toURL();
	// } catch (MalformedURLException e) {
	// Logger.getLogger(KnowWEUtils.class.getName()).warning("Cannot identify url for knowledgebase : "
	// + e.getMessage());
	// }
	// return url;
	// }

	/**
	 * Creates a wiki-markup-styled to the section. The created link navigates
	 * the user to the article of the section. If the section is rendered with
	 * an anchor (see method {@link #getAnchor(Section)}) the page is also
	 * scrolled to the section.
	 * <p>
	 * Please not that the link will only work if it is put into "[" ... "]"
	 * brackets and rendered through the wiki rendering pipeline.
	 * 
	 * @param section
	 *            the section to create the link for
	 * @return the created link
	 */
	public static String getLink(Section<?> section) {
		return section.getTitle() + "#" + Math.abs(section.getID().hashCode());
	}

	/**
	 * Creates a unique anchor name for the section to link to. See method
	 * {@link #getLink(Section)} for more details on how to use this method.
	 * 
	 * @param section
	 *            the section to create the anchor for.
	 * @return the unique anchor name
	 */
	public static String getAnchor(Section<?> section) {
		// TODO: figure out how JSPWiki builds section anchor names
		return "section-"
				+ section.getArticle().getTitle().replace(' ', '+') + "-"
				+ Math.abs(section.getID().hashCode());
	}
	
	/**
	 * Clears all Messages for the given article, section, source and msgType.
	 * 
	 * @param article is the article you want to clear the message for
	 * @param sec is the section you want to clear the message for
	 * @param source is the Class the message you want to clear originate from
	 * @param msgType is the Class of the message you want to clear
	 */
	public static <MSGType> void clearMessages(KnowWEArticle article, Section<?> sec,
			Class<?> source, Class<MSGType> msgType) {
		storeMessages(article, sec, source, msgType, new ArrayList<MSGType>(0));
	}
	
	/**
	 * Stores a single Message for the given Section and source. <p/>
	 * <b>ATTENTION: For this method applies the same as for the method
	 * KnowWEUtils#storeMessages(Section, Class, Class, Collection) .
	 * It can only be used once for the given set of parameters. If you use this method
	 * a second time with the same parameters, the first Message gets overwritten!</b>
	 * 
	 * @param article is the article you want to store the message for
	 * @param sec is the section you want to store the message for
	 * @param source is the Class the message originate from
	 * @param msgType is the Class of the message you want to store
	 * @param msgs is the message you want so store
	 */
	public static <MSGType> void storeSingleMessage(KnowWEArticle article, Section<?> sec,
			Class<?> source, Class<MSGType> msgType, MSGType msg) {
		if (msg != null) {
			List<MSGType> msgList = new ArrayList<MSGType>(1);
			msgList.add(msg);
			storeMessages(article, sec, source, msgType, msgList);
		}
	}
	
	/**
	 * Stores the given Collection of Messages <tt>m</tt> with the type <tt>MSGType</tt>
	 * from the Class <tt>source</tt> for the KnowWEArticle <tt>article</tt> and
	 * the Section <tt>s</tt>. <p/>
	 * <b>ATTENTION: This method can only be used once for each article, section,
	 * source and msgType. If you use this Method a second time with the same parameters, the
	 * first Collection gets overwritten!</b>
	 * 
	 * @param article is the article you want to store the messages for
	 * @param sec is the section you want to store the messages for
	 * @param source is the Class the messages originate from
	 * @param msgType is the Class of the messages you want to store
	 * @param msgs is the Collection of messages you want so store
	 */

	public static <MSGType> void storeMessages(KnowWEArticle article, Section<?> sec,
			Class<?> source, Class<MSGType> msgType, Collection<MSGType> msgs) {
		if (msgs != null) {
			Map<String, Collection<MSGType>> msgsMap = getMessagesMapModifiable(article, sec, msgType);
			if (msgsMap == null) {
				msgsMap = new HashMap<String, Collection<MSGType>>();
				KnowWEUtils.storeSectionInfo(article.getWeb(), article.getTitle(), sec.getID(),
						createMsgMapKey(msgType), msgsMap);
			}
			msgsMap.put(source.getName(), Collections.unmodifiableCollection(msgs));
		}
	}
	
	/**
	 * Returns an unmodifiable Collection containing all Messages of the KDOM subtree
	 * with the given Section as root.
	 * 
	 * @param article is the article you want the message from (not necessarily the same
	 * 		as <tt>sec.getArticle()</tt> because the Section could be included in another
	 * 		article)
	 * @param sec is the root of the KDOM subtree you want the messages from
	 * @param msgType is the Class of the Messages you want
	 * @return an unmodifiable Collection of Messages
	 */
	public static <MSGType> Collection<MSGType> getMessagesFromSubtree(KnowWEArticle article,
			Section<?> sec, Class<MSGType> msgType) {
		Collection<MSGType> msgsList = new ArrayList<MSGType>();
		List<Section<?>> nodes = new ArrayList<Section<?>>();
		sec.getAllNodesPreOrder(nodes);
		for (Section<?> n:nodes) {
			msgsList.addAll(getMessages(article, n, msgType));
		}
		return Collections.unmodifiableCollection(msgsList);
	}
	
	/**
	 * Returns an unmodifiable Collection containing all Messages of the Type <tt>MSGType</tt>.
	 * 
	 * @param article is the article you want the message from (not necessarily the same
	 * 		as <tt>sec.getArticle()</tt> because the Section could be included in another
	 * 		article)
	 * @param sec is the Section you want the messages from
	 * @param msgType is the Class of the Messages you want
	 * @return an unmodifiable Collection of Messages
	 */
	public static <MSGType> Collection<MSGType> getMessages(KnowWEArticle article, Section<?> sec,
			Class<MSGType> msgType) {
		Map<String, Collection<MSGType>> msgsMap =  getMessagesMapModifiable(article, sec, msgType);
		Collection<MSGType> msgsList = new ArrayList<MSGType>();
		if (msgsMap != null) {
			for (Collection<MSGType> coll:msgsMap.values()) {
				msgsList.addAll(coll);
			}
		}
		return Collections.unmodifiableCollection(msgsList);
	}
	
	/**
	 * Returns an unmodifiable Collection containing all Messages of the Type <tt>MSGType</tt>
	 * stored for the Class <tt>source</tt>.
	 * 
	 * @param article is the article you want the message from (not necessarily the same
	 * 		as <tt>sec.getArticle()</tt> because the Section could be included in another
	 * 		article)
	 * @param sec is the Section you want the messages from
	 * @param source is the Class of the source of the messages you want
	 * @param msgType is the Class of the Messages you want
	 * @return an unmodifiable Collection of Messages
	 */
	public static <MSGType> Collection<MSGType> getMessages(KnowWEArticle article, Section<?> sec,
			Class<?> source, Class<MSGType> msgType) {
		Map<String, Collection<MSGType>> msgsMap =  getMessagesMapModifiable(article, sec, msgType);
		if (msgsMap != null && msgsMap.containsKey(source.getName())) {
			return Collections.unmodifiableCollection(msgsMap.get(source.getName()));
		}
		return Collections.unmodifiableCollection(new ArrayList<MSGType>(0));
	}
	
	/**
	 * Returns the an unmodifiable Map containing all Messages of the Type <tt>MSGType</tt>.
	 * The Collections are mapped after the String <tt>source.getName()</tt>.
	 * 
	 * @param article is the article you want the message from (not necessarily the same
	 * 		as <tt>sec.getArticle()</tt> because the Section could be included in another
	 * 		article)
	 * @param sec is the Section you want the messages from
	 * @param msgType is the Class of the Messages you want
	 * @return an unmodifiable Map with the Messages, mapped after <tt>source.getName()</tt>
	 */
	public static <MSGType> Map<String, Collection<MSGType>> getMessagesMap(KnowWEArticle article,
			Section<?> sec, Class<MSGType> msgType) {
		return Collections.unmodifiableMap(getMessagesMapModifiable(article, sec, msgType));
	}
	
	/**
	 * This method is private to avoid misuse (this map is modifiable).
	 */
	@SuppressWarnings("unchecked")
	private static <MSGType> Map<String, Collection<MSGType>> getMessagesMapModifiable(KnowWEArticle article,
			Section<?> sec, Class<MSGType> msgType) {
		return (Map<String, Collection<MSGType>>) KnowWEUtils.getStoredObject(article.getWeb(),
				article.getTitle(), sec.getID(), createMsgMapKey(msgType));
	}



	/**
	 * This method is deprecated! If the Section is included in other articles,
	 * the returned object might be created for an other article!
	 */
	@Deprecated
	public static void storeSectionInfo(Section<?> sec, String key, Object o) {
		storeSectionInfo(sec.getWeb(), sec.getTitle(), sec.getID(), key, o);
	}

	/**
	 * This method is deprecated! If the Section is included in other articles,
	 * the object might be overwritten!
	 */
	@Deprecated
	public static Object getStoredObject(Section<?> s, String key) {
		return getStoredObject(s.getWeb(), s.getTitle(), s.getID(), key);
	}

	public static void storeSectionInfo(KnowWEArticle article, Section<?> sec, String key, Object o) {
		storeSectionInfo(article.getWeb(), article.getTitle(), sec.getID(), key, o);
	}

	public static Object getStoredObject(KnowWEArticle article, Section<?> s, String key) {
		return getStoredObject(article.getWeb(), article.getTitle(), s.getID(), key);
	}

	public static Object getObjectFromLastVersion(KnowWEArticle article, Section<?> s, String key) {
		return KnowWEEnvironment.getInstance().getArticleManager(article.getWeb()).getTypeStore()
				.getLastStoredObject(article.getTitle(),
						s.isReusedBy(article.getTitle()) ? s.getLastID() : s.getID(), key);
	}

	public static void storeSectionInfo(String web, String article, String kdomid, String key, Object o) {
		KnowWEEnvironment.getInstance().getArticleManager(web).getTypeStore().storeObject(
				article, kdomid, key, o);
	}

	public static Object getStoredObject(String web, String article, String kdomid, String key) {
		return KnowWEEnvironment.getInstance().getArticleManager(web).getTypeStore().getStoredObject(
				article, kdomid, key);
	}

	public static TerminologyHandler getTerminologyHandler(String web) {
		return (TerminologyHandler) KnowWEEnvironment.getInstance()
				.getKnowledgeRepresentationManager(web).getHandler(TerminologyHandler.HANDLER_KEY);
	}

	public static List<Integer> getPositionsInKDOM(Section<?> s) {
		return getPositionsInKDOM(s, s.getArticle().getSection());
	}

	public static List<Integer> getPositionsInKDOM(Section<?> start, Section<?> end) {
		List<Integer> positions = new ArrayList<Integer>();
		Section<?> temp = start;
		Section<?> tempFather = temp.getFather();
		while (temp != end && tempFather != null) {
			positions.add(tempFather.getChildren().indexOf(temp));
			temp = tempFather;
			tempFather = temp.getFather();
		}
		return positions;
	}

	public static String convertUmlaut(String text) {
		if (text == null) return null;
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

	/**
	 * returns whether a text contains nothing except spaces and newlines
	 * 
	 * @param text
	 * @return
	 */
	public static boolean isEmpty(String text) {
		if (text == null) return true;
		if (text.length() == 0) return true;
		text = text.replaceAll("\r", "");
		text = text.replaceAll("\n", "");
		text = text.replaceAll(" ", "");

		if (text.length() == 0) return true;

		return false;

	}

	// public static String getVariablePath(ServletContext context, String
	// realPath) {
	// String varPath = context.getRealPath("");
	// varPath = varPath.replace('\\', '/');
	// realPath = realPath.replaceAll(varPath, "\\$webapp_path\\$");
	// return realPath;
	// }

	// public static String repairUmlauts(String s) {
	// // then replace special characters
	// s = s.replaceAll("&szlig;","ß");
	// s = s.replaceAll("&auml;","ä");
	// s = s.replaceAll("&uuml;","ü");
	// s = s.replaceAll("&ouml;","ö");
	// s = s.replaceAll("&Auml;","Ä");
	// s = s.replaceAll("&Uuml;","Ü");
	// s = s.replaceAll("&Ouml;","Ö");
	// s = s.replaceAll("&deg;","°");
	// s = s.replaceAll("&micro;","µ");
	// s = s.replaceAll("&apos;", "'");
	// return(s);
	// }

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
		// htmlContent = htmlContent.replace(
		// KnowWEEnvironment.HTML_CURLY_BRACKET_OPEN, "{");
		// htmlContent = htmlContent.replace(
		// KnowWEEnvironment.HTML_CURLY_BRACKET_CLOSE, "}");

		return htmlContent;
	}

	public static String unmaskNewline(String htmlContent) {
		htmlContent = htmlContent.replace(KnowWEEnvironment.NEWLINE, "\n");
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
		// htmlContent = htmlContent.replace("{",
		// KnowWEEnvironment.HTML_CURLY_BRACKET_OPEN);
		// htmlContent = htmlContent.replace("}",
		// KnowWEEnvironment.HTML_CURLY_BRACKET_CLOSE);
		return htmlContent;
	}

	public static String maskNewline(String htmlContent) {
		htmlContent = htmlContent.replace("\n", KnowWEEnvironment.NEWLINE);
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

	public static String trimQuotes(String text) {
		String trimmed = text.trim();

		if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
			return trimmed.substring(1, trimmed.length() - 1).trim();
		}

		return trimmed;
	}

	// public static Broker getBroker(Model model) {
	// int i;
	// String web = (String) BasicUtils.getModelAttribute(model,
	// KnowWEAttributes.WEB, String.class, true);
	// Broker broker = (Broker) BasicUtils.getModelAttribute(model,
	// KnowWEAttributes.getBrokerConstant(web), Broker.class, true);
	// if(broker == null) {
	// DPSEnvironment env = KnowWEUtils.getEnvironment(model);
	// String userID = (String) BasicUtils.getModelAttribute(model,
	// KnowWEAttributes.USER, String.class, true);
	// if(userID == null || userID.equals(DPSEnvironment.defaultUser) ||
	// userID.trim().equals("")) {
	// broker = env.createBroker(userID);
	// model.setAttribute(KnowWEAttributes.getBrokerConstant(web), broker,
	// model.getWebApp());
	// } else {
	// broker = env.getBroker(userID);
	// model.setAttribute(KnowWEAttributes.getBrokerConstant(web), broker,
	// model.getWebApp());
	// }
	// }
	// return broker;
	// }
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

	// public static String getSessionPath(String user, String web) {
	// ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
	// String sessionDir = rb.getString("knowwe.config.path.sessions");
	// sessionDir = sessionDir.replaceAll("\\$web\\$", web);
	// sessionDir = sessionDir.replaceAll("\\$user\\$", user);
	//
	// sessionDir =
	// getRealPath(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(),
	// sessionDir);
	// return sessionDir;
	// }

	public static String getSessionPath(KnowWEParameterMap parameterMap) {
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
		String sessionDir = rb.getString("knowwe.config.path.sessions");
		sessionDir = sessionDir.replaceAll("\\$web\\$", web);
		sessionDir = sessionDir.replaceAll("\\$user\\$", user);

		sessionDir = getRealPath(parameterMap.getContext(), sessionDir);
		return sessionDir;
	}

	public static String getRenderedInput(String questionid, String question,
			String namespace, String userName, String title, String text,
			String type) {
		question = URLEncoder.encode(question);
		// text=URLEncoder.encode(text);

		String rendering = "<span class=\"semLink\" "
				+ "rel=\"{type: '" + type + "', objectID: '" + questionid
				+ "', termName: '" + text + "', user:'" + userName + "'}\">"
				+ text + "</span>";
		return rendering;
	}

	public static String getErrorQ404(String question, String text) {
		String rendering = "<span class=\"semLink\"><a href=\"#\" title=\""
				+ "Question not found:"
				+ question
				+ "\" >"
				+ text
				+ "</a></span>";
		return rendering;

	}

	/**
	 * Escapes the given string for safely using user-input in web sites.
	 * 
	 * @param text
	 *            Text to escape
	 * @return Sanitized text
	 */
	public static String html_escape(String text) {
		if (text == null)
			return null;

		return text.replaceAll("&", "&amp;").
				replaceAll("\"", "&quot;").
				replaceAll("<", "&lt;").
				replaceAll(">", "&gt;");
	}

	/**
	 * Performs URL encoding on the sting
	 * 
	 * @param text
	 * @return URLencoded string
	 */
	public static String urlencode(String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(text);
		}
	}
	
	/**
	 * Performs URL decoding on the sting
	 * 
	 * @param text URLencoded string
	 * @return URLdecoded string
	 */
	public static String urldecode(String text) {
		try {
			return URLDecoder.decode(text, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return URLDecoder.decode(text);
		}
	}

	// public static String getWebEnvironmentPath(String web) {
	// ResourceBundle rb = ResourceBundle.getBundle("KnowWE_config");
	// String sessionDir = rb.getString("knowwe.config.path.currentWeb");
	// sessionDir = sessionDir.replaceAll("\\$web\\$", web);
	// sessionDir =
	// getRealPath(KnowWEEnvironment.getInstance().getWikiConnector().getServletContext(),
	// sessionDir);
	// return sessionDir;
	// }

	// public static URL getUrl(String path) {
	// URL u = null;
	// try {
	// u = new File(path).toURI().toURL();
	// } catch (MalformedURLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return u;
	// }
	

}
