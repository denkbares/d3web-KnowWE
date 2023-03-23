package de.knowwe.event;

import javax.servlet.http.HttpSession;

import com.denkbares.events.Event;

/**
 * Is fired when an existing http session is about to be invalidated.
 */
public class HttpSessionDestroyedEvent implements Event {

	private final HttpSession session;

	public HttpSessionDestroyedEvent(HttpSession session) {
		this.session = session;
	}

	public HttpSession getSession() {
		return session;
	}

}
