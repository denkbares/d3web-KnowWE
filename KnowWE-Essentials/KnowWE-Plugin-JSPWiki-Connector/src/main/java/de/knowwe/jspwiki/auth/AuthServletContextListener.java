package de.knowwe.jspwiki.auth;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Alex Legler (denkbares GmbH)
 * @created 2024-01-22
 */
public class AuthServletContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// Override the classmapping file to load our own
		System.setProperty("jspwiki.custom.classmapping", "ini/knowwe-classmappings.xml");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}
}
