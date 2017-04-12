/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import de.knowwe.dialog.action.ExternalViewer;
import de.knowwe.dialog.action.StartCase;

import com.denkbares.plugin.Extension;
import com.denkbares.plugin.PluginManager;
import com.denkbares.strings.Locales;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import com.denkbares.utils.Streams;
import de.knowwe.core.action.Action;
import de.knowwe.core.action.UserActionContext;

import static com.denkbares.strings.Strings.Encoding.UTF8;

public class Utils {

	private static final String EXTENDED_PLUGIN_ID = "KnowWEExtensionPoints";
	private static final String EXTENDED_POINT_ID = "Action";

	private static File rootDirectory = null;

	public static String encodeXML(String text) {
		return Strings.encodeHtml(text);
	}

	public static Action getAction(String actionName) {
		PluginManager manager = PluginManager.getInstance();
		Extension[] extensions = manager.getExtensions(EXTENDED_PLUGIN_ID, EXTENDED_POINT_ID);
		for (Extension e : extensions) {
			if (e.getName().equals(actionName)) {
				return ((Action) e.getSingleton());
			}
		}
		Log.warning("Action: \"" + actionName + "\" not found, check plugin.xml.");
		return null;
	}

	public static File getRootDirectory(UserActionContext context) {
		String root = (String) context.getServletContext().getAttribute("rootDirectory");
		if (root != null) return new File(root);
		if (rootDirectory != null) return rootDirectory;
		return new File(context.getServletContext().getRealPath("/"));
	}

	/**
	 * Parses a Locale from a Locale.toString() form
	 *
	 * @param localeString the locale to be parsed
	 * @return the parsed locale
	 * @created 14.12.2010
	 */
	public static Locale parseLocale(String localeString) {
		return Locales.parseLocale(localeString);
	}

	public static void redirectToErrorPage(UserActionContext context, Throwable e) throws IOException {
		String language = context.getParameter(StartCase.PARAM_LANGUAGE);
		String message = e.getLocalizedMessage();
		String trace = Strings.stackTrace(e);
		if (trace.length() > 600) trace = trace.substring(0, 596) + " ...";
		context.sendRedirect("Resource/ui.zip/html/selectBase.html?" +
				StartCase.PARAM_LANGUAGE + "=" + language +
				"&errorMessage=" + Strings.encodeURL(message) +
				"&errorDetails=" + Strings.encodeURL(trace));

	}

	public static void setRootDirectory(File rootDirectory) throws IOException {
		Utils.rootDirectory = rootDirectory;
		installViewers(rootDirectory);
	}

	private static void installViewers(File rootDirectory) throws IOException {
		File configFile = new File(rootDirectory, "../viewers.xml");

		// copy default viewer file to root directory
		// if it not already exists
		if (!configFile.exists()) {
			ClassLoader loader = ExternalViewer.class.getClassLoader();
			InputStream in = loader.getResourceAsStream("viewers.xml");
			if (in == null) {
				// fallback: install empty viewers if defaults not found
				String xml = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?>\n<viewers></viewers>";
				in = new ByteArrayInputStream(xml.getBytes(UTF8.charset()));
			}
			OutputStream out = new FileOutputStream(configFile);
			Streams.streamAndClose(in, out);
		}

		// load the file
		ExternalViewer.loadViewers(configFile);
	}
}
