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
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import com.denkbares.events.EventManager;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.d3web.core.inference.LoopTerminator;
import de.d3web.core.inference.LoopTerminator.LoopStatus;
import de.d3web.core.inference.SessionTerminatedException;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.records.SessionConversionFactory;
import de.d3web.core.records.SessionRecord;
import de.d3web.core.records.io.SessionPersistenceManager;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.values.Unknown;
import de.d3web.indication.inference.PSMethodUserSelected;
import de.d3web.scoring.Score;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.object.D3webTermDefinition;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.taghandler.TagHandlerType;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.d3web.event.FindingSetEvent;
import de.knowwe.notification.NotificationManager;
import de.knowwe.notification.StandardNotification;

public class D3webUtils {

	static ArrayList<String> possibleScorePoints;

	public static List<String> getPossibleScores() {
		if (possibleScorePoints == null) {

			possibleScorePoints = new ArrayList<>();

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

	/**
	 * Return an existing session for the given user context and knowledge base. If, for the given compiler, we
	 * don't yet have a session, we don't create one, but return null.
	 *
	 * @param compiler the compiler for which we want a session
	 * @param context  the user context for which we want the session
	 * @return an existing session for the given context and compiler or null, if there is non
	 */
	@Nullable
	public static Session getExistingSession(@Nullable D3webCompiler compiler, UserContext context) {
		if (compiler != null) {
			return SessionProvider.getExistingSession(context, D3webUtils.getKnowledgeBase(compiler));
		}
		return null;
	}

	@Nullable
	public static NamedObject getTermObject(D3webCompiler compiler, Identifier identifier) {
		Collection<Section<?>> definingSections = compiler.getTerminologyManager().getTermDefiningSections(identifier);
		for (Section<?> definingSection : definingSections) {
			if (definingSection.get() instanceof D3webTermDefinition) {
				Section<D3webTermDefinition> d3webDefinitionSection = Sections.cast(definingSection, D3webTermDefinition.class);
				@SuppressWarnings("unchecked") NamedObject termObject
						= d3webDefinitionSection.get().getTermObject(compiler, d3webDefinitionSection);
				if (termObject != null) return termObject;
			}
		}
		return null;
	}

	@Nullable
	public static D3webCompiler getCompiler(Section<?> section) {
		if (section.get() instanceof TagHandlerType) {
			return D3webUtils.getCompiler(section.getArticle());
		}
		else {
			return Compilers.getCompiler(section, D3webCompiler.class);
		}
	}

	@Deprecated
	@Nullable
	public static D3webCompiler getCompiler(Article master) {
		return Compilers.getCompiler(master, D3webCompiler.class);
	}

	public static void storeSessionRecordsAsAttachment(String user, Collection<Session> sessions, String attachmentArticle, String attachmentName) throws IOException {

		Collection<SessionRecord> sessionRecords = new LinkedList<>();
		for (Session session : sessions) {
			sessionRecords.add(SessionConversionFactory.copyToSessionRecord(session));
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		SessionPersistenceManager.getInstance().saveSessions(outputStream, sessionRecords);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

		Environment.getInstance().getWikiConnector()
				.storeAttachment(attachmentArticle, attachmentName, user, inputStream);
	}

	@Nullable
	public static Score getScoreForString(String argument) {
		for (Score sc : Score.getAllScores()) {
			if (sc.getSymbol().equals(argument)) {
				return sc;
			}
		}
		if (argument.equals("!")) {
			return Score.P7;
		}
		if (argument.equals("?")) {
			return Score.P5;
		}
		if (argument.equalsIgnoreCase("excluded")) {
			return Score.N7;
		}
		if (argument.equalsIgnoreCase("established")) {
			return Score.P7;
		}
		if (argument.equalsIgnoreCase("suggested")) {
			return Score.P5;
		}

		return null;
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

	@Contract("null -> null; !null -> !null")
	public static KnowledgeBase getKnowledgeBase(D3webCompiler compiler) {
		if (compiler == null) return null;
		return compiler.getKnowledgeBase();
	}

	public static Collection<KnowledgeBase> getKnowledgeBases(ArticleManager manager) {
		Collection<D3webCompiler> compilers = Compilers.getCompilers(manager, D3webCompiler.class);
		Collection<KnowledgeBase> kbs = new ArrayList<>(compilers.size());
		for (D3webCompiler d3webCompiler : compilers) {
			kbs.add(d3webCompiler.getKnowledgeBase());
		}
		return kbs;
	}

	/**
	 * Utility method to get a {@link KnowledgeBase} for a specified article.
	 *
	 * @param article the article the knowledge base is compiled
	 * @return the knowledge base if such one exists, null otherwise
	 * @throws NullPointerException if the article is null
	 * @created 15.12.2010
	 */
	@Deprecated
	public static KnowledgeBase getKnowledgeBase(Article article) {
		return getKnowledgeBase(article.getWeb(), article.getTitle());
	}

	/**
	 * Utility method to get a {@link KnowledgeBase} for an article specified by
	 * its web and topic. If no such knowledge base exists, a new knowledge base
	 * is created for the article and returned.
	 *
	 * @param web   the web of the article the knowledge base is compiled
	 * @param title the title of the article the knowledge base is compiled
	 * @return the knowledge base if such one exists, null otherwise
	 * @throws NullPointerException if web or topic is null
	 * @created 15.12.2010
	 * @deprecated it is possible that there are multiple knowledge bases on one
	 * page, this method will always only return the first one. You
	 * can use
	 */
	@Deprecated
	public static KnowledgeBase getKnowledgeBase(String web, String title) {
		Article article = Environment.getInstance().getArticle(web, title);
		if (article == null) return null;
		Section<PackageCompileType> compileSection = Sections.successor(
				article.getRootSection(),
				PackageCompileType.class);
		if (compileSection == null) return null;
		D3webCompiler d3webCompiler = getCompiler(compileSection);
		if (d3webCompiler == null) return null;
		return d3webCompiler.getKnowledgeBase();
	}

	/**
	 * If the given section is compiled by a {@link D3webCompiler} or is a
	 * section of the type {@link PackageCompileType} belonging to a knowledge
	 * base markup, the knowledge base of the right {@link D3webCompiler} is
	 * returned. If a {@link TagHandlerType} is given, the {@link KnowledgeBase}
	 * of the same article is returned for compatibility reasons.
	 *
	 * @created 06.01.2014
	 */
	public static KnowledgeBase getKnowledgeBase(Section<?> section) {
		if (section.get() instanceof TagHandlerType) {
			return D3webUtils.getKnowledgeBase(section.getArticle());
		}
		else {
			D3webCompiler compiler = getCompiler(section);
			return compiler == null ? null : compiler.getKnowledgeBase();
		}
	}

	/**
	 * Returns the current value of a specific {@link ValueObject} within the
	 * specified {@link Session}. If the value is currently being calculated,
	 * the method immediately returns with "null" as value, instead of waiting
	 * for the results of the current propagation.
	 *
	 * @param session the session to read the value from
	 * @param object  the object to read the value for
	 * @return the value of the value object
	 * @created 05.10.2012
	 */
	public static Value getValueNonBlocking(Session session, ValueObject object) {
		// variant 1: conservative
		// only access values if we are not in a propagation. This is very
		// secure to do, but shows no value at all, even if this value is
		// not propagated at all
		// if (session.getPropagationManager().isInPropagation()) return null;
		// synchronized (session) {
		// return session.getBlackboard().getValue((ValueObject) object);
		// }

		// variant 2: aggressive
		// access the value without synchronization but catch the expected error
		// if this access violates concurrency
		try {
			return session.getBlackboard().getValue(object);
		}
		catch (ConcurrentModificationException e) {
			return null;
		}
	}

	public static void setFindingSynchronized(Fact fact, Session session, UserContext context) {
		try {
			//noinspection SynchronizationOnLocalVariableOrMethodParameter
			synchronized (session) {
				// if the fact wants to set unknown as the value and the same fact already exists,
				// the fact gets retracted instead, allowing to take back an answer completely.
				Fact existingFact = session.getBlackboard().getValueFact(fact.getTerminologyObject(),
						PSMethodUserSelected.getInstance());
				if (Unknown.getInstance().equals(fact.getValue()) && fact.equals(existingFact)) {
					session.getBlackboard().removeValueFact(existingFact);
				}
				else {
					session.getBlackboard().addValueFact(fact);
				}
				session.touch();
			}
		}
		catch (SessionTerminatedException e) {
			Log.warning("Unable to set fact, because the current session is " +
					"terminated (possibly due to a detected propagation loop).", e);
		}
		if (context != null) {
			EventManager.getInstance().fireEvent(new FindingSetEvent(fact, session, context));
		}
	}

	/**
	 * Returns the current value of a specific {@link Solution} within the
	 * specified {@link Session}. If the value is currently being calculated,
	 * the method immediately returns with "null" as value, instead of waiting
	 * for the results of the current propagation.
	 *
	 * @param session  the session to read the value from
	 * @param solution the object to read the value for
	 * @return the value of the value object
	 * @created 05.10.2012
	 */
	public static Rating getRatingNonBlocking(Session session, Solution solution) {
		// variant 1: conservative
		// only access values if we are not in a propagation. This is very
		// secure to do, but shows no value at all, even if this value is
		// not propagated at all
		// if (session.getPropagationManager().isInPropagation()) return null;
		// synchronized (session) {
		// return session.getBlackboard().getRating(solution);
		// }

		// variant 2: aggressive
		// access the value without synchronization but catch the expected error
		// if this access violates concurrency
		try {
			return session.getBlackboard().getRating(solution);
		}
		catch (ConcurrentModificationException e) {
			return null;
		}

	}

	/**
	 * Returns all {@link Solution} instances, that hold the specified
	 * {@link Rating} in the specified session. If the value of any solution
	 * currently is being calculated, the method immediately returns with "null"
	 * instead of the list, not waiting for the results of the current
	 * propagation.
	 *
	 * @param session the session to read the current values from
	 * @param state   the Rating the diagnoses must have to be returned
	 * @return a list of diagnoses in this case that have the state 'state' or
	 * null if any of these solutions is currently being calculated
	 */
	public static List<Solution> getSolutionsNonBlocking(Session session, Rating.State state) {
		try {
			return session.getBlackboard().getSolutions(state);
		}
		catch (ConcurrentModificationException e) {
			return null;
		}
	}

	/**
	 * Returns all {@link Question} instances, that have been answered (or
	 * assigned with a value) in the specified session. If the value of any
	 * solution currently is being calculated, the method immediately returns
	 * with "null" instead of the list, not waiting for the results of the
	 * current propagation.
	 *
	 * @param session the session to read the current answers from
	 * @return a list of diagnoses in this case that have the state 'state' or
	 * null if any of these solutions is currently being calculated
	 */
	public static List<Question> getAnsweredQuestionsNonBlocking(Session session) {
		try {
			return session.getBlackboard().getAnsweredQuestions();
		}
		catch (ConcurrentModificationException e) {
			return null;
		}
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
			Log.warning("Cannot identify url for knowledgebase", e);
		}
		return url;
	}

	public static ResourceBundle getD3webBundle() {
		return ResourceBundle.getBundle("KnowWE_plugin_d3web_messages");
	}

	public static ResourceBundle getD3webBundle(HttpServletRequest request) {
		if (request == null) return D3webUtils.getD3webBundle();
		Locale.setDefault(Environment.getInstance().getWikiConnector().getLocale(request));
		return D3webUtils.getD3webBundle();
	}

	public static ResourceBundle getD3webBundle(UserContext user) {
		if (user == null) return getD3webBundle();
		Locale.setDefault(Environment.getInstance().getWikiConnector().getLocale(user.getRequest()));
		return getD3webBundle();
	}

	public static void handleLoopDetectionNotification(ArticleManager manager, UserContext context,
													   Session session) {
		LoopStatus loopStatus =
				LoopTerminator.getInstance().getLoopStatus(session);
		if (loopStatus.hasTerminated()) {
			String notificationId = generateNotificationId(session);
			Collection<TerminologyObject> loopObjects = loopStatus.getLoopObjects();
			String notificationText = getLoopNotificationText(manager, context, session,
					loopObjects);
			StandardNotification notification = new StandardNotification(
					notificationText, Message.Type.WARNING, notificationId);
			NotificationManager.addNotification(context, notification);
		}
	}

	private static String getLoopNotificationText(ArticleManager manager, UserContext user, Session session, Collection<TerminologyObject> loopObjects) {
		String kbName = session.getKnowledgeBase().getName();
		if (kbName == null) kbName = session.getKnowledgeBase().getId();

		String kbUrlLink = KnowWEUtils.getURLLinkToObjectInfoPage(new Identifier(kbName));
		kbName = "<a href=\"" + toAbsolutURL(kbUrlLink) + "\">" + kbName + "</a>";
		Collection<String> renderedObjects = new ArrayList<>(loopObjects.size());
		for (TerminologyObject loopObject : loopObjects) {

			String url = KnowWEUtils.getURLLinkToObjectInfoPage(new Identifier(loopObject.getName()));
			renderedObjects.add("<a href=\"" + toAbsolutURL(url) + "\">" + loopObject.getName()
					+ "</a>");
		}
		String notificationText = "Endless loop detected in knowledge base '"
				+ kbName
				+ "'. The following object"
				+ (loopObjects.size() == 1 ? " is" : "s are")
				+ " mainly involved in the loop: " +
				Strings.concat(",  ", renderedObjects) + ".";
		return notificationText;
	}

	private static String toAbsolutURL(String url) {
		return Environment.getInstance().getWikiConnector().getBaseUrl() + url;
	}

	public static void removedLoopDetectionNotification(UserContext context, Session session) {
		String notificationId = generateNotificationId(session);
		NotificationManager.removeNotification(context, notificationId);
	}

	private static String generateNotificationId(Session session) {
		return session.getId() + "_loop_detected";
	}

	/**
	 * Checks if the knowledge base is empty. A knowledge base is empty, if
	 * there are no knowledge slices. no questions and at most one solution
	 * (root solution).
	 *
	 * @created 19.04.2013
	 */
	public static boolean isEmpty(KnowledgeBase kb) {
		return kb == null
				|| (kb.getAllKnowledgeSlices().size() <= 1
				&& kb.getManager().getQContainers().size() <= 1
				&& kb.getManager().getQuestions().isEmpty()
				&& kb.getManager().getSolutions().size() <= 1);
	}

}
