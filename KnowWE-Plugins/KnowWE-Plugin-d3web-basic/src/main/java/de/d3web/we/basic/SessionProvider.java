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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.jetbrains.annotations.Nullable;

import com.denkbares.events.EventManager;
import de.d3web.core.inference.PSMethod.Type;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.Attributes;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.user.UserContext;

/**
 * This class stores all {@link Session}s. The class itself is stored in each user's HTTPSession and is accessible by
 * calling:
 * <p>
 * <pre>
 * httpSession.getAttribute(Attributes.SESSIONPROVIDER)
 * </pre>
 * <p>
 * on a HTTPSession object. For convenience there exist two static utility methods:
 * <p>
 * <pre>
 * SessionProvider.getSessionProvider(UserContext context)
 * </pre>
 * <p>
 * and
 * <p>
 * <pre>
 * SessionProvider.getSession(UserContext context, KnowledgeBase base)
 * </pre>
 * <p>
 * which does all the dirty work for you by using KnowWE's {@link UserContext}.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created 06.03.2012
 */
public class SessionProvider {

	private final Map<String, SessionEntry> sessions = new HashMap<>();

	private SessionProvider() {
	}

	/**
	 * Returns the SessionProvider object for a specified {@link UserContext}. If there is no SessionProvider object, a
	 * new one will be created and stored in the provided UserContext, i. e. HTTPSession.
	 * <p>
	 * Please be aware that this method only works with an UserContext backed by a real HTTPSession. Otherwise there is
	 * no place to store and retrieve the SessionProvider object.
	 *
	 * @param context UserContext of the current user.
	 * @return SessionProvider object associated to the user
	 * @created 06.03.2012
	 */
	private static SessionProvider getSessionProvider(UserContext context) {
		HttpSession httpSession = context.getSession();
		return getSessionProvider(httpSession);
	}

	/**
	 * Returns the SessionProvider object for a specified http session. If there is no SessionProvider object, a new one
	 * will be created and stored in the provided http session.
	 *
	 * @param httpSession HttpSession of the current user.
	 * @return SessionProvider object associated to the user
	 * @created 06.03.2012
	 */
	public static SessionProvider getSessionProvider(HttpSession httpSession) {
		SessionProvider provider = null;
		if (httpSession != null) {
			provider = (SessionProvider) httpSession.getAttribute(Attributes.SESSIONPROVIDER);
			if (provider == null) {
				provider = new SessionProvider();
				httpSession.setAttribute(Attributes.SESSIONPROVIDER, provider);
			}
		}
		return provider;
	}

	/**
	 * Return an existing session for the given user context and knowledge base. If, for the given knowledge base, we
	 * don't yet have a session, we don't create one, but return null.
	 *
	 * @param context       the user context for which we want the session
	 * @param knowledgeBase the knowledgeBase for which we want a session
	 * @return an existing session for the given context and knowledge base or null, if there is non
	 */
	@Nullable
	public static Session getExistingSession(UserContext context, KnowledgeBase knowledgeBase) {
		if (hasSession(context, knowledgeBase)) {
			return getSession(context, knowledgeBase);
		}
		return null;
	}

	/**
	 * Marks the specified session to be used, so that it will remain until the user actively resets the session. If the
	 * session is currently not (or no longer) provided by this session provider, the method does nothing.
	 *
	 * @param session the session to be pinned, if it is actively provided by this session provider
	 */
	public void pinSession(Session session) {
		SessionEntry entry = sessions.get(session.getKnowledgeBase().getId());
		if (entry != null && entry.session == session) {
			entry.knownToBeUsed = true;
		}
	}

	private Session createSessionInternally(UserContext context, KnowledgeBase kb) {
		removeSessionInternally(context, kb, true);
		Session session = SessionFactory.createSession(kb);
		sessions.put(kb.getId(), new SessionEntry(session));
		EventManager.getInstance().fireEvent(new SessionCreatedEvent(session, context));
		return session;
	}

	/**
	 * Returns an existing {@link Session} for the provided knowledge base. If there exists no session for this
	 * knowledge base this method will create one. If the knowledge base of an existing session is not up to date and no
	 * user facts has been set, the knowledge base will be replaced automatically (the session will be reset).
	 *
	 * @param kb the underlying knowledge base
	 * @return session for the specified knowledge base
	 * @created 06.03.2012
	 */
	private Session getSessionInternally(UserContext context, KnowledgeBase kb) {
		// check if there is no entry, so create and return
		SessionEntry entry = sessions.get(kb.getId());
		if (entry == null) {
			return createSessionInternally(context, kb);
		}

		// check if existing session's knowledge base is outdated, and session not yet used:
		if (entry.session.getKnowledgeBase() != kb && !entry.isActivelyUsed()) {
			// session is no used -> silently reset the session
			removeSessionInternally(context, kb, true);
			return createSessionInternally(context, kb);
		}

		// otherwise, continue with the existing session
		return entry.session;
	}

	private void removeSessionInternally(UserContext context, KnowledgeBase kb, boolean terminate) {
		SessionEntry removedEntry = sessions.remove(kb.getId());
		if (removedEntry != null) {
			EventManager.getInstance().fireEvent(new SessionRemovedEvent(removedEntry.session, context));
			if (terminate) removedEntry.session.getPropagationManager().terminate();
		}
	}

	private void setSessionInternally(UserContext context, Session session) {
		String key = session.getKnowledgeBase().getId();
		SessionEntry entry = sessions.get(key);

		// check if we already have an entry of that session, so do nothing
		if (entry != null && entry.session == session) return;

		// if there was an existing session before,
		// remove existing one (which will also terminate the session), and create a new one
		if (entry != null) {
			removeSessionInternally(context, session.getKnowledgeBase(), true);
		}

		// the create a new session entry for the session
		entry = new SessionEntry(session);
		sessions.put(key, entry);
		EventManager.getInstance().fireEvent(new SessionCreatedEvent(session, context));
	}

	/**
	 * Creates and returns a new {@link Session} for the {@link KnowledgeBase}. The created session is accessible by
	 * using the id of the knowledge base.
	 *
	 * @param context the {@link UserContext} the session will belong to
	 * @param kb      The underlying knowledge base
	 * @return the created session
	 * @created 06.03.2012
	 */
	public static Session createSession(UserContext context, KnowledgeBase kb) {
		SessionProvider sessionProvider = getSessionProvider(context);
		if (sessionProvider == null) {
			return null;
		}
		return sessionProvider.createSessionInternally(context, kb);
	}

	/**
	 * Returns the {@link Session} for a specified {@link UserContext} and a {@link KnowledgeBase}. This methods tries
	 * to load an existing SessionProvider object from the user's HTTPSession. If there is no SessionProvider object, a
	 * new one will be created and stored in the provided UserContext, i. e. HTTPSession. If the knowledge base of an
	 * existing session is not up to date and no user facts has been set, the knowledge base will be replaced
	 * automatically (the session will be reset).
	 * <p>
	 * Please be aware that this method only works with an UserContext backed by a real HTTPSession. Otherwise there is
	 * no place to store and retrieve the SessionProvider object.
	 *
	 * @param context UserContext of the current user.
	 * @param base    the underlying knowledge base
	 * @return Session for the specified knowledge base
	 * @created 07.03.2012
	 */
	public static Session getSession(UserContext context, KnowledgeBase base) {
		SessionProvider provider = getSessionProvider(context);
		if (provider == null) {
			return null;
		}
		return provider.getSessionInternally(context, base);
	}

	/**
	 * Returns if a {@link Session} for a specified {@link UserContext} and a {@link KnowledgeBase} has already been
	 * created. This methods tries to load an existing SessionProvider object from the user's HTTPSession. If there is
	 * no SessionProvider object, a new one will be created and stored in the provided UserContext, i. e. HTTPSession.
	 * <p>
	 * Please be aware that this method only works with an UserContext backed by a real HTTPSession. Otherwise there is
	 * no place to store and retrieve the SessionProvider object.
	 *
	 * @param context UserContext of the current user.
	 * @param base    the underlying knowledge base
	 * @return Session for the specified knowledge base
	 * @created 05.10.2015
	 */
	public static boolean hasSession(UserContext context, KnowledgeBase base) {
		SessionProvider provider = getSessionProvider(context);
		if (provider == null) {
			return false;
		}
		SessionEntry entry = provider.sessions.get(base.getId());
		//noinspection SimplifiableIfStatement
		if (entry == null) {
			return false;
		}
		// check if existing session is up to date
		return entry.session.getKnowledgeBase().getId().equals(base.getId());
	}

	/**
	 * Returns a Collection of all sessions currently stored for the user.
	 *
	 * @param context the user context to get the sessions for
	 * @return all sessions of the user
	 * @created 17.08.2012
	 */
	public static Collection<Session> getSessions(UserContext context) {
		SessionProvider provider = getSessionProvider(context);
		if (provider == null) {
			return Collections.emptyList();
		}
		return provider.sessions.values().stream().map(entry -> entry.session).collect(Collectors.toList());
	}

	/**
	 * Checks whether the current {@link Session} uses an out dated {@link KnowledgeBase}.
	 *
	 * @created 22.03.2012
	 */
	public static boolean hasOutDatedSession(UserContext context, KnowledgeBase base) {
		Session session = getSession(context, base);
		return (session == null) || (session.getKnowledgeBase() != base);
	}

	/**
	 * Removes an existing {@link Session} for the provided knowledge base. If there exists no session for this
	 * knowledge base ID this method will do nothing. The removed session is also terminated, so it cannot be used later
	 * accidentally.
	 *
	 * @param context the {@link UserContext} to which the session belongs
	 * @param kb      the underlying knowledge base
	 * @created 06.03.2012
	 */
	public static void removeSession(UserContext context, KnowledgeBase kb) {
		removeSession(context, kb, true);
	}

	/**
	 * Removes an existing {@link Session} for the provided knowledge base. If there exists no session for this
	 * knowledge base ID this method will do nothing.
	 *
	 * @param context   the {@link UserContext} to which the session belongs
	 * @param kb        the underlying knowledge base
	 * @param terminate a boolean to decide whether the removed session should also be terminated or not
	 * @created 06.03.2012
	 */
	public static void removeSession(UserContext context, KnowledgeBase kb, boolean terminate) {
		SessionProvider sessionProvider = getSessionProvider(context);
		if (sessionProvider != null) {
			sessionProvider.removeSessionInternally(context, kb, terminate);
		}
	}

	/**
	 * Sets the session to the specified session by using the id of the session's underlying kb. An existing session
	 * will be overwritten!
	 *
	 * @param context the {@link UserContext} for which the session should be set
	 * @param session the session to be set
	 * @created 06.03.2012
	 */
	public static void setSession(UserContext context, Session session) {
		SessionProvider sessionProvider = getSessionProvider(context);
		if (sessionProvider != null) {
			sessionProvider.setSessionInternally(context, session);
		}
	}

	/**
	 * Marks the specified session to be used, so that it will remain until the user actively resets the session. If the
	 * session is currently not (or no longer) provided by the user's session provider, the method does nothing.
	 *
	 * @param context the {@link UserContext} for which the session should be pinneed
	 * @param session the session to be pinned, if it is actively provided by the user's session provider
	 */
	public static void pinSession(UserContext context, Session session) {
		SessionProvider sessionProvider = getSessionProvider(context);
		if (sessionProvider != null) {
			sessionProvider.pinSession(session);
			// also pin knowledge base in compiler providing the knowledge base, so it does not change
			// while using the session
			for (D3webCompiler compiler : Compilers.getCompilers(context, context.getArticleManager(), D3webCompiler.class)) {
				if (compiler.getKnowledgeBase() == session.getKnowledgeBase()) {
					compiler.pinKnowledgeBase();
				}
			}
		}
	}

	private static final class SessionEntry {
		private final Session session;
		private boolean knownToBeUsed = false;

		public SessionEntry(Session session) {
			this.session = session;
		}

		/**
		 * Returns true if the session is already actively used by the user.
		 */
		public boolean isActivelyUsed() {
			if (knownToBeUsed) return true;

			// check if the session is empty
			for (TerminologyObject t : session.getBlackboard().getValuedObjects()) {
				Fact fact = session.getBlackboard().getValueFact(t);
				if (fact != null && fact.getPSMethod() != null && fact.getPSMethod().hasType(Type.source)) {
					// session is not empty -> known to be used
					knownToBeUsed = true;
					return true;
				}
			}
			for (TerminologyObject t : session.getBlackboard().getInterviewObjects()) {
				Fact fact = session.getBlackboard().getInterviewFact(t);
				if (fact != null && fact.getPSMethod() != null && fact.getPSMethod().hasType(Type.source)) {
					// session is not empty -> known to be used
					knownToBeUsed = true;
					return true;
				}
			}

			return false;
		}
	}
}
