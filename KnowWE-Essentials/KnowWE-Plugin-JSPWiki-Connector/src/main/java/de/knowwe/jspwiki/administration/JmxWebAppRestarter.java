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

package de.knowwe.jspwiki.administration;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.ServletContext;

import com.denkbares.strings.Strings;

/**
 * Util class to restart the web app
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 07.08.2025
 */
public class JmxWebAppRestarter {

	public static void reload(ServletContext servletContext) throws MalformedObjectNameException, ReflectionException, InstanceNotFoundException, MBeanException {
		// 1. MBean-Server holen
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

		// 2. Den eindeutigen Namen (ObjectName) für die eigene Web-App erstellen
		// Das Muster ist Catalina:type=Context,host=<hostname>,path=<contextpath>
		String hostName = "localhost"; // Anpassen, falls in server.xml anders konfiguriert
		String contextPath = servletContext.getContextPath();

		// Wenn die App die Root-Anwendung ist, ist der Pfad leer. JMX benötigt aber "/".
		if (Strings.isBlank(contextPath)) {
			contextPath = "/";
		}

		ObjectName contextObjectName = new ObjectName("Catalina:type=Context,host=" + hostName + ",path=" + contextPath);

		// 3. Die 'reload'-Operation auf dem MBean aufrufen
		mBeanServer.invoke(contextObjectName, "reload", null, null);
	}
}
