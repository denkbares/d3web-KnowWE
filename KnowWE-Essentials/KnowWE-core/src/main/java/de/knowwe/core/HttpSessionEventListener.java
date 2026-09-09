package de.knowwe.core;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.denkbares.events.Event;
import de.knowwe.event.HttpSessionCreatedEvent;
import de.knowwe.event.HttpSessionDestroyedEvent;

/**
 * Listens to http session events and delegates them to the denkbares event mechanism.
 */
public class HttpSessionEventListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		fire(se, new HttpSessionCreatedEvent(se.getSession()));
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		fire(se, new HttpSessionDestroyedEvent(se.getSession()));
	}

	private static void fire(HttpSessionEvent se, Event event) {
		// sessions may exist before the wiki (and with it the environment) is initialized; nobody listens then
		Environment environment = Environment.findIn(se.getSession().getServletContext());
		if (environment != null) environment.getEventManager().fireEvent(event);
	}

}
