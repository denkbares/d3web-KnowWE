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

package de.d3web.we.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.records.SessionConversionFactory;
import de.d3web.core.records.SessionRecord;
import de.d3web.core.records.io.SessionPersistenceManager;
import de.d3web.core.session.Session;
import de.d3web.scoring.Score;
import de.d3web.we.basic.D3webKnowledgeHandler;
import de.d3web.we.object.D3webTerm;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TermIdentifier;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.knowRep.KnowledgeRepresentationHandler;

public class D3webUtils {

	static ArrayList<String> possibleScorePoints;

	public static List<String> getPossibleScores() {
		if (possibleScorePoints == null) {

			possibleScorePoints = new ArrayList<String>();

			String n = "N";
			String p = "P";
			for (int i = 1; i <= 7; i++) {
				possibleScorePoints.add(n + i);
				possibleScorePoints.add(p + i);
			}
			possibleScorePoints.add("P5x");
			possibleScorePoints.add("N5x");
			possibleScorePoints.add("!");
			possibleScorePoints.add("?");
			possibleScorePoints.add("excluded");
			possibleScorePoints.add("established");
			possibleScorePoints.add("suggested");
		}
		return possibleScorePoints;
	}

	public static boolean storeSessionRecordsAsAttachment(String user, Collection<Session> sessions, String attachmentArticle, String attachmentName) throws IOException {

		Collection<SessionRecord> sessionRecords = new LinkedList<SessionRecord>();

		for (Session session : sessions) {
			sessionRecords.add(SessionConversionFactory.copyToSessionRecord(
					session));
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		SessionPersistenceManager.getInstance().saveSessions(outputStream, sessionRecords);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

		return Environment.getInstance().getWikiConnector()
				.storeAttachment(attachmentArticle, attachmentName, user, inputStream);
	}

	public static Score getScoreForString(String argument) {
		Score score = null;
		List<Score> allScores = Score.getAllScores();
		for (Score sc : allScores) {
			if (sc.getSymbol().equals(argument)) {
				score = sc;
				break;
			}
		}
		if (argument.equals("!")) {
			score = Score.P7;
		}
		if (argument.equals("?")) {
			score = Score.P5;
		}
		if (argument.equalsIgnoreCase("excluded")) {
			score = Score.N7;
		}
		if (argument.equalsIgnoreCase("established")) {
			score = Score.P7;
		}
		if (argument.equalsIgnoreCase("suggested")) {
			score = Score.P5;
		}

		return score;
	}

	/**
	 * Deletes a terminology object and all potential children from the
	 * knowledge base. Before the deletion the corresponding knowledge instances
	 * (KnowledgeSlices) are also removed.
	 * 
	 * @param object the object to be removed
	 */
	public static void removeRecursively(TerminologyObject object) {
		for (TerminologyObject to : object.getChildren()) {
			removeRecursively(to);
		}
		object.destroy();
	}

	/**
	 * Utility method to get a {@link KnowledgeBase} for a specified article.
	 * 
	 * @created 15.12.2010
	 * @param article the article the knowledge base is compiled
	 * @return the knowledge base if such one exists, null otherwise
	 * @throws NullPointerException if the article is null
	 */
	public static KnowledgeBase getKnowledgeBase(Article article) {
		return getKnowledgeBase(article.getWeb(), article.getTitle());
	}

	/**
	 * Utility method to get a {@link KnowledgeBase} for an article specified by
	 * its web and topic.
	 * 
	 * @created 15.12.2010
	 * @param web the web of the article the knowledge base is compiled
	 * @param title the title of the article the knowledge base is compiled
	 * @return the knowledge base if such one exists, null otherwise
	 * @throws NullPointerException if web or topic is null
	 */
	public static KnowledgeBase getKnowledgeBase(String web, String title) {
		if (web == null || title == null) {
			throw new NullPointerException(
					"Cannot acces knowledge base with 'web' and/or 'topic' null");
		}
		D3webKnowledgeHandler knowledgeHandler =
				D3webUtils.getKnowledgeRepresentationHandler(web);
		if (knowledgeHandler != null) {
			KnowledgeBase kb = knowledgeHandler.getKnowledgeBase(title);
			return kb;
		}
		return null;
	}

	/**
	 * Gets the first usable KnowledgeBase of a web.
	 * 
	 * @created 16.06.2011
	 * @param web
	 * @return
	 */
	public static KnowledgeBase getFirstKnowledgeBase(String web) {
		D3webKnowledgeHandler knowledgeHandler =
				D3webUtils.getKnowledgeRepresentationHandler(web);
		D3webKnowledgeHandler repHandler = D3webUtils.getKnowledgeRepresentationHandler(web);
		KnowledgeBase base = null;
		for (String t : knowledgeHandler.getKnowledgeArticles()) {
			base = repHandler.getKnowledgeBase(t);
			if (base.getName() != null) return base;
		}
		return null;
	}

	// public static Session getFirstSession(String web, String user, String
	// topic) {
	//
	// KnowledgeBase kb = D3webUtils.getFirstKnowledgeBase(web);
	// SessionBroker broker = D3webUtils.getBroker(user, web);
	// Session session = broker.getSession(kb.getId());
	//
	// return session;
	// }

	@SuppressWarnings("unchecked")
	public static <TermObject extends NamedObject> TermObject getTermObjectDefaultImplementation(Article article, Section<? extends D3webTerm<TermObject>> section) {
		TermIdentifier termIdentifier = section.get().getTermIdentifier(section);
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(article.getWeb(), article.getTitle());
		TerminologyObject termObject = kb.getManager().search(termIdentifier.getLastPathElement());
		if (termObject != null) {
			if (section.get().getTermObjectClass(section).isAssignableFrom(termObject.getClass())) {
				return (TermObject) termObject;
			}
			else {
				return null;
			}
		}
		Collection<TerminologyObject> foundTermObjects = getTermObjectsIgnoreTermObjectClass(
				article, section);
		if (foundTermObjects.size() == 1) {
			termObject = foundTermObjects.iterator().next();
			if (section.get().getTermObjectClass(section).isAssignableFrom(termObject.getClass())) {
				return (TermObject) termObject;
			}
		}
		return null;
	}

	public static <TermObject extends NamedObject> Collection<TerminologyObject> getTermObjectsIgnoreTermObjectClass(Article article, Section<? extends D3webTerm<TermObject>> section) {
		TermIdentifier termIdentifier = section.get().getTermIdentifier(section);
		TerminologyManager terminologyHandler = KnowWEUtils.getTerminologyManager(article);
		KnowledgeBase kb = D3webUtils.getKnowledgeBase(article.getWeb(), article.getTitle());
		TerminologyObject termObject;
		Collection<TermIdentifier> allTermsEqualIgnoreCase = terminologyHandler.getAllTermsEqualIgnoreCase(termIdentifier);
		List<TerminologyObject> foundTermObjects = new ArrayList<TerminologyObject>();
		for (TermIdentifier termEqualIgnoreCase : allTermsEqualIgnoreCase) {
			termObject = kb.getManager().search(termEqualIgnoreCase.getLastPathElement());
			if (termObject != null) foundTermObjects.add(termObject);
		}
		return foundTermObjects;
	}

	public static D3webKnowledgeHandler getKnowledgeRepresentationHandler(
			String web) {
		Collection<KnowledgeRepresentationHandler> handlers = Environment
				.getInstance().getKnowledgeRepresentationManager(web)
				.getHandlers();
		for (KnowledgeRepresentationHandler handler : handlers) {
			if (handler instanceof D3webKnowledgeHandler) {
				return (D3webKnowledgeHandler) handler;
			}
		}
		return null;
	}

	public static String getSessionPath(UserContext context) {
		String user = context.getParameter(Attributes.USER);
		String web = context.getParameter(Attributes.WEB);
		ResourceBundle rb = KnowWEUtils.getConfigBundle();
		String sessionDir = rb.getString("knowwe.config.path.sessions");
		sessionDir = sessionDir.replaceAll("\\$web\\$", web);
		sessionDir = sessionDir.replaceAll("\\$user\\$", user);
		sessionDir = KnowWEUtils.getRealPath(sessionDir);
		return sessionDir;
	}

	public static String getWebEnvironmentPath(String web) {
		ResourceBundle rb = KnowWEUtils.getConfigBundle();
		String sessionDir = rb.getString("knowwe.config.path.currentWeb");
		sessionDir = sessionDir.replaceAll("\\$web\\$", web);
		sessionDir = KnowWEUtils.getRealPath(sessionDir);
		return sessionDir;
	}

	public static URL getKnowledgeBaseURL(String web, String id) {
		String varPath = getWebEnvironmentPath(web);
		varPath = varPath + id + ".jar";
		URL url = null;
		try {
			url = new File(varPath).toURI().toURL();
		}
		catch (MalformedURLException e) {
			Logger.getLogger(KnowWEUtils.class.getName())
					.warning("Cannot identify url for knowledgebase : "
									+ e.getMessage());
		}
		return url;
	}

	public static ResourceBundle getD3webBundle() {
		return ResourceBundle.getBundle("KnowWE_plugin_d3web_messages");
	}

	public static ResourceBundle getD3webBundle(HttpServletRequest request) {
		if (request == null) return D3webUtils.getD3webBundle();
		Locale.setDefault(Environment.getInstance().getWikiConnector()
				.getLocale(request));
		return D3webUtils.getD3webBundle();
	}

	public static ResourceBundle getD3webBundle(UserContext user) {
		if (user == null) return getD3webBundle();
		Locale.setDefault(Environment.getInstance().getWikiConnector()
				.getLocale(user.getRequest()));
		return getD3webBundle();
	}

}
