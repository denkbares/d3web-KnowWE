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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
 * <p>
 * The wiki writes its process id to {@code tomcat.pid} (and into the flag), so the watchdog can find the Tomcat
 * service without a configured service name. The watchdog reports the state of that service in the second line of
 * {@code watchdog.alive}, which is checked at startup and shown at the restart tool.
 * <p>
 * Every wiki using the directory registers itself with a heartbeat file in {@code instances/}. If several running
 * Tomcats share the directory, a restart could hit the wrong one (and the pid and service state only describe one of
 * them), so the restart is disabled with a warning instead.
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
	private static final String PID_PATH = SERVICE_DIR + "tomcat.pid";

	// If alive file not updated for more than X minutes, assume watchdog is inactive
	private static final String ALIVE_PATH = SERVICE_DIR + "watchdog.alive";
	private static final long MAX_ALIVE_AGE_SECONDS = 60;

	// the watchdog polls every 10 seconds, so it should have picked up the flag well within this time
	private static final long PICK_UP_TIMEOUT_SECONDS = 60;
	// time Tomcat gets to start shutting down after the watchdog removed the flag
	private static final long SHUTDOWN_TIMEOUT_SECONDS = 120;
	private static final long MONITOR_INTERVAL_MILLIS = 2000;
	private static final int WATCHDOG_LOG_TAIL_LINES = 20;
	// time the watchdog gets after startup to report the service state for the pid we have written
	private static final long STARTUP_CHECK_DELAY_SECONDS = 30;
	private static final String STATUS_ERROR_PREFIX = "ERROR";

	private static final String INSTANCES_DIR = SERVICE_DIR + "instances" + File.separator;
	private static final String INSTANCE_SUFFIX = ".instance";
	private static final long INSTANCE_HEARTBEAT_SECONDS = 30;
	// an instance file not updated for this long belongs to a Tomcat that is gone (e.g. killed)
	private static final long INSTANCE_MAX_AGE_SECONDS = 90;
	// left over instance files older than this are removed
	private static final long INSTANCE_CLEANUP_AGE_SECONDS = 24 * 60 * 60;
	private static final String INSTANCE_FILE = INSTANCES_DIR + UUID.randomUUID() + INSTANCE_SUFFIX;

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
			LOGGER.info("Tomcat restart requested by {}, creating restart flag {} for the watchdog", user, FLAG_PATH);
		}
		// the pid tells the watchdog which service to restart, also if the flag was left over
		Files.writeString(flag.toPath(), "pid=" + ProcessHandle.current().pid() + System.lineSeparator());

		registerShutdownListener();
		status = new RestartStatus(RestartState.REQUESTED, "Restart requested, waiting for the watchdog to pick it up.");
		Thread monitor = new Thread(TomcatServiceRestarter::monitorRestart, "Tomcat-Restart-Monitor");
		monitor.setDaemon(true);
		monitor.start();
		return status;
	}

	/**
	 * Called at startup of the wiki: publishes the process id for the watchdog and checks, after the watchdog had
	 * time to pick it up, whether a restart would work. Problems are logged as warnings, so they are noticed before
	 * somebody actually needs the restart.
	 */
	public static void onStartup() {
		if (!new File(SERVICE_DIR).isDirectory()) {
			LOGGER.info("Tomcat restart watchdog is not set up (no directory {})", SERVICE_DIR);
			return;
		}
		writePidFile();
		registerInstance();
		if (!new File(ALIVE_PATH).exists()) {
			LOGGER.info("Tomcat restart watchdog is not set up (no heartbeat file {})", ALIVE_PATH);
			return;
		}
		Thread check = new Thread(() -> {
			try {
				Thread.sleep(STARTUP_CHECK_DELAY_SECONDS * 1000);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			String problem = getWatchdogProblem();
			if (problem == null) {
				LOGGER.info("Tomcat restart watchdog is ready: {}", Objects.requireNonNullElse(getWatchdogServiceStatus(),
						"running, but it does not report the service state (outdated watchdog script?)"));
			}
			else {
				LOGGER.warn("Restarting Tomcat via the watchdog will currently not work: {}", problem);
			}
		}, "Tomcat-Restart-Watchdog-Check");
		check.setDaemon(true);
		check.start();
	}

	/**
	 * Writes the heartbeat file of this instance and keeps it up to date until the web app is destroyed.
	 */
	private static void registerInstance() {
		Properties properties = new Properties();
		properties.setProperty("pid", String.valueOf(ProcessHandle.current().pid()));
		properties.setProperty("host", getHostName());
		properties.setProperty("started", Instant.now().toString());
		properties.setProperty("catalina.base", String.valueOf(System.getProperty("catalina.base")));
		Path file = Path.of(INSTANCE_FILE);
		try {
			writeInstanceFile(file, properties);
		}
		catch (IOException e) {
			LOGGER.warn("Cannot register this Tomcat in {}, other Tomcats using the same restart directory cannot detect it",
					INSTANCES_DIR, e);
			return;
		}
		ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor(runnable -> {
			Thread thread = new Thread(runnable, "Tomcat-Restart-Instance-Heartbeat");
			thread.setDaemon(true);
			return thread;
		});
		heartbeat.scheduleAtFixedRate(() -> {
			try {
				// rewritten if it is gone, e.g. removed by hand or by another Tomcat
				if (!file.toFile().setLastModified(System.currentTimeMillis())) writeInstanceFile(file, properties);
			}
			catch (IOException e) {
				LOGGER.warn("Cannot update the heartbeat of this Tomcat in {}", file, e);
			}
		}, INSTANCE_HEARTBEAT_SECONDS, INSTANCE_HEARTBEAT_SECONDS, TimeUnit.SECONDS);
		ServletContextEventListener.registerOnContextDestroyedTask(event -> {
			heartbeat.shutdownNow();
			try {
				Files.deleteIfExists(file);
			}
			catch (IOException e) {
				LOGGER.warn("Cannot remove the heartbeat file {} of this Tomcat", file, e);
			}
		});
	}

	private static void writeInstanceFile(Path file, Properties properties) throws IOException {
		Files.createDirectories(file.getParent());
		try (OutputStream out = Files.newOutputStream(file)) {
			properties.store(out, "Tomcat using this restart directory, see TomcatServiceRestarter");
		}
	}

	/**
	 * Returns descriptions of the other running Tomcats that use the same restart directory. Files of Tomcats that
	 * are gone are ignored, and removed if they are from this machine or older than a day.
	 */
	private static List<String> getOtherInstances() {
		File[] files = new File(INSTANCES_DIR).listFiles((dir, name) -> name.endsWith(INSTANCE_SUFFIX));
		if (files == null) return List.of();
		String ownFile = new File(INSTANCE_FILE).getName();
		String ownHost = getHostName();
		List<String> others = new ArrayList<>();
		for (File file : files) {
			if (file.getName().equals(ownFile)) continue;
			long age = getAgeSeconds(file);
			if (age > INSTANCE_CLEANUP_AGE_SECONDS) {
				// left over by a Tomcat that did not shut down properly
				//noinspection ResultOfMethodCallIgnored
				file.delete();
				continue;
			}
			Properties properties = new Properties();
			try (InputStream in = Files.newInputStream(file.toPath())) {
				properties.load(in);
			}
			catch (IOException e) {
				continue;
			}
			String host = properties.getProperty("host", "?");
			String pid = properties.getProperty("pid", "?");
			boolean sameHost = host.equals(ownHost);
			// another web app in this Tomcat (same JVM) is restarted together with us, so it is no conflict
			if (sameHost && pid.equals(String.valueOf(ProcessHandle.current().pid()))) continue;
			// a Tomcat that is gone: on this machine its process is not running (e.g. killed before it could remove
			// its file), in general its file is no longer updated (this also covers a pid reused by another process)
			if ((sameHost && !isProcessAlive(pid)) || age > INSTANCE_MAX_AGE_SECONDS) {
				//noinspection ResultOfMethodCallIgnored
				if (sameHost) file.delete();
				continue;
			}
			others.add("process " + pid + " on " + host + ", started " + properties.getProperty("started", "?")
					   + ", catalina.base " + properties.getProperty("catalina.base", "?"));
		}
		return others;
	}

	private static boolean isProcessAlive(String pid) {
		long processId;
		try {
			processId = Long.parseLong(pid);
		}
		catch (NumberFormatException e) {
			// unreadable pid, rather keep it than remove the file of a running Tomcat, the age decides
			return true;
		}
		Optional<ProcessHandle> process = ProcessHandle.of(processId);
		if (process.isPresent()) return process.get().isAlive();
		// on Windows, ProcessHandle.of is empty for processes we are not allowed to open, e.g. another Tomcat service,
		// but they are listed in the snapshot of all processes
		return ProcessHandle.allProcesses().anyMatch(other -> other.pid() == processId);
	}

	private static String getHostName() {
		String host = System.getenv(OS.WINDOWS.isCurrentOS() ? "COMPUTERNAME" : "HOSTNAME");
		if (host != null && !host.isBlank()) return host;
		try {
			return InetAddress.getLocalHost().getHostName();
		}
		catch (IOException e) {
			return "unknown";
		}
	}

	private static void writePidFile() {
		try {
			Files.writeString(Path.of(PID_PATH), ProcessHandle.current().pid() + System.lineSeparator());
		}
		catch (IOException e) {
			LOGGER.warn("Cannot write process id to {}, the watchdog has to rely on the configured service name", PID_PATH, e);
		}
	}

	/**
	 * Returns the state of the Tomcat service as reported by the watchdog in the second line of the heartbeat file,
	 * starting with "OK" or "ERROR", or null if the watchdog does not report it (e.g. older watchdog scripts).
	 */
	public static @Nullable String getWatchdogServiceStatus() {
		File alive = new File(ALIVE_PATH);
		if (!alive.exists()) return null;
		try {
			// strip the BOM Windows PowerShell writes with UTF8 encoding
			String content = new String(Files.readAllBytes(alive.toPath()), StandardCharsets.UTF_8).replace("\uFEFF", "");
			List<String> lines = content.lines().map(String::trim).filter(line -> !line.isEmpty()).toList();
			return lines.size() < 2 ? null : lines.get(1);
		}
		catch (IOException e) {
			LOGGER.warn("Cannot read watchdog heartbeat file {}", ALIVE_PATH, e);
			return null;
		}
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
							 + " on the server (e.g. Tomcat service not found or missing permission to restart the service).";
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
		List<String> otherInstances = getOtherInstances();
		if (!otherInstances.isEmpty()) {
			return "Several Tomcats use the restart directory " + SERVICE_DIR + ": this one (process "
				   + ProcessHandle.current().pid() + ") and " + String.join("; ", otherInstances)
				   + ". A restart could hit the wrong Tomcat, so it is disabled. Give each Tomcat its own directory "
				   + "(-Dknowwe.tomcat.restart.dir) and its own watchdog.";
		}
		String serviceStatus = getWatchdogServiceStatus();
		if (serviceStatus != null && serviceStatus.startsWith(STATUS_ERROR_PREFIX)) {
			return "The watchdog reports: " + serviceStatus.substring(STATUS_ERROR_PREFIX.length()).trim();
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
