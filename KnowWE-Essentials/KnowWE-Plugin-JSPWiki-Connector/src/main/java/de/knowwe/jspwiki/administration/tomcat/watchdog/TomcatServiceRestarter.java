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

package de.knowwe.jspwiki.administration.tomcat.watchdog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.OS;

/**
 * Utility class to interact with the external Tomcat restart watchdog.
 * It can create the restart flag file to trigger a restart, and check
 * if the watchdog is currently active based on the log file timestamp.
 * <p>
 * The watchdog script must run independently and monitor the flag file.
 *
 * @author Albrecht Striffler (denkbares GmbH), ChatGPT
 * @created 20.10.2025
 */
public class TomcatServiceRestarter {

	private static final Logger LOGGER = LoggerFactory.getLogger(TomcatServiceRestarter.class);

	// Resolve service directory from environment, fallback to default
	private static final String SERVICE_DIR = resolveServiceDir();
	// --- Configuration paths (adjust as needed) ---
	private static final String FLAG_PATH = SERVICE_DIR + "restart.flag";
	private static final String FLAG_TEST_PATH = SERVICE_DIR + "restart-test.flag";
	private static final String LOG_PATH = SERVICE_DIR + "watchdog.log";

	// If alive file not updated for more than X minutes, assume watchdog is inactive
	private static final String ALIVE_PATH = SERVICE_DIR + "watchdog.alive";
	private static final long MAX_ALIVE_AGE_SECONDS = 60;

	private static String resolveServiceDir() {
		// Prefer environment variable "knowwe.tomcat.restart.dir", else default
		String dir = System.getProperty("knowwe.tomcat.restart.dir", OS.WINDOWS.isCurrentOS() ? "C:\\tomcat-restart\\" : "/knowwe/tomcat-restart/");
		// Normalize to end with a separator/backslash for concatenation below
		if (!dir.endsWith("/") && !dir.endsWith("\\")) {
			dir += File.separator;
		}
		return dir;
	}

	/**
	 * Creates the restart flag file that signals the watchdog to restart Tomcat.
	 *
	 * @return true if the flag was created successfully, false if it already exists.
	 * @throws IOException if the file cannot be written.
	 */
	public static boolean signalRestart() throws IOException {
		File flag = new File(FLAG_PATH);
		if (flag.exists()) {
			return false; // Already requested, watchdog will pick it up
		}
		File parent = flag.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new IOException("Cannot create directory: " + parent);
		}
		Files.createFile(flag.toPath());
		return true;
	}

	/**
	 * Check if alive file is updated and log file exists
	 */
	public static boolean isWatchdogHealthy() {
		boolean aliveRecent = isWatchdogActive();
		boolean logExists = new File(LOG_PATH).exists();
		boolean canWrite = canWriteFlag();
		return aliveRecent && logExists && canWrite;
	}

	/**
	 * Check if watchdog updates alive file
	 */
	public static boolean isWatchdogActive() {
		File alive = new File(ALIVE_PATH);
		if (!alive.exists()) {
			return false;
		}
		long ageMillis = System.currentTimeMillis() - alive.lastModified();
		return ageMillis < (MAX_ALIVE_AGE_SECONDS * 1000);
	}

	public static boolean canWriteFlag() {
		File testFlag = new File(FLAG_TEST_PATH);
		File parent = testFlag.getParentFile();
		if (parent == null || (!parent.exists() && !parent.mkdirs())) {
			LOGGER.error("Cannot create directory: {}", parent);
			return false;
		}
		try {
			Files.createFile(testFlag.toPath());
			Files.deleteIfExists(testFlag.toPath());
		}
		catch (IOException e) {
			LOGGER.error("Cannot create test flag: {}", testFlag);
			return false;
		}
		return true;
	}
}