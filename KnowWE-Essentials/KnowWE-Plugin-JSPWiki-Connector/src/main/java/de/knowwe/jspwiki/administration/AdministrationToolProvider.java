/*
 * Copyright (C) 2023 denkbares GmbH, Germany
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import com.denkbares.collections.Iterators;
import com.denkbares.utils.Streams;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.Action;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.readOnly.ReadOnlyManager;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * Tools for %%Admin markup
 *
 * @author Tim Abler
 * @created 09.10.2018
 */
public class AdministrationToolProvider extends AbstractAction implements ToolProvider {

	public static final String THREAD_DUMP = "thread-dump";
	public static final String THREAD_DUMP_JCMD = "thread-dump-jcmd";
	public static final String LOGS_RECENT = "logs-recent";
	public static final String LOGS_ALL = "logs-all";
	public static final String RESTART_WEBAPP = "restart-webapp";

	@Override
	public Tool[] getTools(Section<?> section, UserContext user) {
		if (user.userIsAdmin()) {
			boolean readonly = ReadOnlyManager.isReadOnly();

			String js = "javascript:KNOWWE.plugin.jspwikiConnector.setReadOnly(" + readonly + ")";

			Tool readOnlyTool;
			if (readonly) {
				readOnlyTool = new DefaultTool(
						Icon.TOGGLE_ON,
						"Deactivate ReadOnly Mode",
						"Disclaimer: This is a purely administrative feature and should not be used for security purposes, because it is not secure.",
						js,
						Tool.CATEGORY_LAST);
			}
			else {
				readOnlyTool = new DefaultTool(
						Icon.TOGGLE_OFF,
						"Activate ReadOnly Mode",
						"Disclaimer: This is a purely administrative feature and should not be used for security purposes, because it is not secure.",
						js,
						Tool.CATEGORY_LAST);
			}
			DefaultTool threadDumpTool = new DefaultTool(
					Icon.FILE_TEXT,
					"Download thread dump",
					"Generate and download a thread dump",
					"window.location='action/AdministrationToolProvider?type=" + THREAD_DUMP + "'",
					Tool.CATEGORY_DOWNLOAD
			);

			DefaultTool threadDumpJcmdTool = new DefaultTool(
					Icon.FILE_TEXT,
					"Download thread dump (jcmd)",
					"Generate and download a thread dump using jcmd",
					"window.location='action/AdministrationToolProvider?type=" + THREAD_DUMP_JCMD + "'",
					Tool.CATEGORY_DOWNLOAD
			);

			DefaultTool downloadRecent = new DefaultTool(
					Icon.FILE_TEXT,
					"Download recent logs",
					"Download the most recent logs of this wiki",
					"window.location='action/AdministrationToolProvider" +
					"?type=" + LOGS_RECENT + "'",
					Tool.CATEGORY_DOWNLOAD);

			DefaultTool downloadAll = new DefaultTool(Icon.FILE_ZIP,
					"Download all logs",
					"Get all logs of this wiki as a zip file",
					"window.location='action/AdministrationToolProvider" +
					"?type=" + LOGS_ALL + "'",
					Tool.CATEGORY_DOWNLOAD);

			DefaultTool restartWebApp = new DefaultTool(Icon.REFRESH,
					"Restart Web App",
					"Restart this web app. This may take a while",
					"KNOWWE.plugin.jspwikiConnector.restartWebApp()",
					Tool.ActionType.ONCLICK,
					Tool.CATEGORY_EXECUTE);

			return new Tool[] { readOnlyTool, threadDumpTool, threadDumpJcmdTool, downloadRecent, downloadAll, restartWebApp };
		}
		else {
			return null;
		}
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return userContext.userIsAdmin();
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		if (!context.userIsAdmin()) {
			fail(context, HttpServletResponse.SC_FORBIDDEN, "This method is only available for administrators");
		}

		context.setContentType(Action.PLAIN_TEXT);

		String type = context.getParameter("type");
		if (THREAD_DUMP.equals(type)) {
			downloadThreadDump(context, KnowWEUtils.getThreadDump(), "Thread-Dump");
		}
		else if (THREAD_DUMP_JCMD.equals(type)) {
			downloadThreadDump(context, KnowWEUtils.getThreadDumpViaJcmd(), "JDMC-Thread-Dump");
		}
		else if (LOGS_RECENT.equals(type)) {
			List<File> logFiles = getLogFilePaths();
			downloadToday(context, logFiles);
		}
		else if (LOGS_ALL.equals(type)) {
			List<File> logFiles = getAllLogPaths();
			downloadToday(context, logFiles);
		}
		else if (RESTART_WEBAPP.equals(type)) {
			JmxWebAppRestarter.reload(context.getServletContext());
		}
		else {
			failUnexpected(context, "Unknown tool type: " + type);
		}
	}

	private void downloadThreadDump(UserActionContext context, String threadDump, String fileName) throws IOException {
		fileName = new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()) + "-" + fileName + "-" + Environment.getInstance()
				.getWikiConnector()
				.getApplicationName() + ".txt";

		context.setContentType(Action.BINARY);
		context.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

		try (OutputStream out = context.getOutputStream()) {
			out.write(threadDump.getBytes(StandardCharsets.UTF_8));
			out.flush();
		}
	}

	private void downloadToday(UserActionContext context, List<File> logFiles) throws IOException {
		Path tempFile = Files.createTempFile("Log-Download-" + new Date().getTime(), ".zip");
		if (logFiles.size() > 1) {
			// create tmp file first to be able to send error to user, if there is an exception
			File tmpFile = tempFile.toFile();
			tmpFile.deleteOnExit();
			try (OutputStream tmpOutStream = new FileOutputStream(tmpFile); ZipOutputStream zos = new ZipOutputStream(tmpOutStream)) {
				for (File logFile : logFiles) {
					if (logFile.exists()) {
						ZipEntry zipEntry = new ZipEntry(logFile.getName());
						zos.putNextEntry(zipEntry);
						try (InputStream in = new FileInputStream(logFile)) {
							Streams.stream(in, zos);
						}
						finally {
							zos.closeEntry();
						}
					}
				}
			}
			catch (Exception ioe) {
				context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, String.valueOf(ioe));
				tmpFile.delete();
				tmpFile = null;
			}
			finally {
				if (tmpFile != null) {
					String applicationName = Environment.getInstance().getWikiConnector().getApplicationName();
					String filename = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "-" + applicationName + "-logs.zip";
					context.setContentType(BINARY);
					context.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");

					try (FileInputStream in = new FileInputStream(tmpFile); OutputStream out = context.getOutputStream()) {
						Streams.stream(in, out);
					}
					finally {
						tmpFile.delete();
					}
				}
			}
		}
		else if (logFiles.size() == 1) {
			File logFile = logFiles.get(0);
			String filename = logFile.getName();
			context.setContentType(BINARY);
			context.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
			try (InputStream in = new FileInputStream(logFile); OutputStream out = context.getOutputStream()) {
				Streams.stream(in, out);
			}
		}
		else {
			context.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No logs found!");
		}
	}

	private List<File> getLogFilePaths() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		return context.getLoggerList().stream()
				.flatMap(logger -> Iterators.stream(logger.iteratorForAppenders()))
				.filter(appender -> appender instanceof FileAppender)
				.map(appender -> ((FileAppender<ILoggingEvent>) appender).getFile())
				.distinct()
				.map(File::new)
				.filter(File::exists)
				.toList();
	}

	private List<File> getAllLogPaths() {
		return getLogFilePaths().stream()
				.map(File::getParentFile)
				.distinct()
				.map(File::listFiles)
				.filter(Objects::nonNull)
				.flatMap(Arrays::stream)
				.toList();
	}
}
