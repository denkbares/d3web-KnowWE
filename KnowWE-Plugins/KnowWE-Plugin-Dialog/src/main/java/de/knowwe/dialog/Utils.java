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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.denkbares.utils.Streams;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.action.Action;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.utils.KnowWEUtils;

import static com.denkbares.strings.Strings.Encoding.UTF8;

public class Utils {
	private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

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
		LOGGER.warn("Action: \"" + actionName + "\" not found, check plugin.xml.");
		return null;
	}

	/**
	 * Checks whether the current dialog user is allowed to view the knowledge base the dialog is based on. The
	 * knowledge base is taken from the dialog session; the wiki page defining it is found by matching it against the
	 * registered {@link D3webCompiler}s, and read access to that page is asserted.
	 * <p>
	 * Dialogs that are not backed by a wiki page (e.g. the file based mobile application) have no page to check and
	 * pass. Actions should call this and declare {@code Access.HELPER} for it, because the check lives here and not in
	 * the action itself.
	 *
	 * @param context the context of the current dialog action
	 * @throws de.knowwe.core.wikiConnector.NotAuthorizedException if the user may not view the knowledge base page
	 */
	public static void assertCanViewKnowledgeBase(UserActionContext context) {
		KnowledgeBase kb = (KnowledgeBase) context.getSession()
				.getAttribute(SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);
		if (kb == null) return;
		for (D3webCompiler compiler : Compilers.getCompilers(context, context.getArticleManager(), D3webCompiler.class)) {
			if (compiler.getKnowledgeBase() == kb) {
				KnowWEUtils.assertCanView(compiler.getCompileSection().getArticle().getTitle(), context);
				return;
			}
		}
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
		Locale locale = Locales.parseLocale(localeString);
		return locale == null ? Locale.ROOT : locale;
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
