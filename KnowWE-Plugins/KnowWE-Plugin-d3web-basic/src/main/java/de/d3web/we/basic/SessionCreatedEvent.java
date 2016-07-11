package de.d3web.we.basic;

import de.d3web.core.session.Session;
import com.denkbares.events.Event;
import de.knowwe.core.user.UserContext;

public class SessionCreatedEvent implements Event {

	private final Session session;
	private final UserContext context;

	public SessionCreatedEvent(Session session, UserContext context) {
		this.session = session;
		this.context = context;
	}

	public Session getSession() {
		return this.session;
	}

	public UserContext getContext() {
		return context;
	}
}
