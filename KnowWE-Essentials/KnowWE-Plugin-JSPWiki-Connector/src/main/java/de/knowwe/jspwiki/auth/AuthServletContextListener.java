package de.knowwe.jspwiki.auth;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.wiki.util.ClassUtil;

/**
 * @author Alex Legler (denkbares GmbH)
 * @created 2024-01-22
 */
public class AuthServletContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// Make sure our own classmapping file is set
		String mappingProp = System.getProperty(ClassUtil.CUSTOM_MAPPINGS, "ini/knowwe-classmappings.xml");
		System.setProperty(ClassUtil.CUSTOM_MAPPINGS, mappingProp);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}
}
