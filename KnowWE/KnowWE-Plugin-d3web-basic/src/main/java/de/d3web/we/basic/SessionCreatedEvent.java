package de.d3web.we.basic;

import de.d3web.core.session.Session;
import de.knowwe.core.event.Event;

public class SessionCreatedEvent extends Event {

	private final Session session;

	public SessionCreatedEvent(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return this.session;
	}
}
