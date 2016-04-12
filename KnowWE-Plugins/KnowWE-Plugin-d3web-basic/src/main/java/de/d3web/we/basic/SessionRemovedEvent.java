package de.d3web.we.basic;

import de.d3web.core.session.Session;
import de.knowwe.core.event.Event;
import de.knowwe.core.user.UserContext;

public class SessionRemovedEvent extends Event {

	private final Session session;
	private final UserContext context;

	public SessionRemovedEvent(Session session, UserContext context) {
		this.context = context;
		this.session = session;
	}

	public Session getSession() {
		return this.session;
	}

	public UserContext getContext() {
		return context;
	}
}
