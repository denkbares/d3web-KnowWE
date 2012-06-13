package de.d3web.we.basic;

import de.d3web.core.session.Session;
import de.knowwe.core.event.Event;

public class SessionRemovedEvent extends Event {

	private final Session session;

	public SessionRemovedEvent(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return this.session;
	}
}
