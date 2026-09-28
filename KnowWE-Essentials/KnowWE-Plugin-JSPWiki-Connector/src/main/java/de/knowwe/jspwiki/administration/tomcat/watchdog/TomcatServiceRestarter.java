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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.OS;
import de.knowwe.core.ServletContextEventListener;

/**
 * Utility class to interact with the external Tomcat restart watchdog.
 * It can create the restart flag file to trigger a restart, and check
 * if the watchdog is currently active based on the log file timestamp.
 * <p>
 * The watchdog script must run independently and monitor the flag file.
 * After a restart was signaled, a monitor thread checks whether the watchdog
 * picks up the flag and whether Tomcat actually shuts down afterwards. If not,
 * the problem is logged together with the tail of the watchdog log, so it can
 * be analyzed from the wiki logs without access to the server.
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

	// the watchdog polls every 10 seconds, so it should have picked up the flag well within this time
	private static final long PICK_UP_TIMEOUT_SECONDS = 60;
	// time Tomcat gets to start shutting down after the watchdog removed the flag
	private static final long SHUTDOWN_TIMEOUT_SECONDS = 120;
	private static final long MONITOR_INTERVAL_MILLIS = 2000;
	private static final int WATCHDOG_LOG_TAIL_LINES = 20;

	public enum RestartState {
		/**
		 * No restart was requested in this JVM
		 */
		IDLE,
		/**
		 * The flag was written, waiting for the watchdog to pick it up
		 */
		REQUESTED,
		/**
		 * The watchdog removed the flag, waiting for Tomcat to shut down
		 */
		RESTARTING,
		/**
		 * The watchdog did not pick up the flag in time
		 */
		NOT_PICKED_UP,
		/**
		 * The watchdog removed the flag, but Tomcat did not shut down
		 */
		NOT_RESTARTED;

		public boolean isPending() {
			return this == REQUESTED || this == RESTARTING;
		}
	}

	public record RestartStatus(@NotNull RestartState state, @NotNull String message) {
	}

	private static volatile RestartStatus status = new RestartStatus(RestartState.IDLE, "No restart requested.");
	private static volatile boolean shutdownStarted = false;
	private static boolean shutdownListenerRegistered = false;

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
	 * Creates the restart flag file that signals the watchdog to restart Tomcat and starts monitoring whether the
	 * restart actually happens. If a restart is already pending, nothing is done and the current status is returned.
	 *
	 * @param user the name of the user requesting the restart, only used for logging
	 * @return the status of the restart after signaling
	 * @throws IOException if the file cannot be written.
	 */
	public static synchronized RestartStatus signalRestart(String user) throws IOException {
		if (status.state().isPending()) {
			LOGGER.info("Tomcat restart requested by {}, but a restart is already pending: {}", user, status.message());
			return status;
		}

		File flag = new File(FLAG_PATH);
		if (flag.exists()) {
			// we are not monitoring a restart, so this flag is left over, e.g. from a previous failed attempt
			LOGGER.warn("Tomcat restart requested by {}, but restart flag {} already exists since {} s. " +
						"The watchdog apparently did not process it, waiting for it again.",
					user, FLAG_PATH, getAgeSeconds(flag));
		}
		else {
			File parent = flag.getParentFile();
			if (!parent.exists() && !parent.mkdirs()) {
				throw new IOException("Cannot create directory: " + parent);
			}
			Files.createFile(flag.toPath());
			LOGGER.info("Tomcat restart requested by {}, created restart flag {} for the watchdog", user, FLAG_PATH);
		}

		registerShutdownListener();
		status = new RestartStatus(RestartState.REQUESTED, "Restart requested, waiting for the watchdog to pick it up.");
		Thread monitor = new Thread(TomcatServiceRestarter::monitorRestart, "Tomcat-Restart-Monitor");
		monitor.setDaemon(true);
		monitor.start();
		return status;
	}

	/**
	 * Returns the status of the last restart requested in this JVM.
	 */
	public static RestartStatus getRestartStatus() {
		return status;
	}

	private static synchronized void registerShutdownListener() {
		if (shutdownListenerRegistered) return;
		ServletContextEventListener.registerOnContextDestroyedTask(event -> shutdownStarted = true);
		shutdownListenerRegistered = true;
	}

	private static void monitorRestart() {
		File flag = new File(FLAG_PATH);
		try {
			long start = System.currentTimeMillis();
			while (flag.exists() && !shutdownStarted && secondsSince(start) < PICK_UP_TIMEOUT_SECONDS) {
				Thread.sleep(MONITOR_INTERVAL_MILLIS);
			}
			if (shutdownStarted) return;
			if (flag.exists()) {
				String message = "The watchdog did not process the restart flag within " + PICK_UP_TIMEOUT_SECONDS + " s. " +
								 "Tomcat was not restarted. " + describeWatchdogState();
				LOGGER.error("{}\nRemoving restart flag {}, so it does not trigger an unexpected restart later.\n{}",
						message, FLAG_PATH, getWatchdogLogTail());
				Files.deleteIfExists(flag.toPath());
				status = new RestartStatus(RestartState.NOT_PICKED_UP, message);
				return;
			}

			LOGGER.info("The watchdog picked up the restart flag after {} s, waiting for Tomcat to shut down",
					secondsSince(start));
			status = new RestartStatus(RestartState.RESTARTING, "The watchdog picked up the restart request, waiting for Tomcat to shut down.");
			long pickedUp = System.currentTimeMillis();
			while (!shutdownStarted && secondsSince(pickedUp) < SHUTDOWN_TIMEOUT_SECONDS) {
				Thread.sleep(MONITOR_INTERVAL_MILLIS);
			}
			if (shutdownStarted) return;
			String message = "The watchdog removed the restart flag, but Tomcat did not shut down within "
							 + SHUTDOWN_TIMEOUT_SECONDS + " s. The restart most likely failed, see " + LOG_PATH
							 + " on the server (e.g. wrong service name or missing permission to restart the service).";
			LOGGER.error("{}\n{}", message, getWatchdogLogTail());
			status = new RestartStatus(RestartState.NOT_RESTARTED, message);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		catch (Exception e) {
			LOGGER.error("Exception while monitoring Tomcat restart", e);
			status = new RestartStatus(RestartState.NOT_RESTARTED, "Exception while monitoring Tomcat restart: " + e);
		}
	}

	private static String describeWatchdogState() {
		File alive = new File(ALIVE_PATH);
		if (!alive.exists()) {
			return "The watchdog heartbeat file " + ALIVE_PATH + " does not exist.";
		}
		return "The watchdog heartbeat file " + ALIVE_PATH + " was last updated " + getAgeSeconds(alive) + " s ago.";
	}

	private static String getWatchdogLogTail() {
		File log = new File(LOG_PATH);
		if (!log.exists()) {
			return "Watchdog log " + LOG_PATH + " does not exist.";
		}
		try {
			// the watchdog writes UTF-8, invalid bytes (e.g. from older ANSI logs) are replaced instead of failing
			String content = new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8);
			List<String> lines = Arrays.asList(content.split("\\R"));
			List<String> tail = lines.subList(Math.max(0, lines.size() - WATCHDOG_LOG_TAIL_LINES), lines.size());
			return "Last " + tail.size() + " lines of watchdog log " + LOG_PATH + ":\n" + String.join("\n", tail);
		}
		catch (IOException e) {
			return "Cannot read watchdog log " + LOG_PATH + ": " + e;
		}
	}

	private static long getAgeSeconds(File file) {
		return secondsSince(file.lastModified());
	}

	private static long secondsSince(long millis) {
		return (System.currentTimeMillis() - millis) / 1000;
	}

	/**
	 * Check if alive file is updated and log file exists
	 */
	public static boolean isWatchdogHealthy() {
		return getWatchdogProblem() == null;
	}

	/**
	 * Returns a description of why the watchdog is currently not usable, or null if it is healthy.
	 */
	public static @Nullable String getWatchdogProblem() {
		File alive = new File(ALIVE_PATH);
		if (!alive.exists()) {
			return "The watchdog heartbeat file " + ALIVE_PATH + " does not exist, the watchdog is probably not installed or not running.";
		}
		if (!isWatchdogActive()) {
			return "The watchdog heartbeat file " + ALIVE_PATH + " was last updated " + getAgeSeconds(alive) + " s ago, the watchdog is probably not running.";
		}
		if (!new File(LOG_PATH).exists()) {
			return "The watchdog log file " + LOG_PATH + " does not exist.";
		}
		if (!canWriteFlag()) {
			return "Cannot write to " + SERVICE_DIR + ", check the permissions of the Tomcat service account.";
		}
		return null;
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
			LOGGER.error("Cannot create test flag: {}", testFlag, e);
			return false;
		}
		return true;
	}
}
