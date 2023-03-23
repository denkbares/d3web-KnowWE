package de.knowwe.event;

import javax.servlet.http.HttpSession;

import com.denkbares.events.Event;

public class HttpSessionCreatedEvent implements Event {

	private final HttpSession session;

	public HttpSessionCreatedEvent(HttpSession session) {
		this.session = session;
	}

	public HttpSession getSession() {
		return session;
	}
}
