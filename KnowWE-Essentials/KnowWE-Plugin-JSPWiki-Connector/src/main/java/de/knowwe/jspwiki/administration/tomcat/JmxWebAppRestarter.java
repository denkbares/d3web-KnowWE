/*
 * Copyright (C) 2025 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.jspwiki.administration.tomcat;

import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.TreeSet;

import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class to restart the web app (mostly ChatGPT)
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 07.08.2025
 */
public class JmxWebAppRestarter {

	// 1. Logger für saubere Ausgaben initialisieren
	private static final Logger LOGGER = LoggerFactory.getLogger(JmxWebAppRestarter.class);

	/**
	 * Attempts to reload the web application identified by the given ServletContext.
	 * It will first try the 'WebModule' MBean strategy. If that fails, it will
	 * automatically fall back to the standard 'Context' MBean strategy.
	 *
	 * @param servletContext The ServletContext of the web application to reload.
	 */
	public static void reload(ServletContext servletContext) {
		LOGGER.info("Attempting to reload web application '{}' via JMX...", servletContext.getContextPath());

		// 2. Primäre Methode (für deine IntelliJ-Umgebung) versuchen
		if (tryWebModuleReload(servletContext)) {
			LOGGER.info("✅ Reload command sent successfully via 'WebModule' MBean.");
			return;
		}

		// 3. Fallback-Methode (für "nackten" Tomcat) versuchen
		LOGGER.warn("Primary reload method failed. Trying fallback 'Context' MBean strategy...");
		if (tryContextReload(servletContext)) {
			LOGGER.info("✅ Reload command sent successfully via fallback 'Context' MBean.");
			return;
		}

		LOGGER.error("❌ All implemented JMX reload methods failed. Could not restart the application.");
		LOGGER.error("Check the following existing JMX MBean operations for more information on how to restart.");
		listAllMBeanOperations();
	}

	/**
	 * Strategy 1: Tries to reload using the j2eeType=WebModule MBean.
	 */
	private static boolean tryWebModuleReload(ServletContext servletContext) {
		try {
			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
			// Hostname ist fast immer 'localhost', wenn man von innen zugreift.
			String host = "localhost";
			String contextPath = getCanonicalContextPath(servletContext);
			String nameProperty = String.format("//%s%s", host, contextPath);

			ObjectName objectName = new ObjectName(
					"Catalina:j2eeType=WebModule,name=" + nameProperty + ",J2EEApplication=none,J2EEServer=none"
			);

			return invokeReloadIfAvailable(mBeanServer, objectName);
		}
		catch (Exception e) {
			LOGGER.debug("Strategy 'WebModule' failed with an exception.", e);
			return false;
		}
	}

	/**
	 * Strategy 2: Tries to reload using the type=Context MBean.
	 */
	private static boolean tryContextReload(ServletContext servletContext) {
		try {
			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
			String host = "localhost";
			String contextPath = getCanonicalContextPath(servletContext);

			ObjectName objectName = new ObjectName(
					"Catalina:type=Context,host=" + host + ",context=" + contextPath
			);

			return invokeReloadIfAvailable(mBeanServer, objectName);
		}
		catch (Exception e) {
			LOGGER.debug("Strategy 'Context' failed with an exception.", e);
			return false;
		}
	}

	/**
	 * Checks if the MBean and its 'reload' operation exist, and if so, invokes it.
	 */
	private static boolean invokeReloadIfAvailable(MBeanServer server, ObjectName name) throws JMException {
		// 4. Prüfen, ob das MBean überhaupt existiert
		if (!server.isRegistered(name)) {
			LOGGER.warn("MBean not found: {}", name);
			return false;
		}

		// 5. Prüfen, ob die 'reload' Operation verfügbar ist
		boolean operationFound = false;
		MBeanInfo mBeanInfo = server.getMBeanInfo(name);
		for (MBeanOperationInfo opInfo : mBeanInfo.getOperations()) {
			if ("reload".equals(opInfo.getName())) {
				operationFound = true;
				break;
			}
		}

		if (!operationFound) {
			LOGGER.warn("MBean '{}' exists, but 'reload' operation is not available.", name);
			return false;
		}

		// 6. Wenn alles passt, die Operation aufrufen
		LOGGER.info("Found MBean and 'reload' operation. Invoking now: {}", name);
		server.invoke(name, "reload", null, null);
		return true;
	}

	/**
	 * Ensures the context path is "/" for the root context, as JMX expects.
	 */
	private static String getCanonicalContextPath(ServletContext servletContext) {
		String path = servletContext.getContextPath();
		return path.isEmpty() ? "/" : path;
	}

	/**
	 * Listet alle MBeans und deren Operationen auf der Systemkonsole auf.
	 */
	@SuppressWarnings("UseOfSystemOutOrSystemErr")
	public static void listAllMBeanOperations() {
		LOGGER.info("============================================================");
		LOGGER.info("======= STARTING JMX MBEAN OPERATION INSPECTION ========");
		LOGGER.info("============================================================");

		try {
			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

			// Alle MBean-Namen holen und für eine saubere Ausgabe sortieren
			Set<ObjectName> objectNames = new TreeSet<>(mBeanServer.queryNames(null, null));

			LOGGER.info("\nFound " + objectNames.size() + " MBeans in total.\n");

			for (ObjectName name : objectNames) {
				LOGGER.info("\n------------------------------------------------------------");
				LOGGER.info("MBEAN: " + name.toString());
				LOGGER.info("------------------------------------------------------------");

				try {
					MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(name);
					MBeanOperationInfo[] operations = mBeanInfo.getOperations();

					if (operations.length == 0) {
						LOGGER.info("  -> No operations available.");
					}
					else {
						for (MBeanOperationInfo op : operations) {
							LOGGER.info("  + OPERATION:   " + op.getName());
							LOGGER.info("    Description: " + op.getDescription());
							LOGGER.info("    Return Type: " + op.getReturnType());

							MBeanParameterInfo[] params = op.getSignature();
							if (params.length == 0) {
								LOGGER.info("    Parameters:  (none)");
							}
							else {
								StringBuilder paramsString = new StringBuilder();
								paramsString.append("    Parameters:  (");
								for (int i = 0; i < params.length; i++) {
									paramsString.append(params[i].getType()).append(" ").append(params[i].getName());
									if (i < params.length - 1) {
										paramsString.append(", ");
									}
								}
								paramsString.append(")");
								LOGGER.info(paramsString.toString());
							}
							LOGGER.info("\n"); // Leerzeile
						}
					}
				}
				catch (Exception e) {
					System.err.println("  -> Could not retrieve info for this MBean: " + e.getMessage());
				}
			}
		}
		catch (Exception e) {
			System.err.println("\n### A CRITICAL ERROR occurred while querying MBeans: ###");
			e.printStackTrace(System.err);
		}
		finally {
			LOGGER.info("============================================================");
			LOGGER.info("======== JMX MBEAN OPERATION INSPECTION FINISHED =========");
			LOGGER.info("============================================================");
		}
	}
}
