package de.knowwe.event;

import javax.servlet.ServletContextEvent;

import com.denkbares.events.Event;

/**
 * Gets fired when the current servlet context gets destroyed, e.g. tomcat shutdown or redeploy of the webapp.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 18.10.16
 */
public class ServletContextDestroyedEvent implements Event {

	private final ServletContextEvent servletContextEvent;

	public ServletContextDestroyedEvent(ServletContextEvent servletContextEvent) {

		this.servletContextEvent = servletContextEvent;
	}

	public ServletContextEvent getServletContextEvent() {
		return servletContextEvent;
	}
}
