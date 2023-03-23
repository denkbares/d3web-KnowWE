package de.knowwe.core;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.denkbares.events.Event;
import com.denkbares.events.EventManager;
import de.knowwe.event.HttpSessionCreatedEvent;
import de.knowwe.event.HttpSessionDestroyedEvent;

/**
 * Listens to http session events and delegates them to the denkbares event mechanism.
 */
public class HttpSessionEventListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		Event event = new HttpSessionCreatedEvent(session);
		EventManager.getInstance().fireEvent(event);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		Event event = new HttpSessionDestroyedEvent(session);
		EventManager.getInstance().fireEvent(event);
	}

}
