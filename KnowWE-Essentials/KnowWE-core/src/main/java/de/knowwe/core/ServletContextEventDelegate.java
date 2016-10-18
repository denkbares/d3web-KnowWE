package de.knowwe.core;

import javax.servlet.ServletContextEvent;

import com.denkbares.events.EventManager;
import de.knowwe.event.ServletContextDestroyedEvent;

/**
 * Delegates servlet events to denkbares Event mechanism.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 18.10.16
 */
public class ServletContextEventDelegate implements javax.servlet.ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		// no event listeners will be registered at this point, so no point in firing and event
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		EventManager.getInstance().fireEvent(new ServletContextDestroyedEvent(servletContextEvent));
	}
}
