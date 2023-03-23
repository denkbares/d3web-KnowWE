package de.knowwe.event;

import javax.servlet.http.HttpSession;

import com.denkbares.events.Event;

public class HttpSessionDestroyedEvent implements Event {

	private final HttpSession session;

	public HttpSessionDestroyedEvent(HttpSession session) {
		this.session = session;
	}

	public HttpSession getSession() {
		return session;
	}

}
