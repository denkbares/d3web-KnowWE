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

import javax.servlet.http.HttpSession;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.knowwe.core.KnowWEAttributes;
import de.knowwe.core.user.UserContext;

/**
 * This class stores all {@link Sessions}. The class itself is stored in each
 * user's {@link HTTPSession} and is accessible by calling:
 * 
 * <pre>
 * httpSession.getAttribute(KnowWEAttributes.SESSIONPROVIDER)
 * </pre>
 * 
 * on a {@link HTTPSession} object. For convenience there exist two static
 * utility methods:
 * 
 * <pre>
 * SessionProvider.getSessionProvider(UserContext context)
 * </pre>
 * 
 * and
 * 
 * <pre>
 * SessionProvider.getSession(UserContext context, KnowledgeBase base)
 * </pre>
 * 
 * which does all the dirty work for you by using KnowWE's {@link UserContext}.
 * 
 * @author Sebastian Furth (denkbares GmbH)
 * @created 06.03.2012
 */
public class SessionProvider {

	private Map<String, Session> sessions;

	/**
	 * Returns the SessionProvider object for a specified {@link UserContext}.
	 * If there is no SessionProvider object, a new one will be created and
	 * stored in the provided UserContext, i. e. HTTPSession.
	 * 
	 * Please be aware that this method only works with an UserContext backed by
	 * a real HTTPSession. Otherwise there is no place to store and retrieve the
	 * SessionProvider object.
	 * 
	 * @created 06.03.2012
	 * @param context UserContext of the current user.
	 * @return SessionProvider object associated to the user
	 */
	public static SessionProvider getSessionProvider(UserContext context) {
		HttpSession httpSession = context.getSession();
		SessionProvider provider = null;
		if (httpSession != null) {
			provider = (SessionProvider) httpSession.getAttribute(KnowWEAttributes.SESSIONPROVIDER);
			if (provider == null) {
				provider = new SessionProvider();
				context.getSession().setAttribute(KnowWEAttributes.SESSIONPROVIDER, provider);
			}
		}
		return provider;
	}

	/**
	 * Returns the {@link Session} for a specified {@link UserContext} and a
	 * {@link KnowledgeBase}. This methods tries to load an existing
	 * SessionProvider object from the user's HTTPSession. If there is no
	 * SessionProvider object, a new one will be created and stored in the
	 * provided UserContext, i. e. HTTPSession.
	 * 
	 * Please be aware that this method only works with an UserContext backed by
	 * a real HTTPSession. Otherwise there is no place to store and retrieve the
	 * SessionProvider object.
	 * 
	 * @created 07.03.2012
	 * @param context UserContext of the current user.
	 * @param base the underlying knowledge base
	 * @return Session for the specified knowledge base
	 */
	public static Session getSession(UserContext context, KnowledgeBase base) {
		SessionProvider provider = getSessionProvider(context);
		if (provider == null) {
			return null;
		}
		return provider.getSession(base);
	}

	public SessionProvider() {
		sessions = new HashMap<String, Session>();
	}

	/**
	 * Returns all {@link Session} objects of this provider.
	 * 
	 * @created 06.03.2012
	 * @return all sessions
	 */
	public Collection<Session> getSessions() {
		return Collections.unmodifiableCollection(sessions.values());
	}

	/**
	 * Returns an existing {@link Session} for the provided knowledge base. If
	 * there exists no session for this knowledge base this method will create
	 * one.
	 * 
	 * @created 06.03.2012
	 * @param kb the underlying knowledge base
	 * @return session for the specified knowledge base
	 */
	public Session getSession(KnowledgeBase kb) {
		Session session = sessions.get(kb.getId());
		if (session == null) {
			session = createSession(kb);
		}
		return session;
	}

	/**
	 * Creates and returns a new {@link Session} for the {@link KnowledgeBase}.
	 * The created session is accessible by using the id of the knowledge base.
	 * 
	 * @created 06.03.2012
	 * @param kb The underlying knowledge base
	 * @return the created session
	 */
	public Session createSession(KnowledgeBase kb) {
		Session session = SessionFactory.createSession(kb);
		sessions.put(kb.getId(), session);
		return session;
	}

	/**
	 * Removes an existing {@link Session} for the provided knowledge base. If
	 * there exists no session for this knowledge base ID this method will do
	 * nothing.
	 * 
	 * @created 06.03.2012
	 * @param kb the underlying knowledge base
	 */
	public void removeSession(KnowledgeBase kb) {
		sessions.remove(kb.getId());
	}

	/**
	 * Sets the session to the specified session by using the id of the
	 * session's underlying kb. An existing session will be overwritten!
	 * 
	 * @created 06.03.2012
	 * @param session the session to be set
	 */
	public void setSession(Session session) {
		sessions.put(session.getKnowledgeBase().getId(), session);
	}

}
